package com.example.trycall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.trycall.Adapters.AllUsersAdapter;
import com.example.trycall.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView ;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    SinchClient sinchClient;
    Call call;
    ArrayList<User> usersArrayList;
    DatabaseReference reference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersArrayList=new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        auth=FirebaseAuth.getInstance();
        firebaseUser= auth.getCurrentUser();


        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(firebaseUser.getUid())
                .applicationKey("6fb91f40-4448-4422-a91a-462755f2d151")
                .applicationSecret("RbVQuXU3S0KBY/jTkQNU/w==")
                .environmentHost("clientapi.sinch.com")
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener(){

        });

        sinchClient.start();

        fethchAllUsers();

    }

    private void fethchAllUsers() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersArrayList.clear();

                for(DataSnapshot dss: dataSnapshot.getChildren()) {
                    User user= dss.getValue(User.class);
                    usersArrayList.add(user);

                }

                AllUsersAdapter adapter = new AllUsersAdapter(MainActivity.this,usersArrayList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"error:"+ databaseError.getMessage(),Toast.LENGTH_LONG ).show();
            }
        });
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(getApplicationContext(),"Ringing",Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCallEstablished(Call call) {
            sinchClient.getAudioController().enableSpeaker();
            Toast.makeText(getApplicationContext(),"Call established",Toast.LENGTH_LONG).show();
            AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(true);

        }

        @Override
        public void onCallEnded(Call endedCall) {
            Toast.makeText(getApplicationContext(),"Call ended",Toast.LENGTH_LONG).show();
            call= null;
            endedCall.hangup();

        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== R.id.menu_logout){
            if (firebaseUser!=null){
                auth.signOut();
                finish();
                Intent i = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(i);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class SinchCallClientListener implements CallClientListener{

        @Override
        public void onIncomingCall(CallClient callClient, final Call incomingcall) {
            //open dialog incoming call
            AlertDialog alertDialog= new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Incoming Cal  l");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reject", (dialog, which) -> {
                dialog.dismiss();
                call.hangup();
            });

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept", (dialog, which) -> {
                call = incomingcall;
                call.answer();
                call.addCallListener(new SinchCallListener());
                Toast.makeText(getApplicationContext(),"Call in action",Toast.LENGTH_LONG).show();


            });

            alertDialog.show();

        }
    }

    public void callUser(User user){
        if (call==null){
            call= sinchClient.getCallClient().callUser(user.getUserid());
            call.addCallListener(new SinchCallListener());

            openCallerDialog(call);
            sinchClient.getAudioController().enableSpeaker();
            AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(true);
        }

    }

    private void openCallerDialog(final Call call) {
        AlertDialog alertDialogCall = new AlertDialog.Builder(MainActivity.this).create();
        alertDialogCall.setTitle("VoIP Call Initiated");
        alertDialogCall.setMessage("Calling");
        alertDialogCall.setButton(AlertDialog.BUTTON_NEUTRAL, "Hang Up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                call.hangup();
            }
        });

        alertDialogCall.show();
    }
}
