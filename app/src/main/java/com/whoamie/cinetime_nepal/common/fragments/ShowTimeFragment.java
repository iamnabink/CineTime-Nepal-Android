package com.whoamie.cinetime_nepal.common.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.adapter.ShowTimeAdapter;
import com.whoamie.cinetime_nepal.common.models.ShowTime;
import com.whoamie.cinetime_nepal.common.network.API;
import com.whoamie.cinetime_nepal.common.network.HandleNetworkError;
import com.whoamie.cinetime_nepal.common.network.RestClient;
import com.whoamie.cinetime_nepal.common.utils.CustomProgressDialog;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowTimeFragment extends Fragment {
    ArrayList<ShowTime> showTimes = new ArrayList<>();
    ShowTimeAdapter adapter;
    RecyclerView recyclerView;
    int movie_id;
    String showDate;
    View view;
    Context mContext;

    public ShowTimeFragment(int movie_id, String showDate) {
        this.movie_id = movie_id;
        this.showDate = showDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_showtime, container, false);
         recyclerView=view.findViewById(R.id.showtime_recycler_v);
         adapter = new ShowTimeAdapter(showTimes,getContext());
         recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
         recyclerView.setAdapter(adapter);
         loadData();
         return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void loadData() {
        showTimes.clear();
        final CustomProgressDialog dialog = new CustomProgressDialog(mContext);
        dialog.show();
//        Window window = dialog.getWindow();
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800);
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("movie_id",movie_id);
            jsonObject.put("date",showDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API.showtimeMovieUrl, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialog.dismiss();
                try {
                    JSONArray showTimeArray = response.getJSONArray(SharedPref.key_data_details);
                    for (int i = 0;i<showTimeArray.length();i++){
                        JSONObject showtimeObject = showTimeArray.getJSONObject(i);
                        ShowTime showTime = new Gson().fromJson(showtimeObject.toString(),ShowTime.class);
                        showTimes.add(showTime);
                    }
                    if (adapter.getItemCount()==0){
                        recyclerView.setVisibility(View.GONE);
                        view.findViewById(R.id.st_empty_view).setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                System.out.println(error);
                HandleNetworkError.handlerError(error, getContext());
                dialog.dismiss();
            }
        });
        RestClient.getInstance(getContext()).addToRequestQueue(request);
    }

}
