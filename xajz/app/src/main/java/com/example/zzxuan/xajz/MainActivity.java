package com.example.zzxuan.xajz;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    private View view;
    public WebView webview;
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setIcon(R.drawable.logomini);
        init();

    }

    void init(){
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                //重新刷新页面
                webviewRefresh();
            }
        });
        swipeLayout.setColorSchemeResources(R.color.holo_blue_bright,
                R.color.holo_green_light, R.color.holo_orange_light,
                R.color.holo_red_light);
        swipeLayout.setSize(5);

        webview = (WebView)findViewById(R.id.webview);

        webviewRefresh(getResources().getString(R.string.web_home));
        //添加javaScript支持
        webview.getSettings().setJavaScriptEnabled(true);
        //取消滚动条
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //触摸焦点起作用
        webview.requestFocus();
        //点击链接继续在当前browser中响应
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        //设置下载
        webview.setDownloadListener(new MyWebViewDownLoadListener());
        //设置进度条
        webview.setWebChromeClient(new WebChromeClient(){

            boolean isstopfresh = false;
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.i("process", "newProgress = " + newProgress);
                if(newProgress == 0){
                    isstopfresh = false;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                           // Log.i("process", "run()");
                            try{
                                Thread.sleep(2000);
                               // Log.i("process", "run() ->setRefreshing false");
                                isstopfresh = true;
                                swipeLayout.setRefreshing(false);
                            }catch (Exception ex) {

                            }
                        }
                    }).start();
                   // Log.i("process", "tread start");
                }
                if (newProgress >= 60){
                    //隐藏进度条
                  //  Log.i("process", "newProgress >= 60 ->setRefreshing false");
                    isstopfresh = true;
                    swipeLayout.setRefreshing(false);
                } else {
                    if (!swipeLayout.isRefreshing()
                            && !isstopfresh)
                        swipeLayout.setRefreshing(true);
                }

                super.onProgressChanged(view, newProgress);
            }
        });
    }

    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode== KeyEvent.KEYCODE_BACK)
        {
            if(webview.canGoBack())
            {
                webview.goBack();//返回上一页面
                return true;
            }
            else
            {
                System.exit(0);//退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_share) {
            share();
            return true;
        }
        else if (id == R.id.menu_clear){
            clearCacheFolder(this.getCacheDir(), System.currentTimeMillis());//删除此时之前的缓存.
        }
        else if (id == R.id.menu_about){
            webviewRefresh(getResources().getString(R.string.web_about));
        }
        else if (id == R.id.menu_exit){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public void webviewRefresh(){
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.loadUrl(webview.getUrl());
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    public void webviewRefresh(String url){
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.loadUrl(url);
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    void share()
    {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_TEXT, webview.getUrl());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }


    // clear the cache before time numDays
    private int clearCacheFolder(File dir, long numDays) {
        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }
                    if (child.lastModified() < numDays) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }
}
