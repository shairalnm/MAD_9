package com.example.inclass10;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
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

public class Notes extends AppCompatActivity implements NotesInterface {

    String token;
    private final OkHttpClient client = new OkHttpClient();
    private User user;
    LinearLayoutManager myLayout;
    private TextView userName;
    ArrayList<NotesData> result = null;
    private RecyclerView recyclerView;
    NotesAdapter notesAdapter;
    private Button btnAddNewNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Notes");
        userName = findViewById(R.id.textView_UserWelcome);
        btnAddNewNote = findViewById(R.id.button_AddNote);

        MainActivity.dialog.hide();

        /*if (getIntent() != null && getIntent().getExtras() != null)
        {
            user = (User) getIntent().getExtras().getSerializable(MainActivity.user_key);

            String user_name = "Hey " + user.firstName + "!!!";
            userName.setText(user_name);
            }*/
        if (getIntent() != null && getIntent().getExtras() != null) {
            user = (User) getIntent().getExtras().getSerializable(MainActivity.user_key);
            Log.d("demo",user.toString());
            userName.setText("Hey");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        token = preferences.getString("Token", "");

        if (!token.equalsIgnoreCase(""))
            //fetch notes
            getNotes();

        else {

            finish();
            Intent intent_logOff = new Intent(Notes.this, MainActivity.class);
            startActivity(intent_logOff);
        }
        //}



        findViewById(R.id.floatingActionButton_Logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                token="";
                MainActivity.dialog.show();
                finish();
                startActivity(new Intent(Notes.this, MainActivity.class));
            }
        });

        btnAddNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addNote = new Intent(Notes.this, AddNewNote.class);
                startActivity(addNote);
            }
        });


    }

    public void getNotes(){

        Request request = new Request.Builder()
                .url("http://ec2-3-91-77-16.compute-1.amazonaws.com:3000/api/note/getall")
                .addHeader("x-access-token" , token)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("demo", "Notes " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    JSONObject root = new JSONObject(responseBody.string());

                    result = new ArrayList<>();

                    JSONArray source = root.getJSONArray("notes");

                    for (int i = 0; i < source.length(); i++) {
                        JSONObject sourceJSON = source.getJSONObject(i);

                        NotesData notesData = new NotesData();

                        notesData.userId = sourceJSON.getString("userId");
                        notesData.id = sourceJSON.getString("_id");
                        notesData.textNote = sourceJSON.getString("text");

                        result.add(notesData);

                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            setAdapter(result);
                            Log.d("demo", result.toString());
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void setAdapter(ArrayList<NotesData> s) {
        MainActivity.dialog.hide();

        recyclerView = findViewById(R.id.recyclerView_Notes);
        myLayout = new LinearLayoutManager(this);

        myLayout.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(myLayout);

        notesAdapter = new NotesAdapter(s, this, this);
        recyclerView.setAdapter(notesAdapter);
    }

    @Override
    public void deleteThreads(int id) {
        result.remove(id);
        getNotes();
    }

}
