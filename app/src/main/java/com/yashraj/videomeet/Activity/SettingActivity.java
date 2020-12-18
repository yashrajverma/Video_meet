package com.yashraj.videomeet.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yashraj.videomeet.R;

import java.util.HashMap;
import java.util.Map;
import android.app.ProgressDialog;
public class SettingActivity extends AppCompatActivity {
    private Button savebutton;
    private EditText usernameET,statusET;
    private ImageView profileiamge;
    private StorageReference storageReference;
    FirebaseAuth mAuth;
    public static final int GALLERYCODE=1;
    public String downloadUrl;
    Uri imageUri;
    private DatabaseReference reference;
    ProgressDialog progressDialog;
    TextView userProfile;
    ImageView userProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        savebutton=findViewById(R.id.settings_save_btn);
        usernameET=findViewById(R.id.settings_username);
        statusET=findViewById(R.id.settings_status);
        profileiamge=findViewById(R.id.settings_profile_image);
        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("ProfileImages");
        reference= FirebaseDatabase.getInstance().getReference().child("UserData");
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        userProfile=findViewById(R.id.userProfile_on_context);
//        userProfileImageView=findViewById(R.id.user_profile_image_on_context);

        profileiamge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERYCODE);
            }
        });

        retrieveData();
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imageUri==null){
                    Toast.makeText(getApplicationContext(), "Select Image", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else{
                    progressDialog.dismiss();
                    saveDataOfUser();
                }
            }
        });

    }

    private void saveDataOfUser() {
        progressDialog.setTitle("Account Setting");
        progressDialog.setMessage("Updating account...");
        progressDialog.show();

        final String getUsername=usernameET.getText().toString();
        final String getUserstatus=statusET.getText().toString();


        if(TextUtils.isEmpty(getUsername) && TextUtils.isEmpty(getUserstatus)){
            Toast.makeText(this, "Provide Username and Status.", Toast.LENGTH_SHORT).show();progressDialog.dismiss();progressDialog.dismiss();
            progressDialog.dismiss();
        }
        else{
            final StorageReference filePath=storageReference.child(mAuth.getCurrentUser().getUid());
            final UploadTask uploadTask=filePath.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    downloadUrl=filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        downloadUrl=task.getResult().toString();
                        HashMap<String,Object> MapData=new HashMap<>();
                        MapData.put("uid",mAuth.getCurrentUser().getUid());
                        MapData.put("Username",getUsername);
                        MapData.put("Status",getUserstatus);
                        MapData.put("Profileimage",downloadUrl);

                        reference.child(mAuth.getCurrentUser().getUid()).updateChildren(MapData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    startActivity(new Intent(SettingActivity.this,ContextActivity.class));
                                    finish();
                                    Toast.makeText(SettingActivity.this, "Changes Saved!", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    progressDialog.dismiss();
                                    String error=task.getException().getMessage();
                                    Toast.makeText(SettingActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    }
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==GALLERYCODE && data!=null){
            imageUri=data.getData();
            profileiamge.setImageURI(imageUri);
        }

    }

    private void retrieveData(){

        reference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    String imageDb = snapshot.child("Profileimage").getValue().toString();
                    String usernameDb = snapshot.child("Username").getValue().toString();
                    String statususerDb = snapshot.child("Status").getValue().toString();

                    usernameET.setText(usernameDb);
                    statusET.setText(statususerDb);
//                    userProfile.setText(usernameDb); //TODO
                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(profileiamge);
//                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(userProfileImageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}