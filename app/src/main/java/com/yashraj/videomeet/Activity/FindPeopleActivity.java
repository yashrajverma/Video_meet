package com.yashraj.videomeet.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.yashraj.videomeet.R;

import Model.Contacts;

public class FindPeopleActivity extends AppCompatActivity {
    EditText searchET;
    private String str="";
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("UserData");
        searchET=findViewById(R.id.searchET);
        recyclerView=findViewById(R.id.find_people_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchET.getText().toString().equals("")){
                    Toast.makeText(FindPeopleActivity.this, "Write username to Search", Toast.LENGTH_SHORT).show();
                }
                else{
                    str=s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =null;
        if(str.equals("")){
            options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(databaseReference,Contacts.class).build();
         }
        else{
            options=new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(databaseReference
                            .orderByChild("Username")
                            .startAt(str)
                            .endAt(str+"\uf8ff"),Contacts.class)
                    .build();
        }
       firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, final int i, @NonNull final Contacts contacts) {
                findFriendsViewHolder.username.setText(contacts.getUsername());
                Picasso.get().load(contacts.getProfileimage()).into(findFriendsViewHolder.userimage);


                findFriendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef(i).getKey();
                        Intent sendActivityIntent=new Intent(FindPeopleActivity.this,ProfileActivity.class);
                        sendActivityIntent.putExtra("visit_user_id",visit_user_id);
                        sendActivityIntent.putExtra("username",contacts.getUsername());
                        sendActivityIntent.putExtra("profile_image",contacts.getProfileimage());
                        sendActivityIntent.putExtra("status",contacts.getStatus());
                        startActivity(sendActivityIntent);

                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_layout, parent ,false);
                return new FindFriendsViewHolder(view);

            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        ImageView userimage;
        Button acceptbtn;
        RelativeLayout cardView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            userimage=itemView.findViewById(R.id.find_user_contact_imageView);
            username=itemView.findViewById(R.id.find_user_contact_username);
            acceptbtn=itemView.findViewById(R.id.videocall_btn_contact);
            cardView=itemView.findViewById(R.id.cardView_contact_relative_layout);

            acceptbtn.setVisibility(View.GONE);


        }
    }

}