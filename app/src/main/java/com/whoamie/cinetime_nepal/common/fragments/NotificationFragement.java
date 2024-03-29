package com.whoamie.cinetime_nepal.common.fragments;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.adapter.NotificationAdapter;
import com.whoamie.cinetime_nepal.common.models.Notification;
import com.whoamie.cinetime_nepal.common.network.API;
import com.whoamie.cinetime_nepal.common.network.HandleNetworkError;
import com.whoamie.cinetime_nepal.common.network.RestClient;
import com.whoamie.cinetime_nepal.common.utils.CheckConnectivity;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationFragement extends Fragment {
    View view;
    RecyclerView recyclerView;
    ArrayList<Notification> notifications = new ArrayList<>();
    NotificationAdapter adapter;
    Context context;
    ShimmerFrameLayout shimmerFrameLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        initVar();
        initViews();
        loadData();
        setHasOptionsMenu(true);
        return view;
    }
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.hall_location).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
    private void initViews() {

        adapter = new NotificationAdapter(notifications,getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        shimmerFrameLayout=view.findViewById(R.id.notification_shimmer_effect);
    }
    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.startShimmer();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.stopShimmer();
    }

    private void loadData() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API.getNotification, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                try {
                    JSONObject jsonObject = response.getJSONObject("notifications");
                    JSONArray notificationDataArrya = jsonObject.getJSONArray(SharedPref.key_data_details);
                    for (int i = 0; i <notificationDataArrya.length(); i++){
                        JSONObject object = notificationDataArrya.getJSONObject(i);
                        Notification notification = new Gson().fromJson(object.toString(),Notification.class);
                        notifications.add(notification);
                    }
                    if (adapter.getItemCount()==0){
                        recyclerView.setVisibility(View.GONE);
                        view.findViewById(R.id.empty_v_notification).setVisibility(View.VISIBLE);
                    }
                    else {
                        recyclerView.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.empty_v_notification).setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                HandleNetworkError.handlerError(error, context);
            }
        });
        if (CheckConnectivity.isNetworkAvailable(context)){
            RestClient.getInstance(getContext()).addToRequestQueue(request);
        }
        else {
            shimmerFrameLayout.setVisibility(View.GONE);
            shimmerFrameLayout.stopShimmer();
            Toast.makeText(getContext(), "No network detected", Toast.LENGTH_SHORT).show();
        }
        //making request with retrofit 2
//        APIEndPoints apiEndPoints = API.getInstance().create(APIEndPoints.class);
//        Call<Notification> call = apiEndPoints.getNotifications();
//        call.enqueue(new Callback<Notification>() {
//            @Override
//            public void onResponse(Call<Notification> call, Response<Notification> response) {
//                if (!response.isSuccessful()) {
//                    Toast.makeText(getContext(), "server responding", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                else if(response.body() != null) {
//                    String successmsg,sessionkey;
//                    Notification data = response.body();
//                    Boolean isSuccess = true;
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<Notification> call, Throwable t) {
//                Toast.makeText(getContext(), "Error" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }

    private void initVar() {
        recyclerView=view.findViewById(R.id.notification_recycler_view);
    }
}
