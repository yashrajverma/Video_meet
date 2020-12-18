package com.yashraj.videomeet.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yashraj.videomeet.R;

import Model.Contacts;
import android.app.*;
public class NotificationActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = NotificationActivity.class.getName();
    RecyclerView recyclerViewNotification;
    DatabaseReference friendrequestRef,contactsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    TextView notitext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        recyclerViewNotification=findViewById(R.id.myrecyclerView_notification);
        recyclerViewNotification.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewNotification.setHasFixedSize(true);
        friendrequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contact");
        userRef=FirebaseDatabase.getInstance().getReference().child("UserData");
        notitext=findViewById(R.id.user_notification_username);


// Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.rectangle_1_9)
                .setContentTitle("New Friend request")
                .setContentText("Accept or Reject the request!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        ImageView userimage;
        Button acceptbtn,cancelbtn;
        RelativeLayout cardView;


        public NotificationViewHolder(View itemView) {
            super(itemView);
            userimage=itemView.findViewById(R.id.find_user_notification_imageView);
            username=itemView.findViewById(R.id.find_user_notification_username);
            acceptbtn=itemView.findViewById(R.id.accept_btn_notification);
            cancelbtn=itemView.findViewById(R.id.cancel_btn_notification);
            cardView=itemView.findViewById(R.id.cardView_relative_layout);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(friendrequestRef.child(currentUserID) ,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,NotificationViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int i, @NonNull Contacts contacts) {
                holder.acceptbtn.setVisibility(View.VISIBLE);
                holder.cancelbtn.setVisibility(View.VISIBLE);

               final String listuserId=getRef(i).getKey();
                DatabaseReference requesttypeRef=getRef(i).child("request_type").getRef();
                requesttypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String type=snapshot.getValue().toString();
                            if(type.equals("received"))
                            {
                                holder.cardView.setVisibility(View.VISIBLE);
                                userRef.child(listuserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("Profileimage")){
                                            final String imagestr=snapshot.child("Profileimage").getValue().toString();
                                            Picasso.get().load(imagestr).into(holder.userimage);

                                        }
                                        if(snapshot.hasChild("Username")){
                                            final String namestr=snapshot.child("Username").getValue().toString();
                                            holder.username.setText(namestr);
                                            notitext.setVisibility(View.GONE);
                                        }

                                        holder.acceptbtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                contactsRef.child(currentUserID).child(listuserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            contactsRef.child(listuserId).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){


                                                                        friendrequestRef.child(currentUserID).child(listuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    friendrequestRef.child(listuserId).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                Toast.makeText(NotificationActivity.this, "Request Accepted!", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });




                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        holder.cancelbtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                friendrequestRef.child(currentUserID).child(listuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            friendrequestRef.child(listuserId).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(NotificationActivity.this, "Request Denied!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }else{
                                holder.cardView.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design,parent,false);
                return new NotificationViewHolder(view);
            }
        };
        recyclerViewNotification.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


}