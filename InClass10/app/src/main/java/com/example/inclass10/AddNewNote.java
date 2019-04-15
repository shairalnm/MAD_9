package com.example.inclass10;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AddNewNote extends AppCompatActivity {

    private Button btnAddNode, btnCancel;
    private EditText noteTxt;
    String token;
    NotesData notes;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnAddNode = findViewById(R.id.button_AddNote1);
        btnCancel = findViewById(R.id.buttonCancel);
        noteTxt = findViewById(R.id.editText_AddNote);
        setTitle("Add New Note");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        token = preferences.getString("Token", "");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (noteTxt.getText().toString().trim().length() == 0) {

                    Toast.makeText(AddNewNote.this, "Enter Note", Toast.LENGTH_SHORT).show();
                }  else {
                    notes = new NotesData();

                    RequestBody formBody = new FormBody.Builder()
                            .add("text", noteTxt.getText().toString())
                            .build();
                    Request request = new Request.Builder()
                            .addHeader("x-access-token" , token)
                            .url("http://ec2-3-91-77-16.compute-1.amazonaws.com:3000/api/note/post")
                            .post(formBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            try (ResponseBody responseBody = response.body()) {

                                if (!response.isSuccessful())
                                    throw new IOException("Unexpected code " + response);

                                JSONObject root = new JSONObject(response.body().string());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        noteTxt.setText("");
                                        startActivity(new Intent(AddNewNote.this, Notes.class));
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    });
                }
            }
        });
    }
}
