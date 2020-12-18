package com.yashraj.videomeet.Activity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.v3.OpentokException;
import com.yashraj.videomeet.R;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static final String API_KEY="46843404";
    private static final String SESSION_ID="2_MX40Njg0MzQwNH5-MTYwODI4MDUwMDg4Mn5zbUptUmlWSk16ZTRERmJ5K2dHMGRyU25-fg";
    private static final String TOKEN="";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_PERMISSIONS=124;
    private ImageView cancel_call_btn;
    private DatabaseReference userRef;
    String userId="";
    private FrameLayout msubscriberlayout;
    private FrameLayout mpublisherlayout;
    private Session msession;
    private com.opentok.android.Publisher mpushlier;
    private Subscriber mSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        userRef= FirebaseDatabase.getInstance().getReference().child("UserData");
        cancel_call_btn=findViewById(R.id.cancel_videocall_btn);


        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        cancel_call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(userId).hasChild("Ringing")){
                            userRef.child(userId).child("Ringing").removeValue();
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();

                            if (mpushlier!=null){
                                mpushlier.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                        }
                        if(snapshot.child(userId).hasChild("Calling")){
                            userRef.child(userId).child("Calling").removeValue();
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                            if (mpushlier!=null){
                                mpushlier.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                        }
                        else {
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                            if (mpushlier!=null){
                                mpushlier.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        try {
            requestPermission();
        } catch (OpentokException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_PERMISSIONS)
    private void requestPermission() throws OpentokException {
        String[] permission={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this,permission)){
            msubscriberlayout=findViewById(R.id.subscriber_window);
            mpublisherlayout=findViewById(R.id.publisher_window);
            msession=new Session.Builder(this,API_KEY,SESSION_ID).build();
            msession.setSessionListener(VideoChatActivity.this);
            msession.connect(TOKEN);
        }
        else{
            EasyPermissions.requestPermissions(this,"Grant Permissions",RC_VIDEO_PERMISSIONS,permission);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        mpushlier=new com.opentok.android.Publisher.Builder(VideoChatActivity.this).build();
        mpushlier.setPublisherListener(VideoChatActivity.this);
        mpublisherlayout.addView(mpushlier.getView());

        if (mpushlier.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mpushlier.getView()).setZOrderOnTop(true);

        }
        msession.publish(mpushlier);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
            if (mSubscriber==null){
                mSubscriber=new Subscriber.Builder(this,stream).build();
                msession.subscribe(mSubscriber);
                msubscriberlayout.addView(mSubscriber.getView());
            }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        if (mSubscriber!=null){
            mSubscriber=null;
            msubscriberlayout.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}