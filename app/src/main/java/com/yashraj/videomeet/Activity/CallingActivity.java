package com.yashraj.videomeet.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {
    private TextView username_calling;
    private ImageView cut_call,answer_call,useriamge;
    String recieverUserimage="",recieverUserId="",receiverUsername="";
    String senderUserimage="",senderUserId="",senderUsername="";
    private String checker="";
    private String callingID="",ringingID="";
    DatabaseReference userRef;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);        username_calling=findViewById(R.id.calling_username);
        cut_call=findViewById(R.id.calling_cut);
        mediaPlayer=MediaPlayer.create(this,R.raw.ringtone);
        senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        answer_call=findViewById(R.id.calling_make_call);
        useriamge=findViewById(R.id.calling_imageView);
        userRef= FirebaseDatabase.getInstance().getReference().child("UserData");

        recieverUserId=getIntent().getExtras().get("visit_user_id").toString();

        getAndsetUserProfile();


        cut_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  checker="clicked";
                cancelCallingUser();



            }
        });

        answer_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final HashMap<String ,Object> callingpickupmap=new HashMap<>();
                callingpickupmap.put("picked","picked");
                userRef.child(senderUserId).child("Ringing").updateChildren(callingpickupmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            startActivity(new Intent(CallingActivity.this,VideoChatActivity.class));
                        }
                    }
                });
            }
        });
    }

    private void cancelCallingUser() {

        //////////////////////////////////////////////// From sender side /////////////////////////////////////////////////
        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("calling")){
                    callingID=snapshot.child("calling").getValue().toString();


                    userRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                       finish();
                                   }
                               });
                           }
                        }
                    });

                }else {
                    startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //////////////////////////////////////////////////////////////    From reciever side  ////////////////////////////////////////////////////


        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("ringing")){
                    ringingID=snapshot.child("calling").getValue().toString();

                    userRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    });

                }else {
                    startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAndsetUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(recieverUserId).exists()){
                    recieverUserimage=snapshot.child(recieverUserId).child("Profileimage").getValue().toString();
                    receiverUsername=snapshot.child(recieverUserId).child("Username").getValue().toString();
                    username_calling.setText(receiverUsername);
                    Picasso.get().load(recieverUserimage).placeholder(R.drawable.profile_image).into(useriamge);

                }if (snapshot.child(senderUserId).exists()){
                    senderUserimage=snapshot.child(senderUserId).child("Profileimage").getValue().toString();
                    senderUsername=snapshot.child(senderUserId).child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing") ){
                    final HashMap<String ,Object> callingmap=new HashMap();
                    callingmap.put("uid",senderUserId);
                    callingmap.put("name",senderUsername);
                    callingmap.put("image",senderUserimage);
                    callingmap.put("calling",recieverUserId);

                    userRef.child(senderUserId).child("Calling").updateChildren(callingmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                final HashMap<String ,Object> ringingmap=new HashMap();
                                ringingmap.put("uid",recieverUserId);
                                ringingmap.put("name",receiverUsername);
                                ringingmap.put("image",recieverUserimage);
                                ringingmap.put("ringing",senderUserId);
                                userRef.child(recieverUserId).child("Ringing").updateChildren(ringingmap);

                            }

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserId).hasChild("Ringing") && !snapshot.child(senderUserId).hasChild("Calling")){
                    answer_call.setVisibility(View.VISIBLE);
                }
                if (snapshot.child(recieverUserId).child("Ringing").hasChild("picked")){
                    startActivity(new Intent(CallingActivity.this,VideoChatActivity.class));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}