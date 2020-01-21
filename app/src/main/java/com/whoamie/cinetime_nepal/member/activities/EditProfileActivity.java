package com.whoamie.cinetime_nepal.member.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.activities.HomeActivity;
import com.whoamie.cinetime_nepal.common.network.API;
import com.whoamie.cinetime_nepal.common.network.AuthenticatedJSONRequest;
import com.whoamie.cinetime_nepal.common.network.RestClient;
import com.whoamie.cinetime_nepal.common.utils.ProgressDialog;
import com.whoamie.cinetime_nepal.common.utils.ImageConverter;
import com.whoamie.cinetime_nepal.common.utils.CheckConnectivity;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;
import com.whoamie.cinetime_nepal.member.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    TextInputEditText editName, editBio;
    TextView changeTv;
    SharedPreferences preferences;
    CircleImageView profileIv;
    SharedPreferences.Editor editor;
    String selectedImagePath; //get image and image path from mobile
    Button editBtn;
    ProgressDialog dialog;
    private static final int IMAGE_PICKER_REQ_CODE = 100;
    private static final int READ_REQ_CODE = 293;
    String profile_pic_url; //gets profile image string

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        initVar();
        loadData();
        editProfile();
        onClickImage();
        onClickLister();
    }

    private void onClickLister() {
        changeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, ChangePwdActivity.class));
            }
        });
    }


    private void onClickImage() {
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //if permission not granted
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_REQ_CODE);
                }
            } else {
                readImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void readImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICKER_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {

                case IMAGE_PICKER_REQ_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        //data gives you the image uri.
                        Uri selectedImageUri = data.getData();
                        selectedImagePath = getPath(selectedImageUri);
                        //System.out.println("Image Path : " + selectedImagePath);
                        profileIv.setImageURI(selectedImageUri);
                        //Converting to bitmap
                        profile_pic_url = ImageConverter.imageConvert(EditProfileActivity.this, selectedImageUri);
//                        System.out.println("base 64??->" + profile_pic_url);
                        break;
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        Log.e(this.getLocalClassName(), "Invalid Image");
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(this.getLocalClassName(), "Exception in onActivityResult : " + e.getMessage());
        }
    }

    public String getPath(Uri uri) { //this function will get path
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void loadData() {
        preferences = getSharedPreferences(SharedPref.key_shared_pref, MODE_PRIVATE);
        String userDetails = preferences.getString(SharedPref.key_user_details, null);
        User user = new Gson().fromJson(userDetails, User.class);
        editName.setText(user.getName());
        editBio.setText(user.getBio());
        Picasso.get().load(user.getProfile_pic_url()).into(profileIv);
    }

    private void initVar() {
        editName = findViewById(R.id.edit_name);
        editBio = findViewById(R.id.edit_bio);
        profileIv = findViewById(R.id.edit_profile_iv);
        editBtn = findViewById(R.id.edit_btn);
        changeTv=findViewById(R.id.chng_pwd_tv);
    }

    private void editProfile() {
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
    }

    private void update() {
        dialog = new ProgressDialog(this);
        Window window = dialog.getWindow();
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", editName.getText().toString());
            jsonObject.put("bio", editBio.getText().toString());
            if (profile_pic_url != null) jsonObject.put("profile_pic_url", profile_pic_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AuthenticatedJSONRequest jsonRequest = new AuthenticatedJSONRequest(this, Request.Method.PUT, API.updateUrl, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialog.cancel();
                try {
                    if (response.getBoolean("status")) {
                        preferences = getSharedPreferences(SharedPref.key_shared_pref, MODE_PRIVATE);
                        editor=preferences.edit();
                        String userDetails = preferences.getString(SharedPref.key_user_details, null);
                        User user = new Gson().fromJson(userDetails, User.class); //changing saved data to object since editing data needs to be updated not replaced
                        JSONObject userData = response.getJSONObject(SharedPref.key_data_details);
                        user.setBio(userData.getString("bio"));
                        user.setName(userData.getString("name"));
                        user.setProfile_pic_url(userData.getString("profile_pic_url"));

                        //only replace name and bio and image url
                        String updatedUserString = new Gson().toJson(user);
                        editor.putString(SharedPref.key_user_details, updatedUserString);
                        editor.apply();
                        Toast.makeText(EditProfileActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                    }
                    else {
                        System.out.println(response.getString("message"));
                        Toast.makeText(EditProfileActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.cancel();
//                System.out.println(error);
                Toast.makeText(EditProfileActivity.this, "Server error! try again later", Toast.LENGTH_SHORT).show();
            }
        });
        if (CheckConnectivity.isNetworkAvailable(getApplicationContext())){
            RestClient.getInstance(this).addToRequestQueue(jsonRequest);
        }
        else {
            dialog.cancel();
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }


}