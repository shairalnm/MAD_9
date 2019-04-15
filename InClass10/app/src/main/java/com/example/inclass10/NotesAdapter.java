package com.example.inclass10;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    ArrayList<NotesData> myNotes;
    Context myContext;
    private final OkHttpClient client = new OkHttpClient();
    private String token;
    NotesInterface notesInterface;


    public NotesAdapter(ArrayList<NotesData> myNotes, Context myContext, NotesInterface notesInterface) {
        this.myNotes = myNotes;
        this.myContext = myContext;
        this.notesInterface = notesInterface;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public NotesData getItem(int position) {
        return myNotes.get(position);
    }

    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notes_recycler_view, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        token = preferences.getString("Token", "");

        FloatingActionButton btnDelete = v.findViewById(R.id.floatingActionButtonDelete);
        TextView textNote = v.findViewById(R.id.textView_note);

        final NotesData notesData = myNotes.get(i);
        textNote.setText(notesData.textNote);

        textNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_logOff = new Intent();

            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(vh.getAdapterPosition());
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder viewHolder, int i) {

        viewHolder.txtNote.setText(myNotes.get(i).textNote);

    }

    @Override
    public int getItemCount() {
        return myNotes.size();
    }

    public void delete(int pos){

        NotesData notes_dat = getItem(pos);
        Request request = new Request.Builder()
                .url("http://ec2-3-91-77-16.compute-1.amazonaws.com:3000/api/note/delete?msgId=" + notes_dat.id)
                .addHeader("x-access-token" , token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("demo", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {

                notesInterface.deleteThreads(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, myNotes.size());
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNote;
        FloatingActionButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);

            txtNote = itemView.findViewById(R.id.textView_note);
            deleteButton = itemView.findViewById(R.id.floatingActionButtonDelete);

        }
    }
}
