package com.example.transporter_module;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.BG)); // Replace 'your_color' with the desired color

        btnSignIn = findViewById(R.id.btnSignIn);
        context = SignIn.this;
        etPasswordSignin = findViewById(R.id.etPasswordSignin);
        etUsernameSignin = findViewById(R.id.etUsernameSignin);
        ivTransport_Clip = findViewById(R.id.ivTransport_Clip);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(SignIn.this, MainActivity.class);
                //startActivity(intent);
                //finish();
                //new SendRequestTask().execute("http://192.168.1.5:5000/sendToJavaApp");
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://192.168.1.5:5000/") // Replace with your Node.js server's IP
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                apiService = retrofit.create(ApiService.class);

                // Fetch the message from the Node.js server
                ClientData clientData = new ClientData("Shehroz", 21);

                // Send the data to the server
                sendDataToServer(clientData);
            }
        });
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
    private void sendDataToServer(ClientData clientData) {
        Call<ServerResponse> call = apiService.sendData(clientData);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
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
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(context, "Response Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getMessageFromServer() {
        Call<ServerResponse> call = apiService.getMessage();

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "Response :"+ response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle the response from the server
                    Log.d("Server Response", "Message: " + response.body().getMessage());
                } else {
                    Log.e("Server Response", "Request failed");
                    Toast.makeText(context, "Response Denied", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(context, "Response Failed", Toast.LENGTH_SHORT).show();
                Log.e("Server Response", "Request error: " + t.getMessage());
            }
        });
    }
    private class SendRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder responseBuilder = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        responseBuilder.append(inputLine);
                    }
                    in.close();
                    response = responseBuilder.toString();
                } else {
                    Log.e(TAG, "Request failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in request: " + e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Response: " + result);
            // Display a Toast message on the main thread
            if (result != null && !result.isEmpty()) {
                Toast.makeText(context, "Response: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "No response from server", Toast.LENGTH_LONG).show();
            }
        }
    }


}