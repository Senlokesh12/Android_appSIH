package com.example.hackathonapp;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {
    public WebView webView;
    public ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = MainActivity.class.getSimpleName();


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView=findViewById(R.id.webview);
        progressBar=findViewById(R.id.progressBar) ;
        swipeRefreshLayout=findViewById(R.id.swipeToRefresh);
        swipeRefreshLayout.setColorSchemeColors(Color.RED,Color.MAGENTA,Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();

            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#292121")));


       //webView.loadUrl("https://js-qr-test.herokuapp.com/");
       //webView.loadUrl("https://webcamtests.com/check");

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        //webView.getSettings().setAllowFileAccess(true);
       // webSettings.setDomStorageEnabled(true);
       // webView.getSettings().setAllowFileAccessFromFileURLs(true);
       // webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

       // webView.getSettings().setPluginState(WebSettings.PluginState.ON);
       // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { webView.getSettings().getMediaPlaybackRequiresUserGesture();}

        System.out.println("Before web Chrome view client ");
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                setTitle("Loading...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                setTitle(view.getTitle());
                swipeRefreshLayout.setRefreshing(false);
            }



        });

         webView.setWebChromeClient(new WebChromeClient() {
             // Need to accept permissions to use the camera
             @TargetApi(Build.VERSION_CODES.M)

             @Override
             public void onPermissionRequest(final PermissionRequest request) {
                 if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M) {
                     Log.i(TAG, "onPermissionRequest");
                     if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                         request.grant(request.getResources());

                     } else {
                         ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 2);
                         if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                             request.grant(request.getResources());
                             //webView.getWebViewRenderProcess();

                         }
                     }
                 }

             }

             @Override
             public void onPermissionRequestCanceled(PermissionRequest request) {
                 Toast.makeText(MainActivity.this, "Permission cancelled ", Toast.LENGTH_SHORT).show();
             }
         });


        //override webview back press methods




//provide downloading mechanism  below
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String useragent, String contentDisposion, String mimeType, long contentlength) {

                  if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                  {

                      if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                      {
                          downloadFiles(url,useragent,contentDisposion,mimeType);

                      }
                      else
                      {
                          ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                      }

                  }
                  else
                  {
                      downloadFiles(url,useragent,contentDisposion,mimeType);

                  }
            }
        });



    }

    public void downloadFiles(final String url,final String useragent,final String contentDisposition,final String mimeType)
    {
        final String fileName= URLUtil.guessFileName(url,contentDisposition,mimeType);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(" Confirmation ?")
                .setMessage("Do you want to Download this File ?  " +fileName)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
                        String cookie= CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("Cookie",cookie);
                        request.addRequestHeader("User-Agent",useragent);
                        request.allowScanningByMediaScanner();

                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        DownloadManager downloadManager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                        System.out.println("Download manager opened");
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
                        downloadManager.enqueue(request);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                          dialogInterface.cancel();
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.webview_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

      /*  if(item.getItemId()==R.id.refresh)
        {
            webView.reload();
        }
*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(webView.canGoBack())
        {
            webView.goBack();
        }
        else
        {
            super.onBackPressed();
        }

    }

  /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        userPermission();
    }




    private void userPermission()
    {
         String permissions[]= {Manifest.permission.CAMERA};
         if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0])==PackageManager.PERMISSION_GRANTED)
         {
             //ActivityCompat.requestPermissions(MainActivity.this,permissions,2);
             System.out.println("permission alloted");
         }
         else
         {
             ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.CAMERA},2);
         }

     }
*/

}

