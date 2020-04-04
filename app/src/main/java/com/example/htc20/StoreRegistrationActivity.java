package com.example.htc20;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class StoreRegistrationActivity extends AppCompatActivity {
    private EditText userEmail, uniqueID, shopName;
    private TextInputEditText userPassword;
    private Button register;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private MapsActivity map;
    private Spinner service_category;
    private FusedLocationProviderClient client;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    double Latitude = 0;
    double Longitude = 0;


    private static final String TAG = "DocSnippets";

    protected void setup(){
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_registration);

        userPassword = findViewById(R.id.etSetPassword);
        userEmail = findViewById(R.id.etEmail);
        userLogin = findViewById(R.id.etNotLogin);
        register = findViewById(R.id.etRegister);
        uniqueID = findViewById(R.id.etUniqueID);
        shopName = findViewById(R.id.etShopName);
        service_category = findViewById(R.id.etCategories);
        // map = (MapActivity)......................

        //final GeoPoint gp = new GeoPoint((int)(Latitude * 1E6), (int)(Longitude * 1E6));

        //get location
        client = LocationServices.getFusedLocationProviderClient(this);


        firebaseAuth = FirebaseAuth.getInstance();
//        setup();
        db = FirebaseFirestore.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    final String email = userEmail.getText().toString().trim();
                    String pass = userPassword.getText().toString().trim();
                    String strUniqueID = uniqueID.getText().toString().trim();
                    String shop_name = shopName.getText().toString().trim();
                    String category = String.valueOf(service_category.getSelectedItem());

                    final Map<String, Object> user = new HashMap<>();
                    user.put("unique_id", strUniqueID);
                    user.put("email", email);
                    user.put("shop_name", shop_name);
                    user.put("service_category", category);
                    
                    requestLocationPermission();

                    client.getLastLocation().addOnSuccessListener(StoreRegistrationActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                Latitude =  location.getLatitude();
                                Longitude = location.getLongitude();
                                Log.d("Latitude", "value: "+Latitude);
                                //GeoPoint gp = new GeoPoint(Latitude , Longitude);
                                //Log.d("gp", "val: "+gp);
                                //user.put("shop_loc", gp);
                            }
                        }
                    });
                        user.put("latitude",Latitude);
                        user.put("longitude",Longitude);
                    db.collection("store")
                            .document(strUniqueID)
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot added");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                    String temp_email = strUniqueID + "@htc2020.com";
                    firebaseAuth.createUserWithEmailAndPassword(temp_email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
//                                sendEmailVerification(email);
                                Toast.makeText(StoreRegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(StoreRegistrationActivity.this, StoreLoginActivity.class));

                            } else {
                                Toast.makeText(StoreRegistrationActivity.this, String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreRegistrationActivity.this, StoreLoginActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("requestCode", "value : "+requestCode);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();

        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);

        }
    }

    private Boolean validate() {
        Boolean result = false;
        String strUniqueID = uniqueID.getText().toString();
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String shop_name = shopName.getText().toString();
        String category = String.valueOf(service_category.getSelectedItem());

        if (strUniqueID.isEmpty() || email.isEmpty() || password.isEmpty() || category.equalsIgnoreCase("Choose a Service category") || shop_name.isEmpty()) {
            Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }
        return result;
    }

//    private void sendEmailVerification(String email) {
//        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//        final String temp_email = firebaseUser.getEmail();
//        firebaseUser.updateEmail(email);
//        if (firebaseUser != null) {
//            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(StoreRegistrationActivity.this, "A Verification Link has been sent to you E-mail", Toast.LENGTH_LONG).show();
//                        firebaseAuth.signOut();
//                        finish();
//                        if (temp_email != null) {
//                            firebaseUser.updateEmail(temp_email);
//                        }
//                    } else {
//                        Toast.makeText(StoreRegistrationActivity.this, "Please Try Again Later or check if entered email is correct", Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        }
//    }
}
