package com.whoamie.cinetime_nepal.common.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.network.API;
import com.whoamie.cinetime_nepal.common.network.HandleNetworkError;
import com.whoamie.cinetime_nepal.common.network.RestClient;
import com.whoamie.cinetime_nepal.common.utils.CheckConnectivity;
import com.whoamie.cinetime_nepal.common.utils.CustomProgressDialog;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;
import com.whoamie.cinetime_nepal.common.utils.Validator;
import com.whoamie.cinetime_nepal.member.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedBackActivity extends AppCompatActivity {
    EditText nameEt,contactEt,descEt;
    Button btnSubmit;
    CustomProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Send Feedbacks");
        initViews();
        onCLick();
        SharedPreferences preferences = getSharedPreferences(SharedPref.key_shared_pref,MODE_PRIVATE);
        String user1 = preferences.getString("user",null);
        User user = new Gson().fromJson(user1,User.class);
        if (user1 != null){
            nameEt.setText(user.getName());
            contactEt.setText(user.getEmail());
        }
    }

    private void onCLick() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString();
                String contact = contactEt.getText().toString();
                String description = descEt.getText().toString();
                if (!name.isEmpty() && !contact.isEmpty() && !description.isEmpty()){
                    if (Validator.isEmailValid(contact)){
                        callCallFeedBackAPi(name,contact,description);
                    }
                    else {
                        contactEt.setError("Invalid Email Address");
                    }
                }
                else {
                    nameEt.setError("Name field is required");
                    contactEt.setError("Email field is required");
                    descEt.setError("Message field is required");
                }
            }
        });
    }

    private void callCallFeedBackAPi(String name, String contact, String description) {
        dialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("name", name);
            object.put("contact", contact);
            object.put("message", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API.sendFeedback, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialog.dismiss();
                try {
                    if (response.getBoolean("status")){
                        Toast.makeText(FeedBackActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        nameEt.setText("");
                        contactEt.setText("");
                        descEt.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                HandleNetworkError.handlerError(error,FeedBackActivity.this);
            }
        });
        if (CheckConnectivity.isNetworkAvailable(FeedBackActivity.this)) {
            RestClient.getInstance(FeedBackActivity.this).addToRequestQueue(jsonObjectRequest);
        } else {
            dialog.dismiss();
            Toast.makeText(FeedBackActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        nameEt=findViewById(R.id.feedback_name_et);
        contactEt=findViewById(R.id.feedback_contact_et);
        descEt=findViewById(R.id.feedback_desc_et);
        btnSubmit=findViewById(R.id.feedback_submit_btn);
        dialog = new CustomProgressDialog(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
