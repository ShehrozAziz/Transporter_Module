package com.example.transporter_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.security.identity.PersonalizationData;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ImageButton btnMore,btnLogout;
    RecyclerView rvPendings;
    public static BadgeDrawable Pending;
    private WebSocketClient webSocketClient;
    private static final String TAG = "MainActivity";
    Context context;

    List<Order> pendingOrders = new ArrayList<>();
    PendingOrdersAdapter adapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.this;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);  // Prevent canceling by tapping outside
        progressDialog.setIndeterminate(true);  // Show indeterminate spinner
        progressDialog.show();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.BG)); // Replace 'your_color' with the desired color
        setContentView(R.layout.activity_main);
        btnMore = findViewById(R.id.btnMore);
        btnLogout = findViewById( R.id.btnLogout );
        rvPendings = findViewById(R.id.rvPendings);
        rvPendings.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        adapter = new PendingOrdersAdapter(pendingOrders,MainActivity.this);
        rvPendings.setAdapter(adapter);
        btnLogout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView messageTextView = new TextView(context);
                messageTextView.setText("Do you really want to logout?");
                messageTextView.setTextSize(18); // Set the desired text size
                messageTextView.setPadding(50, 50, 50, 20); // Set padding for left and right margins
                messageTextView.setGravity( Gravity.CENTER); // Center the text

                // Set custom font
                messageTextView.setTypeface( ResourcesCompat.getFont(v.getContext(), R.font.poppins_medium));  // Apply Poppins font


                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(messageTextView);
                builder.setCancelable(true); // Allow canceling the dialog by tapping outside or pressing back

                // Set Positive Button
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Handle logout action here
                    SharedPrefsManager manager = new SharedPrefsManager( context );
                    manager.clearTransporterData();
                    SignIn.locationfetched=false;

                    dialog.dismiss();
                    Intent intent = new Intent(MainActivity.this,SignIn.class);
                    startActivity( intent );
                    finish();
                });

                // Set Negative Button
                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } );
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileDialog();
            }
        });
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Disable swipe if the item is already opened
                if (viewHolder.getAdapterPosition() == adapter.swipedPosition) {
                    return 0; // No swipe direction, disabling swipe
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Prevent full swipe, reveal the buttons without removing the item

                adapter.showButtons(viewHolder.getAdapterPosition());
                adapter.notifyItemChanged(viewHolder.getAdapterPosition()); // Reset the swipe state
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                //adapter.hideButtons(viewHolder.getAdapterPosition());
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                // Only allow partial swipe to reveal buttons (e.g., 0.3f means 30% of the item width)
                return 0.3f;
            }
            /*
            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return super.getSwipeEscapeVelocity(defaultValue) * 2; // Slow down the swipe speed
            }

            @Override
            public float getSwipeVelocityThreshold(float defaultValue) {
                return super.getSwipeVelocityThreshold(defaultValue) * 2; // Slow down the swipe release velocity
            }*/
        };

        connectWebSocket();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvPendings);
        // Attach swipe actions

    }
    private void showProfileDialog() {
        // Inflate the layout_profile.xml
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_profile, null);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        // Make the dialog background transparent
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get all views by their IDs
        TextView tvName = dialogView.findViewById(R.id.tvName);
        tvName.setText(SignIn.transporter.getName());
        TextView tvCarLocation = dialogView.findViewById(R.id.tvCarLocation);
        tvCarLocation.setText("Pick Up, Lahore");
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        tvPhone.setText(SignIn.transporter.getPhone());
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        etPhone.setText(SignIn.transporter.getPhone());
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);

        // Show the dialog
        alertDialog.show();
        final View rootView = findViewById(android.R.id.content).getRootView();

        // Add an OnGlobalLayoutListener to detect keyboard visibility changes
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // If the visible height is much smaller than the total screen height, keyboard is open
                //boolean keyboardVisible = screenHeight - visibleHeight > screenHeight * 0.2;

                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is open, hide the elements
                    tvName.setVisibility(View.GONE);
                    tvCarLocation.setVisibility(View.GONE);
                    tvPhone.setVisibility(View.GONE);
                } else {
                    // Keyboard is closed, show the elements
                    tvName.setVisibility(View.VISIBLE);
                    tvCarLocation.setVisibility(View.VISIBLE);
                    tvPhone.setVisibility(View.VISIBLE);
                }
            }
        });

        // Optional: Adjust the dialog window's soft input mode
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    private void connectWebSocket() {
        try {
            String baseAddress = getString(R.string.server_IP);

            URI uri = new URI("ws:" + baseAddress); // Replace with your server's WebSocket URL
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket Opened");
                    runOnUiThread(() -> Toast.makeText(context, "Connected to Server", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);

                    try {
                        // Convert the message into a JSON object
                        JSONObject jsonMessage = new JSONObject(message);

                        // Check the event type
                        String eventType = jsonMessage.getString("event");

                        if ("orderCancelled".equals(eventType)) {
                            // Handle order cancellation event
                            String orderID = jsonMessage.getString("orderID");
                            String cancelMessage = jsonMessage.getString("message");

                            // Show a toast with the cancellation message
                            runOnUiThread(() ->
                                    adapter.removeorder(orderID)
                                    //Toast.makeText(getApplicationContext(), "Order Cancelled: " + cancelMessage + " (ID: " + orderID + ")", Toast.LENGTH_SHORT).show()
                            );
                        } else if ("allOrders".equals(eventType)) {
                            // Handle all orders event
                            String dataJson = jsonMessage.getJSONArray( "data" ).toString();

                            // Parse the order data
                            List<Order> orders = OrderParser.parseOrderData( dataJson );

                            for (Order order : orders) {
                                Log.d( TAG, "Order by: " + order.getName() + ", ID: " + order.getorderID() );
                            }

                            // Update the UI with the new order data
                            runOnUiThread( () -> {
                                pendingOrders.clear();
                                pendingOrders.addAll( orders );
                                adapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                            } );
                        } else if ("newOrder".equals(eventType))
                        {
                            String dataJson = jsonMessage.getJSONArray( "data" ).toString();
                            // Parse the order data
                            List<Order> orders = OrderParser.parseOrderData( dataJson );
                            runOnUiThread( () ->
                            {
                                adapter.addOrder(orders.get(0));
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON message: " + e.getMessage());
                    }
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket Closed: " + reason);
                    runOnUiThread(() -> Toast.makeText(context, "Server Closed: " + reason, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error: " + ex.getMessage());
                    runOnUiThread(() -> Toast.makeText(context, "Server Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Error connecting WebSocket: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(context, "Error Connecting Server: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    public void SignInrequest(String phone, String password, LoginCallBack callback)
    {
        final int[] returnvariable = {0};
        String baseAddress = getString(R.string.server_IP);

        // Create the ClientData object
        LoginData clientData = new LoginData(phone, password);

        // Retrofit setupw
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http:" + baseAddress)
                .addConverterFactory( GsonConverterFactory.create())
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
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                callback.onLoginResult( -3,null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }





}
