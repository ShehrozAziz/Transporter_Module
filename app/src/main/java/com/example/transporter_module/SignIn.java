package com.example.transporter_module;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SignIn extends AppCompatActivity {
    MaterialButton btnSignIn;
    ImageView ivTransport_Clip;
    TextInputEditText etUsernameSignin,etPasswordSignin;
    private ApiService apiService;

    public Context context;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    static public boolean locationfetched;

    static public Transporter transporter;

    static public boolean LoggedIn;

    static public double longitude;
    static public double latitude;

    static public String baseURL;

    ProgressDialog progressDialog;

    SharedPrefsManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        baseURL = getString(R.string.server_IP);


        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.BG)); // Replace 'your_color' with the desired color
        btnSignIn = findViewById(R.id.btnSignIn);
        context = SignIn.this;
        manager = new SharedPrefsManager(context);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);  // Prevent canceling by tapping outside
        progressDialog.setIndeterminate(true);  // Show indeterminate spinner
        progressDialog.show();
        etPasswordSignin = findViewById(R.id.etPasswordSignin);
        etUsernameSignin = findViewById(R.id.etUsernameSignin);
        ivTransport_Clip = findViewById(R.id.ivTransport_Clip);
        transporter = new Transporter();


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    SignInrequest(etUsernameSignin.getText().toString(), etPasswordSignin.getText().toString(), new LoginCallBack() {
                        @Override
                        public void onLoginResult(int result, LoginResponse loginResponse) {
                            switch (result) {
                                case 1:
                                    // Successful login
                                    transporter = loginResponse.getTransporter();
                                    Log.d("Transporter", "Transporter Name: " + transporter.getName());
                                    manager.clearTransporterData();
                                    manager.saveTransporter(transporter,etUsernameSignin.getText().toString(),etPasswordSignin.getText().toString());
                                    Log.d("Sign In", transporter.getId());
                                    Intent intent = new Intent(SignIn.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                    break;
                                case -1:
                                    // Login failed (incorrect credentials)
                                    Toast.makeText(context, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    break;
                                case -2:
                                    // Response failure
                                    Toast.makeText(context, "Login failed due to response error", Toast.LENGTH_SHORT).show();
                                    break;
                                case -3:
                                    // Network error
                                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    // Unknown error
                                    Toast.makeText(context, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
            }
        });

        // Location Manager Initialization
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if(!locationfetched)
                {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Toast.makeText( context,"Location Fetched",Toast.LENGTH_LONG ).show();
                    Log.d( "100",String.valueOf(latitude) + " " + String.valueOf(longitude));
                    locationfetched = true;
                    progressDialog.dismiss();
                    if(manager.getUsername() != null)
                    {
                        SignIn.transporter = manager.getTransporter();
                        Intent intent = new Intent(SignIn.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };
        // Request Location Permissions
        checkLocationPermissions();


        final View rootView = findViewById(android.R.id.content).getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    ivTransport_Clip.setVisibility(View.GONE);

                } else {
                    ivTransport_Clip.setVisibility(View.VISIBLE);

                }
            }
        });
    }
   private void sendDataToServer(LoginData loginData) {
        Call<LoginResponse> call = apiService.sendData( loginData );

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle the response from the Node.js server
                    Log.d("Server Response", "Message: " + response.body().getMessage());
                    Toast.makeText(context, "Response" +response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Server Response", "Request failed");
                    Toast.makeText(context, "Request Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(context, "Response Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void SignInrequest(String phone, String password, LoginCallBack callback)
    {
        final int[] returnvariable = {0};
        String baseAddress = getString(R.string.server_IP);

        // Create the ClientData object
        LoginData clientData = new LoginData(phone, password);

        // Retrofit setupw
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http:" + baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Call the login method
        Call<LoginResponse> call = apiService.login(clientData);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        // Handle success
                        if (serverResponse.isSuccess()) {
                            // Successful login, do something (e.g., navigate to another activity)
                            Log.d("API Response", "Login successful");
                            transporter = serverResponse.getTransporter();
                            Log.d("Transporter", "Transporter Name: " + transporter.getId());
                            callback.onLoginResult(1,response.body());
                            //Toast.makeText(SignIn.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            // Login failed
                            Log.e("API Response", "Login failed: " + serverResponse.getMessage());
                            callback.onLoginResult( -1,response.body() );
                            //Toast.makeText(SignIn.this, serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Log.e("API Error", "Response failed: " + response.message());
                    //Toast.makeText(SignIn.this, "Login failed", Toast.LENGTH_SHORT).show();
                    callback.onLoginResult( -2,response.body() );
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("API Error", "Request failed: " + t.getMessage());
                Toast.makeText(SignIn.this, "Network error", Toast.LENGTH_SHORT).show();
                callback.onLoginResult( -3,null);
            }
        });
    }

    // Handle the permission request result
    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request foreground location permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);  // Stop receiving updates when the app is paused
        }
    }

}