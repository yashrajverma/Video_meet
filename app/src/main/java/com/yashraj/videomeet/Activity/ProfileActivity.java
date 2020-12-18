package com.yashraj.videomeet.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserId="",recieverUsername="",recieverUserImage="",recieverstaus="";
    private ImageView profileimage;
    TextView profileusername,profilestatus;
    Button addfriend,denyfriend;
    private String currentstate="new";
    private FirebaseAuth mAuth;
    String senderUserID;
    DatabaseReference friendrequestRef,contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        addfriend=findViewById(R.id.addfriend_btn_);
        profileimage=findViewById(R.id.profile_user_contact_imageView);
        profilestatus=findViewById(R.id.profile_user_contact_status);
        profileusername=findViewById(R.id.profile_user_contact_username);
        denyfriend=findViewById(R.id.denyfriend_btn_);
        mAuth=FirebaseAuth.getInstance();
        senderUserID=mAuth.getCurrentUser().getUid();
        friendrequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contact");

        recieverUserId=getIntent().getExtras().get("visit_user_id").toString();
        recieverUsername=getIntent().getExtras().get("username").toString();
        recieverUserImage=getIntent().getExtras().get("profile_image").toString();
        recieverstaus=getIntent().getExtras().get("status").toString();

        profileusername.setText(recieverUsername);
        profilestatus.setText(recieverstaus);
        Picasso.get().load(recieverUserImage).into(profileimage);

        manageClickEvents();
    }

    private void manageClickEvents() {

        friendrequestRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(recieverUserId)){
                    String requestType=snapshot.child(recieverUserId).child("request_type").getValue().toString();
                    if (requestType.equals("sent")){
                        currentstate="request_sent";
                        addfriend.setText("Cancel Friend Request");
                    }
                    else if(requestType.equals("received")){
                        currentstate="request_received";
                        addfriend.setText("Accept Friend Request");
                        denyfriend.setVisibility(View.VISIBLE);
                        denyfriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                }
                else{
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(recieverUserId)){
                                currentstate="friends";
                                addfriend.setText("Delete Contact");
                            }else{
                                currentstate="new";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        if(senderUserID.equals(recieverUserId)){
            addfriend.setVisibility(View.GONE);
        }
        else {
            addfriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentstate.equals("new")){
                        sendFriendRequest();
                    }
                    if(currentstate.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(currentstate.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if(currentstate.equals("request_sent")){
                        cancelFriendRequest();
                    }
                }
            });
        }

    }
    private void sendFriendRequest() {
        friendrequestRef.child(senderUserID)
                .child(recieverUserId)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendrequestRef
                                    .child(recieverUserId)
                                    .child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        currentstate="request_sent";
                                        addfriend.setText("Cancel Request");
                                        Toast.makeText(ProfileActivity.this, "Friend Request Sent!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
    }


    private void acceptFriendRequest() {
        contactsRef.child(senderUserID).child(recieverUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactsRef.child(recieverUserId).child(senderUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                friendrequestRef.child(senderUserID).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            friendrequestRef.child(recieverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        currentstate="friends";
                                                        addfriend.setText("Delete Contact");
                                                        denyfriend.setVisibility(View.GONE);
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


    private void cancelFriendRequest() {
        friendrequestRef.child(senderUserID).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendrequestRef.child(recieverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                currentstate="new";
                                addfriend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }



}