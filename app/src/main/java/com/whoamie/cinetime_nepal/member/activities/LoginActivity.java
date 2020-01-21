package com.whoamie.cinetime_nepal.member.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.activities.SplashScreenActivity;
import com.whoamie.cinetime_nepal.common.network.API;
import com.whoamie.cinetime_nepal.common.network.RestClient;
import com.whoamie.cinetime_nepal.common.utils.ProgressDialog;
import com.whoamie.cinetime_nepal.common.utils.CheckConnectivity;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;
import com.whoamie.cinetime_nepal.common.utils.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    TextView signupTv, emailEt, pwdEt,forgetPwdTv;
    Button signinBtn;
    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initVar();
        clickListener();
    }

    private void initVar() {
        emailEt = findViewById(R.id.email_et);
        pwdEt = findViewById(R.id.pwd_et);
        signupTv = findViewById(R.id.signup_tv);
        signinBtn = findViewById(R.id.signin_btn);
        forgetPwdTv=findViewById(R.id.forget_pwd_tv);
    }

    private void clickListener() {
        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString();
                String password = pwdEt.getText().toString();
                if (!Validator.isEmailValid(email)) {
                    emailEt.setError("Invalid email address");
                } else if (password.isEmpty()) {
                    pwdEt.setError("please enter password");
                } else if (password.length() < 5) {
                    pwdEt.setError("password must be greater than 5 character");
                } else {
                    signIn();
                }
            }
        });
        signupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });
        forgetPwdTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPwdActivity.class));
            }
        });
    }

    private void signIn() {
        final ProgressDialog dialog = new ProgressDialog(this);
        Window window = dialog.getWindow();
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        preferences = getSharedPreferences(SharedPref.key_shared_pref, MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", emailEt.getText().toString());
            jsonObject.put("password", pwdEt.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API.loginUrl, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialog.cancel();
                try {
                    if (response.getBoolean("status")) {
                        System.out.println("response----->" + response);
                        JSONObject dataObject = response.getJSONObject(SharedPref.key_data_details);
                        JSONObject userObject = dataObject.getJSONObject(SharedPref.key_user_details);
                        JSONObject tokernObject = dataObject.getJSONObject(SharedPref.key_user_token);
                        String userDetails = userObject.toString(); //convert JSONObject to string
                        String tokenDetails = tokernObject.toString();
                        editor.putString(SharedPref.key_user_details, userDetails);
                        editor.putString(SharedPref.key_user_token, tokenDetails);
                        editor.apply();
//                        System.out.println("errrrror----------->"+preferences.getAll());
                        Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        //Go to user profile fragment
                        startActivity(new Intent(LoginActivity.this, SplashScreenActivity.class));
                    } else {

                        Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        System.out.println("error-->" + response.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error------->" + error);
                dialog.cancel();
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(LoginActivity.this, "Password or email do not match", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Server error please try again later", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }

            }
        });
        if (CheckConnectivity.isNetworkAvailable(getApplicationContext())){
            RestClient.getInstance(this).addToRequestQueue(request);
        }
        else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            dialog.cancel();
        }
    }

}