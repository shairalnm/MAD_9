package com.example.inclass10;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class UserSignUp extends AppCompatActivity {

    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private EditText confirmPasswordEdit;
    private Button cancelButton;
    private Button signUpButton;
    private final OkHttpClient client = new OkHttpClient();
    private User user;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Sign Up");

        firstNameEdit = findViewById(R.id.editText_FirstName);
        lastNameEdit = findViewById(R.id.editText_LastName);
        emailEdit = findViewById(R.id.editText_Email);
        passwordEdit = findViewById(R.id.editText_Password);
        confirmPasswordEdit = findViewById(R.id.editText_CnfPassword);
        cancelButton = findViewById(R.id.button_Cancel);
        signUpButton = findViewById(R.id.button_SignUp);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnected()) {

                    if (firstNameEdit.getText() == null || firstNameEdit.getText().toString().equalsIgnoreCase("")) {

                        firstNameEdit.setError("Enter First Name");
                    } else if (lastNameEdit.getText() == null || lastNameEdit.getText().toString().equalsIgnoreCase("")) {

                        lastNameEdit.setError("Enter Last Name");
                    } else if (emailEdit.getText() == null || emailEdit.getText().toString().equalsIgnoreCase("")) {

                        emailEdit.setError("Enter Email Id");
                    } else if (passwordEdit.getText() == null || passwordEdit.getText().toString().equalsIgnoreCase("")) {

                        passwordEdit.setError("Enter Password");
                    } else if (confirmPasswordEdit.getText() == null || confirmPasswordEdit.getText().toString().equalsIgnoreCase("")) {

                        confirmPasswordEdit.setError("Enter Confirm Password");
                    } else if (passwordEdit.getText() != null
                            && !passwordEdit.getText().toString().equalsIgnoreCase("")
                            && confirmPasswordEdit.getText() != null
                            && !confirmPasswordEdit.getText().toString().equalsIgnoreCase("")
                            && !passwordEdit.getText().toString().equalsIgnoreCase(confirmPasswordEdit.getText().toString())) {

                        confirmPasswordEdit.setError("Password and Confirm Password do not match");
                    } else if (passwordEdit.getText().length() < 6 || confirmPasswordEdit.getText().length() < 6) {

                        passwordEdit.setError("Password has to be 6 or more than 6 characters");
                    } else {

                        user = new User(firstNameEdit.getText().toString(), lastNameEdit.getText().toString(), emailEdit.getText().toString(), passwordEdit.getText().toString());
                        Log.d("User", user.toString());
                        signUp();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void signUp() {

        if (user != null) {
            RequestBody formBody = new FormBody.Builder()
                    .add("email", user.email)
                    .add("password", user.password)
                    .add("fname", user.firstName)
                    .add("lname", user.lastName)
                    .build();

            Request request = new Request.Builder()
                    .url("http://ec2-3-91-77-16.compute-1.amazonaws.com:3000/api/auth/register")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("demo", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try (ResponseBody responseBody = response.body()) {

                        if (!response.isSuccessful()) {
                            Looper.prepare();
                            Toast.makeText(UserSignUp.this, "User email already exists", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            throw new IOException("Unexpected code " + response);
                        }

                        JSONObject root = new JSONObject(response.body().string());

                        Log.d("demo", root.toString());
                        String status = root.getString("auth");

                        if (status.equalsIgnoreCase("true")) {

                            token = root.getString("token");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("Token", token);
                                    editor.apply();
                                    Toast.makeText(UserSignUp.this, "User Created Successfully", Toast.LENGTH_LONG).show();
                                }
                            });

                            Intent int_login = new Intent(UserSignUp.this, Notes.class);
                            Bundle bnd = new Bundle();
                            bnd.putSerializable(MainActivity.user_key, user);

                            int_login.putExtras(bnd);
                            startActivity(int_login);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
