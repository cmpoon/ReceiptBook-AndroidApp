package com.receiptbook.receiptbook;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
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
        WebView webView = getWebView();
        mPbar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        if (webView != null) {
            if (webView.getOriginalUrl() == null) {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                webView.getSettings().setUserAgentString("ReceiptBook-App");
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        mPbar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        mPbar.setVisibility(View.GONE);
                    }
                });

                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int progress)
                    {
                        if(progress < 100 && mPbar.getVisibility() == ProgressBar.GONE){
                            mPbar.setVisibility(ProgressBar.VISIBLE);
                        }
                        mPbar.setProgress(progress);
                        if(progress == 100) {
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
