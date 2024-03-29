package com.whoamie.cinetime_nepal.common.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.activities.MovieDetailActivity;
import com.whoamie.cinetime_nepal.common.activities.ShowingMovieActivity;
import com.whoamie.cinetime_nepal.common.activities.UpComingMovieActivity;
import com.whoamie.cinetime_nepal.common.adapter.SearchMovieAdapter;
import com.whoamie.cinetime_nepal.common.adapter.MovieFragmentAdapter;
import com.whoamie.cinetime_nepal.common.interfaces.AdapterClickListener;
import com.whoamie.cinetime_nepal.common.models.Movie;
import com.whoamie.cinetime_nepal.common.network.API;
import com.whoamie.cinetime_nepal.common.network.HandleNetworkError;
import com.whoamie.cinetime_nepal.common.network.RestClient;
import com.whoamie.cinetime_nepal.common.utils.CustomProgressDialog;
import com.whoamie.cinetime_nepal.common.utils.CheckConnectivity;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.sentry.core.Sentry;

public class MovieFragment extends Fragment {
    RecyclerView showsShowingRecyclerV, showsComingRecyclerV, searchRecyclerView;
    ArrayList<Movie> umovies = new ArrayList<>();
    ArrayList<Movie> smovies = new ArrayList<>();
    ArrayList<Movie> movies = new ArrayList<>();
    //    MovieActivityAdapter uadapter;
    MovieFragmentAdapter adapterS, adapterU;
    SearchMovieAdapter searchMovieAdapter;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    CustomProgressDialog dialog;
    View noInternetView, view, linearLayout;
    SwipeRefreshLayout refreshLayout;
    private Context mContext;
    TextView showingMoreBtn, comingMorebtn;
    ShimmerFrameLayout shimmerFrameLayoutU, shimmerFrameLayoutS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_movie, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);
        intiVar();
        initViews();
        AccessToken accessToken = AccessToken.getCurrentAccessToken(); //check use logged in status
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
//        if (isLoggedIn){
//            Toast.makeText(mContext, "Logged in with fb - true", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Toast.makeText(mContext, "Logged in with fb - false", Toast.LENGTH_SHORT).show();
//        }
        listeners();
        loadMovieData();
        onRefresh();
        searchView();
        onClick();
//        try {
//            throw new Exception("This is a test.");
//        } catch (Exception e) {
//            Sentry.captureException(e);
//        }
//        Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_LONG).show();
        return view;
    }

    private void onClick() {
        comingMorebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UpComingMovieActivity.class));
            }
        });
        showingMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ShowingMovieActivity.class));
            }
        });
    }

    private void searchView() {
        searchRecyclerView = linearLayout.findViewById(R.id.search_recycler_view);
        searchMovieAdapter = new SearchMovieAdapter(movies, getContext(), new AdapterClickListener() {
            @Override
            public void onClick(int position, View view) {
                Movie movie = movies.get(position);
                String movieDetails = new Gson().toJson(movie);
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra(SharedPref.key_shared_movies_details, movieDetails);
                startActivity(intent);
            }
        });
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(searchMovieAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) { //one prepare listner an be used to show or hide actionbar icon in fragment : don't forget to add : setHasOptionsMenu(true); in oncreate : fragmnt
        menu.findItem(R.id.hall_location).setVisible(false);
        menu.findItem(R.id.search).setVisible(true);
        menu.findItem(R.id.settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) { //we can over write oncreate listner in fragment to customize as we like
        final MenuItem menuItem = menu.findItem(R.id.search);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                linearLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Movies by Name or Genre");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMovieAdapter.getFilter().filter(query, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {
//                        Toast.makeText(mContext, "Total Item--> "+count, Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(mContext, "text changing", Toast.LENGTH_SHORT).show();
                searchMovieAdapter.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
//                    searchView.setIconified(false);
                    menuItem.collapseActionView();
                    searchView.setQuery("", false);
                    linearLayout.setVisibility(View.GONE);

                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void onRefresh() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dialog.show();
                loadMovieData();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void listeners() {
        noInternetView.findViewById(R.id.button_try_again) //button doesn't need to be cast since it doesn't have any specific property (but note: EditText should be casted)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMovieData();
                    }

                });
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayoutS.startShimmer();
        shimmerFrameLayoutU.startShimmer();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayoutS.stopShimmer();
        shimmerFrameLayoutU.stopShimmer();
    }

    //initializing views variables
    private void intiVar() {
        shimmerFrameLayoutS = view.findViewById(R.id.shmovie_shimmer_layout);
        showingMoreBtn = view.findViewById(R.id.showing_more_btn);
        comingMorebtn = view.findViewById(R.id.coming_soon_more_btn);
        shimmerFrameLayoutU = view.findViewById(R.id.upmovie_shimmer_layout);
        refreshLayout = view.findViewById(R.id.swipe_refresh_l);
        noInternetView = view.findViewById(R.id.view_no_internet);
        showsShowingRecyclerV = view.findViewById(R.id.shows_showing_recycler_v);
        showsComingRecyclerV = view.findViewById(R.id.shows_coming_recycler_v);
        linearLayout = view.findViewById(R.id.search_layout);
        dialog = new CustomProgressDialog(mContext);
//        Window window = dialog.getWindow();
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800);
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        preferences = getContext().getSharedPreferences(SharedPref.key_shared_pref, Context.MODE_PRIVATE);
    }

    //initialize views
    private void initViews() {
        adapterU = new MovieFragmentAdapter(umovies, getContext(), new AdapterClickListener() {
            @Override
            public void onClick(int position, View view) {
                Movie movie = umovies.get(position);
                String movieDetails = new Gson().toJson(movie);
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra(SharedPref.key_shared_movies_details, movieDetails);
                startActivity(intent);
            }
        });
        adapterS = new MovieFragmentAdapter(smovies, getContext(), new AdapterClickListener() {
            @Override
            public void onClick(int position, View view) {
                Movie movie = smovies.get(position);
                String movieDetails = new Gson().toJson(movie);
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra(SharedPref.key_shared_movies_details, movieDetails);
                startActivity(intent);
            }
        });
        showsComingRecyclerV.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        showsShowingRecyclerV.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        showsComingRecyclerV.setAdapter(adapterU);
        showsShowingRecyclerV.setAdapter(adapterS);
    }

    //consuming web apis //loading data from server
    private void loadMovieData() {
        smovies.clear();
        umovies.clear();
        shimmerFrameLayoutS.setVisibility(View.VISIBLE);
        shimmerFrameLayoutU.setVisibility(View.VISIBLE);
        shimmerFrameLayoutU.startShimmer();
        shimmerFrameLayoutS.startShimmer();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API.getMoviesDetail, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                shimmerFrameLayoutS.stopShimmer();
                shimmerFrameLayoutS.setVisibility(View.GONE);
                shimmerFrameLayoutU.stopShimmer();
                shimmerFrameLayoutU.setVisibility(View.GONE);
                dialog.dismiss();
                try {
                    JSONArray showingMovieList = response.getJSONArray("showing");
                    for (int i = 0; i < showingMovieList.length(); i++) {
                        JSONObject movieObject = showingMovieList.getJSONObject(i);
                        Movie movie = new Gson().fromJson(movieObject.toString(), Movie.class);
                        smovies.add(movie);
                    }
                    if (adapterS.getItemCount() == 0) {
                        showsShowingRecyclerV.setVisibility(View.GONE);
                        view.findViewById(R.id.empty_layout_smoviefrag).setVisibility(View.VISIBLE);
                    } else {
                        showsShowingRecyclerV.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.empty_layout_smoviefrag).setVisibility(View.GONE);
                    }
                    adapterS.notifyDataSetChanged();
                    JSONArray comingMoviesList = response.getJSONArray("coming");
                    for (int i = 0; i < comingMoviesList.length(); i++) {
                        JSONObject movieObject = comingMoviesList.getJSONObject(i);
                        Movie movie = new Gson().fromJson(movieObject.toString(), Movie.class);
                        umovies.add(movie);
                    }
                    searchMovieAdapter.notifyDataSetChanged();
                    if (adapterU.getItemCount() == 0) {
                        showsComingRecyclerV.setVisibility(View.GONE);
                        view.findViewById(R.id.empty_layout_cmoviefrag).setVisibility(View.VISIBLE);
                    } else {
                        showsComingRecyclerV.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.empty_layout_cmoviefrag).setVisibility(View.GONE);
                    }
                    adapterU.notifyDataSetChanged();
                    movies.clear();
                    movies.addAll(smovies);
                    movies.addAll(umovies);
                    searchMovieAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Sentry.captureException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                System.out.println(error);
                shimmerFrameLayoutS.stopShimmer();
                shimmerFrameLayoutS.setVisibility(View.GONE);
                shimmerFrameLayoutU.stopShimmer();
                shimmerFrameLayoutU.setVisibility(View.GONE);
                dialog.dismiss();
                try {
                    HandleNetworkError.handlerError(error, mContext);
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
            }
        });
        if (CheckConnectivity.isNetworkAvailable(mContext)) {
            noInternetView.setVisibility(View.GONE);
            RestClient.getInstance(getContext()).addToRequestQueue(request);
        } else {
            noInternetView.setVisibility(View.VISIBLE);
            dialog.dismiss();
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context; //prevents context from being null (since getContext sometimes get null)
    }
}
