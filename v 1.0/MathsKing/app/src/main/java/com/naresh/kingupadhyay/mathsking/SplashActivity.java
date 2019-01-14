package com.naresh.kingupadhyay.mathsking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private final int STR_SPLASH_TIME = 2800;
    private FirebaseAuth mAuth;
    private static DatabaseReference favourite;
    private RecyclerView mBlogList;
    protected String NAME="name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

       // if (isNetworkAvailable()){
            SharedPreferences prefs =getSharedPreferences(NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear().apply();
       // }
        mBlogList=(RecyclerView)findViewById(R.id.recycler_VIEW);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();

    }


    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("favourites")) {

                        favourite=FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("favourites");
                        favourite.keepSynced(true);
                        FirebaseRecyclerAdapter<dFavourite,ChapterViewHolder> firebaseRecyclerAdapter =new FirebaseRecyclerAdapter <dFavourite, ChapterViewHolder>
                                (dFavourite.class,R.layout.content_determinants,ChapterViewHolder.class,favourite) {
                            @Override
                            protected void populateViewHolder(ChapterViewHolder viewHolder, dFavourite model, int position) {
                                viewHolder.setTitle(model.getTitle());

                            }
                        };
                        mBlogList.setAdapter(firebaseRecyclerAdapter);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }

        startSplashTimer();

        if (!isNetworkAvailable()){
            Toast.makeText(SplashActivity.this,"You are offline", Toast.LENGTH_SHORT).show();
        }
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder{
        View mView;
        private String tileText;
        private String NAME="name";
        private String KEY;

        public ChapterViewHolder(View itemView){
            super(itemView);
            mView=itemView;

        }

        public void setTitle(String title){
            tileText=title;
            KEY = tileText;
            final Activity myActivity=(Activity)(itemView.getContext());
            final SharedPreferences prefs=myActivity.getSharedPreferences(NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor edt = prefs.edit();
            edt.putBoolean(KEY,false);
            edt.apply();
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void startSplashTimer() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser!=null) {

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, LoginOption.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }


                }
            }, STR_SPLASH_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
