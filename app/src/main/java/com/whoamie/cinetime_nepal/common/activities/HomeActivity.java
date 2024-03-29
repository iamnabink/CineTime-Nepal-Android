package com.whoamie.cinetime_nepal.common.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.fragments.HallFragment;
import com.whoamie.cinetime_nepal.common.fragments.MovieFragment;
import com.whoamie.cinetime_nepal.common.fragments.NotificationFragement;
import com.whoamie.cinetime_nepal.common.fragments.HomeFragment;
import com.whoamie.cinetime_nepal.common.fragments.RegisterFragment;
import com.whoamie.cinetime_nepal.common.utils.SharedPref;
import com.whoamie.cinetime_nepal.member.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Toolbar myToolbar;
    private static final String TAG = "HomeActivity";
    Menu menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        replaceFragment(new MovieFragment(), "MovieFragment");
        FirebaseMessaging.getInstance().subscribeToTopic("all"); //to notify all user
        setUpBottomNavigation();
//        handleFirebaseNotification();
    }

    private void handleFirebaseNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("all"); //to notify all user
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        System.out.println("Tokern------------->" + msg);
                        Log.d(TAG, msg);
                        Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        menuItem = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
//        if (id == R.id.hall_location) {
//            checkLocationpermission();
//        }
        if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpBottomNavigation() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottomnav_home:
                                replaceFragment(new HomeFragment(), "HomeFragment");
                                break;
                            case R.id.bottomnav_profile:
//                              if (SharedPref.name has  user data go to userprofile fragment else replaceFragment() with log in fragment;)
                                SharedPreferences preferences = getSharedPreferences(SharedPref.key_shared_pref, MODE_PRIVATE);
                                String userToken = preferences.getString(SharedPref.key_user_token, null);
                                String userDetails = preferences.getString(SharedPref.key_user_details, null);
                                if (userToken == null && userDetails == null) {
                                    replaceFragment(new RegisterFragment(), "RegisterFragment");

                                } else {
                                    replaceFragment(new ProfileFragment(), "ProfileFragment");
                                }
                                break;
                            case R.id.bottomnav_notification:
                                replaceFragment(new NotificationFragement(), "NotificationFragement");
                                break;
                            case R.id.bottomnav_hall:
                                replaceFragment(new HallFragment(), "HallFragment");
                                break;
                            case R.id.bottomnav_movies:
                                replaceFragment(new MovieFragment(), "MovieFragment");
                                break;
                        }
                        return true; //not false (it will automatically make bottom icon selected
                    }
                });
        bottomNavigationView.setSelectedItemId(R.id.bottomnav_movies);
    }

    private void replaceFragment(Fragment fragment, String s) {
        // Create new fragment and transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.container, fragment, s);
        // transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

}
