package com.yashraj.videomeet.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.Condition;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yashraj.videomeet.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import Model.Contacts;

public class ContextActivity extends AppCompatActivity {
    BottomNavigationView navView;
    private RecyclerView myrecyclerView;
    private ImageView findpeopleview;
    DatabaseReference contactsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserID,usernamestr="",useriamgestr="";
    String calledBy="";
    Button dialoglogoutbutton,dialognobutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        mAuth = FirebaseAuth.getInstance();
        myrecyclerView = findViewById(R.id.myrecyclerView_context);
        findpeopleview = findViewById(R.id.find_people_btn);
        myrecyclerView.setHasFixedSize(true);
        userRef=FirebaseDatabase.getInstance().getReference().child("UserData");
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contact");
        myrecyclerView.setLayoutManager(new LinearLayoutManager(this));


        findpeopleview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContextActivity.this, FindPeopleActivity.class));
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent=new Intent(ContextActivity.this, ContextActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                case R.id.navigation_logout:
                    signoutuser();
                    break;
                case R.id.navigation_settings:
                    Intent intent1=new Intent(ContextActivity.this, SettingActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                    break;
                case R.id.navigation_notifications:
                    Intent intent2=new Intent(ContextActivity.this, NotificationActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent2);
                    break;
            }
            return true;
        }
    };

    private void signoutuser(){
        View layout = LayoutInflater.from(this).inflate(R.layout.logout_dialog, null);
        dialoglogoutbutton=(Button) layout.findViewById(R.id.logoutButtonDialog);
        dialognobutton=(Button) layout.findViewById(R.id.nobutton);
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(ContextActivity.this);
        alertDialogBuilder.setView(layout);
        final AlertDialog materialDialogs=alertDialogBuilder.create();
        materialDialogs.show();
        dialognobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialogs.dismiss();
            }
        });
        dialoglogoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ContextActivity.this, RegistrationActivity.class));
                finish();
            }
        });

    }
    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView userimage;
        Button callbtn;


        public ContactsViewHolder(View itemView) {
            super(itemView);
            userimage = itemView.findViewById(R.id.find_user_contact_imageView);
            username = itemView.findViewById(R.id.find_user_contact_username);
            callbtn = itemView.findViewById(R.id.videocall_btn_contact);

        }
    }

    @Override
    protected void onStart() {

        validateUser();
        checkForReceivingcall();
        super.onStart();
       FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsRef.child(currentUserID),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {

                final String listuserId=getRef(i).getKey();
                userRef.child(listuserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            usernamestr=snapshot.child("Username").getValue().toString();
                            useriamgestr=snapshot.child("Profileimage").getValue().toString();
                            contactsViewHolder.username.setText(usernamestr);
                            Picasso.get().load(useriamgestr).into(contactsViewHolder.userimage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                contactsViewHolder.callbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callingintent =new Intent(ContextActivity.this,CallingActivity.class);
                        callingintent.putExtra("visit_user_id",listuserId);
                        startActivity(callingintent);


                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_layout,parent,false);
                return new ContactsViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        myrecyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    private void validateUser() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        reference.child("UserData").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    startActivity(new Intent(ContextActivity.this,SettingActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void checkForReceivingcall() {
        userRef.child(currentUserID).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ringing")){
                    calledBy=snapshot.child("ringing").getValue().toString();

                    Intent callingintent =new Intent(ContextActivity.this,CallingActivity.class);
                    callingintent.putExtra("visit_user_id",calledBy);
                    startActivity(callingintent);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}