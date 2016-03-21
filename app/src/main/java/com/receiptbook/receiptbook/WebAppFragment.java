package com.receiptbook.receiptbook;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebAppFragment extends WebViewFragment {

    private String mURL;
    private ProgressBar mPbar = null;

    private Context c;
    private boolean isConnected = true;

    public static WebAppFragment getInstance(String url) {
        WebAppFragment f = new WebAppFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mURL = getArguments().getString("url");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isConnected = true;

        c = this.getActivity().getApplicationContext();

        //check for connectivity.
        ConnectivityManager connectivityManager = (ConnectivityManager)
                c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if (ni == null || ( ni != null && ni.getState() != NetworkInfo.State.CONNECTED)) {
                // record the fact that there is not connection
                isConnected = false;
            }
        }

        WebView webView = getWebView();
        mPbar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        if (webView != null) {
            webView.setNetworkAvailable(isConnected);
            if (webView.getOriginalUrl() == null) {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                webView.getSettings().setUserAgentString("ReceiptBook-App");
                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        mPbar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        mPbar.setVisibility(View.GONE);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (isConnected) {
                            // return false to let the WebView handle the URL
                            view.loadUrl(url);
                        } else {
                            // show the proper "not connected" message
                            view.loadData(getString(R.string.offlineMessageHtml), "text/html", "utf-8");
                            // return true if the host application wants to leave the current
                            // WebView and handle the url itself
                        }

                        return false;
                    }

                    @Override
                    public void onReceivedError(WebView view, int errorCode,
                                                String description, String failingUrl) {
                        if (errorCode == ERROR_TIMEOUT) {
                            view.stopLoading();  // may not be needed
                            view.loadData(getString(R.string.timeoutMessageHtml), "text/html", "utf-8");
                        } else if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_IO){

                            view.stopLoading();  // may not be needed
                            view.loadData(getString(R.string.offlineMessageHtml), "text/html", "utf-8");
                        }
                    }

                    @TargetApi(android.os.Build.VERSION_CODES.M)
                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                        // Redirect to deprecated method, so you can use it in all SDK versions
                        onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                    }

                });

                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int progress) {
                        if (progress < 100 && mPbar.getVisibility() == ProgressBar.GONE) {
                            mPbar.setVisibility(ProgressBar.VISIBLE);
                        }
                        mPbar.setProgress(progress);
                        if (progress == 100) {
                            mPbar.setVisibility(ProgressBar.GONE);
                        }
                    }
                });

                webView.setBackgroundColor(Color.TRANSPARENT);
                webView.loadUrl(mURL);
            }
        }
    }

}
