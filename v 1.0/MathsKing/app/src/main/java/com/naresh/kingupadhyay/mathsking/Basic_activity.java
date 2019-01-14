package com.naresh.kingupadhyay.mathsking;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


public class Basic_activity extends AppCompatActivity {
    private Toolbar toolbar;
    public File imageFile;
    private String sharePath="nothing";
    private RelativeLayout relativeLayout;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_activity);

        relativeLayout=findViewById(R.id.nameKing);
        relativeLayout.setVisibility(View.VISIBLE);

        MobileAds.initialize(this, "ca-app-pub-6924423095909700~8475665982");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Bundle bundle = getIntent().getExtras();
        String pdfUrl = bundle.getString("pdfUrl");
        final String title = bundle.getString("title");
        final String topic = bundle.getString("topicN");
        final TextView textView=findViewById(R.id.titleb);

        ImageButton back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        WebView webView= findViewById(R.id.webView);
        final ProgressBar progressBar=findViewById(R.id.progressBar);
        //String pdfUrl="http://docs.google.com/viewerng/viewer?embedded=true&url=" + imageUrl;
        progressBar.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                textView.setText("Loading....");
                if (newProgress==100){
                    progressBar.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.GONE);
                    if(!isNetworkAvailable()){
                        view.setVisibility(View.GONE);
                        Toast.makeText(Basic_activity.this,"Connect to Internet than refresh", Toast.LENGTH_LONG).show();
                    }else
                        view.setVisibility(View.VISIBLE);
                    textView.setText(title+topic);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(pdfUrl);


    }

    private void refreshPage(){
        try{
            final TextView textView=findViewById(R.id.titleb);
            WebView webView= findViewById(R.id.webView);
            Bundle bundle = getIntent().getExtras();
            String pdfUrl = bundle.getString("pdfUrl");
            final String title = bundle.getString("title");
            final String topic = bundle.getString("topicN");
            final ProgressBar progressBar=findViewById(R.id.progressBar);
            //String pdfUrlServerLink="http://docs.google.com/viewerng/viewer?embedded=true&url=" + pdfUrl;
            progressBar.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    textView.setText("Loading....");
                    if (newProgress==100){
                        progressBar.setVisibility(View.GONE);
                        if(!isNetworkAvailable()){
                            view.setVisibility(View.GONE);
                            Toast.makeText(Basic_activity.this,"Connect to Internet than refresh", Toast.LENGTH_LONG).show();
                        }else
                            view.setVisibility(View.VISIBLE);

                        textView.setText(title+topic);
                    }
                }
            });
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(pdfUrl);


        }catch (Throwable e){
            e.printStackTrace();

        }

    }


    private void takeScreenShot(View v,boolean shareCheck){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        try {
            relativeLayout.setVisibility(View.VISIBLE);
            v.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);
            String filename= now +".jpeg";
            imageFile = new File(this.getExternalCacheDir(), filename);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            //setting screenshot in imageview
            popUpShareImage(imageFile,shareCheck);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            relativeLayout.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void share(final File imageFile){
            Uri uri = Uri.fromFile(imageFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent .setType("image/*");
            intent.putExtra(Intent.EXTRA_SUBJECT,"Boost up your speed in mathematics with 0 cost");
            intent.putExtra(Intent.EXTRA_TEXT,"Click here to get FREE all mathematics formulas,shortcuts,concepts with examples and questions.  \nhttp://mathsking.com");
            intent .putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
            startActivity(Intent.createChooser(intent,"Share Image"));
    }

    private void sendMail(final File imageFile){
        Uri uri = Uri.fromFile(imageFile);
        Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check The Error ");
        intent.putExtra(Intent.EXTRA_TEXT, "There is an error present in this concept/question/answer please recheck it."+"\n[Edit here to say something about the problem...");
        intent.setData(Uri.parse("mailto:maths.developers@gmail.com")); // or just "mailto:" for blank
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
        intent .putExtra(Intent.EXTRA_STREAM, uri);

        try {
            startActivity(Intent.createChooser(intent, "Report feedback"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Basic_activity.this, "No email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
    private void popUpShareImage(final File imageFile, final boolean shareCheck) {
        final Dialog myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.share_image);
        myDialog.setCancelable(false);
        final Button share = (Button) myDialog.findViewById(R.id.shareHere);
        Button cancel = (Button) myDialog.findViewById(R.id.cancelHere);
        ImageView imageView=(ImageView)myDialog.findViewById(R.id.shareImage);
        Bitmap ssbitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView.setImageBitmap(ssbitmap);
        myDialog.show();
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shareCheck)
                    share(imageFile);
                else
                    sendMail(imageFile);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.GONE);
                myDialog.dismiss();
            }
        });
    }
    public void rateUs(){
        Context context=getApplicationContext();
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.last_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.last_refresh) {
            refreshPage();
            return true;
        }else if (id==R.id.last_share){
            RelativeLayout relativeLayout= findViewById(R.id.screenShot);
            takeScreenShot(relativeLayout,true);
            return true;
        }else if (id==R.id.rate_us){
            rateUs();
            return true;
        }else if (id==R.id.last_report){
            RelativeLayout relativeLayout= findViewById(R.id.screenShot);
            takeScreenShot(relativeLayout,false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
