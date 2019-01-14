
package com.naresh.kingupadhyay.mathsking;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class Short_note extends AppCompatActivity{

    private Toolbar toolbar;
    private RecyclerView mBlogList;
    private RecyclerView fav_BlogList;
    private static DatabaseReference mDatabase;
    private static DatabaseReference favourites;
    public ProgressDialog progDailog;
    public String[] arraySpinner;
    public static Drawable draw;
    private static DatabaseReference favourite;
    protected String NAME="name";
    public String book;
    public String chapter;
    public static String topic;
    public String topicString;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        Bundle bundle = getIntent().getExtras();
        book = bundle.getString("book");
        chapter = bundle.getString("chapter");
        topicString=bundle.getString("topic","All");
        topic="all";

        ImageButton back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // MobileAds.initialize(getApplicationContext(), "ca-app-pub-6924423095909700~8475665982");

        MobileAds.initialize(this, "ca-app-pub-6924423095909700~8475665982");

        mAdView = findViewById(R.id.advCourse);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mBlogList=(RecyclerView)findViewById(R.id.myrecyclerview);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));


        fav_BlogList=(RecyclerView)findViewById(R.id.reCyclerview);
        fav_BlogList.setHasFixedSize(true);
        fav_BlogList.setLayoutManager(new LinearLayoutManager(this));

        // Adding Toolbar to Main screen
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        arraySpinner=topicString.split(":");
        final Spinner spinner=(Spinner) findViewById(R.id.spinner_determinants);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
        // rotation.setRepeatCount(Animation.INFINITE);
        rotation.setRepeatCount(-1);
        rotation.setDuration(1000);


        final ImageButton imageButton=(ImageButton)findViewById(R.id.refresh_determinants);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topic=String.valueOf(spinner.getSelectedItem()).toLowerCase();
                refreshFavourites();
                addCard(book,chapter,topic,mBlogList);
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        refreshFavourites();
        addCard(book,chapter,topic,mBlogList);
    }

    public static class mChapterViewHolder extends RecyclerView.ViewHolder{
        View mView;
        boolean check;
        private String tileText;
        private String conceptUrl;
        private String conceptPdfUrl;

        private String NAME="name";
        private String KEY;
        final Resources res = itemView.getResources();
        final ImageButton favoriteImageButton = (ImageButton) itemView.findViewById(R.id.add_favorite);

        public mChapterViewHolder(View itemView){
            super(itemView);
            mView=itemView;


            final ImageView concept_Imag=(ImageView)itemView.findViewById(R.id.concept_image);
            concept_Imag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, Basic_activity.class);
                    intent.putExtra("pdfUrl",conceptPdfUrl);
                    intent.putExtra("topicN",topic);
                    intent.putExtra("title","Concept:");//activity
                    context.startActivity(intent);
                }
            });

            final Activity myActivity=(Activity)(itemView.getContext());
            favoriteImageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    KEY = tileText;
                    final SharedPreferences prefs=myActivity.getSharedPreferences(NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edt = prefs.edit();

                    FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
                    favourites=FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("favourites");
                    favourites.keepSynced(true);
                    favourites.push().child("title").setValue(tileText);

                    if(check){
                        draw = res.getDrawable(R.drawable.favourite_fill);
                        favoriteImageButton.setImageDrawable(draw);

                        Query applesQuery = favourites.orderByChild("title").equalTo(tileText);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().child("image_concept").setValue(conceptUrl);
                                    appleSnapshot.getRef().child("concept_pdf").setValue(conceptPdfUrl);

                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        edt.putBoolean(KEY,false);
                        edt.apply();
                        check=prefs.getBoolean(KEY,true);
                        // check=false;
                    }else {
                        draw = res.getDrawable(R.drawable.favourite_blank);
                        favoriteImageButton.setImageDrawable(draw);

                        Query applesQuery = favourites.orderByChild("title").equalTo(tileText);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        edt.putBoolean(KEY,true);
                        edt.apply();
                        check=prefs.getBoolean(KEY,true);
                        //check=true;
                    }


                }
            });


        }

        public void setTitle(String title){
            tileText=title;
            KEY = tileText;
            final Activity myActivity=(Activity)(itemView.getContext());
            final SharedPreferences prefs=myActivity.getSharedPreferences(NAME, Context.MODE_PRIVATE);
            check=prefs.getBoolean(KEY,true);
            if(check){
                draw = res.getDrawable(R.drawable.favourite_blank);
                favoriteImageButton.setImageDrawable(draw);
            }else {
                draw = res.getDrawable(R.drawable.favourite_fill);
                favoriteImageButton.setImageDrawable(draw);
            }
            TextView title_text=(TextView)mView.findViewById(R.id.concept);
            title_text.setText(title);
        }


        private void setImage_concept(String image_concept) {
            conceptUrl=image_concept;
            ImageView concept_Image=(ImageView) itemView.findViewById(R.id.concept_image);
            final ProgressBar progressBar1=itemView.findViewById(R.id.progressBar1);
            picassoLoadImage(concept_Image,image_concept,progressBar1);
        }

        private void setConcept_pdf(String conceptPdf){
            conceptPdfUrl=conceptPdf;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    public void progressBar(){
        progDailog = new ProgressDialog(Short_note.this);
        progDailog.setTitle("Data Loading....");
        progDailog.setMessage("Please Wait.....");
        progDailog.setCancelable(false);
        progDailog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        progDailog.show();

    }

    public void addCard(String book,String chapter,String topic,RecyclerView recyclerView){
        progressBar();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("chapters").child(book).child(chapter).child("shortnote").child(topic);
        mDatabase.keepSynced(true);
        FirebaseRecyclerAdapter<dChapter,mChapterViewHolder> firebaseRecyclerAdapter =new FirebaseRecyclerAdapter <dChapter, mChapterViewHolder>
                (dChapter.class,R.layout.content_short_note,mChapterViewHolder.class,mDatabase) {
            @Override
            protected void populateViewHolder(mChapterViewHolder viewHolder, dChapter model, int position) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setImage_concept(model.getImage_concept());
                viewHolder.setConcept_pdf(model.getConcept_pdf());
                progDailog.dismiss();
                ((ImageButton)findViewById(R.id.refresh_determinants)).clearAnimation();
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }


    public void refreshFavourites(){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("favourites")) {

                    favourite=FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("favourites");
                    favourite.keepSynced(true);
                    FirebaseRecyclerAdapter<dFavourite,MainActivity.ChapterViewHolder> fireBaseRecyclerAdapter =new FirebaseRecyclerAdapter <dFavourite, MainActivity.ChapterViewHolder>
                            (dFavourite.class,R.layout.content_short_note,MainActivity.ChapterViewHolder.class,favourite) {
                        @Override
                        protected void populateViewHolder(MainActivity.ChapterViewHolder viewHolder, dFavourite model, int position) {
                            viewHolder.setTitle(model.getTitle());
                        }
                    };
                    fav_BlogList.setAdapter(fireBaseRecyclerAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }




    public static void picassoLoadImage(final ImageView imageView , String imageUrl, final ProgressBar progressBar){
        Picasso.get().load(imageUrl)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    @Override
    public void onBackPressed(){
        if (isNetworkAvailable()){
            SharedPreferences prefs =getSharedPreferences(NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear().apply();
        }
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }


}