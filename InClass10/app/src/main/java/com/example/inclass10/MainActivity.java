package com.example.inclass10;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnSignUp;
    private OkHttpClient client;
    private static String token;
    public static String user_key = "User";
    private EditText email;
    private EditText password;
    private User user;
    public static AlertDialog.Builder builder;
    public static AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Login");

        email = findViewById(R.id.editText_Email);
        password = findViewById(R.id.editText_Password);
        password.setTransformationMethod(new PasswordTransformationMethod());

        btnLogin = findViewById(R.id.button_Login);
        btnSignUp = findViewById(R.id.btn_SignUp);


        client = new OkHttpClient();

        builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle("Loading").setView(inflater.inflate(R.layout.dialog_bar, null));
        dialog = builder.create();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent int_signup = new Intent(MainActivity.this, UserSignUp.class);
                startActivity(int_signup);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnected()) {

                    if (email.getText().toString().trim().length() == 0) {
                        Toast.makeText(MainActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    } else if (password.getText().toString().trim().length() == 0) {
                        Toast.makeText(MainActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    } else {
                        user = new User(email.getText().toString().trim(), password.getText().toString().trim());
                        Log.d("demo", user.toString());
                        dialog.show();
                        login();
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void login() {

        RequestBody formBody = new FormBody.Builder()
                .add("email", user.email)
                .add("password", user.password)
                .build();

        Request request = new Request.Builder()
                .url("http://ec2-3-91-77-16.compute-1.amazonaws.com:3000/api/auth/login")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("demo", "FAILURE " + e.getMessage());
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d("demo", response.toString());

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                                Toast.makeText(MainActivity.this, "User do not exist", Toast.LENGTH_LONG).show();
                            }
                        });

                        throw new IOException("Unexpected code " + response);
                    }

                    JSONObject root = new JSONObject(response.body().string());
                    Log.d("demo", root.toString());

                    String status = root.getString("auth");
                    token = root.getString("token");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("Token", token);
                            editor.apply();
                        }
                    });

                    if (status.equals("true")) {

                        Intent int_login = new Intent(MainActivity.this, Notes.class);
                        Bundle bnd = new Bundle();
                        bnd.putSerializable(MainActivity.user_key, user);
                        int_login.putExtras(bnd);
                        startActivity(int_login);

                    } else {

                        Toast.makeText(MainActivity.this, "User do not exist", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }

        return true;
    }
}
