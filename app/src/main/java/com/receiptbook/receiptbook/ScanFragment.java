package com.receiptbook.receiptbook;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    private final int SUCCESS_DISPLAY_LENGTH = 5000;
    public static final double NOT_SET = -1.0d;
    public static final double LOADING = -0.5d;
    public static final double FAILED = -2.0d;

    private static String vendor = "";
    private static double price = NOT_SET;

    private TextView mAction;
    private TextView mReceipt;
    private ImageView mImg;


    public ScanFragment() {
        // Required empty public constructor
    }

    public static ScanFragment getInstance(String vendor, double price) {
        ScanFragment f = new ScanFragment();
        Bundle args = new Bundle();
        args.putString("vendor", vendor);
        args.putDouble("price", price);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Check NFC state

        setRetainInstance(true);
        try {
            vendor = getArguments().getString("vendor");
            price = getArguments().getDouble("price");
        } catch (Exception e) {
            e.printStackTrace();
            vendor = "";
            price = NOT_SET;
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mReceipt = (TextView) getActivity().findViewById(R.id.receipt);
        mAction = (TextView) getActivity().findViewById(R.id.action);
        mImg = (ImageView) getActivity().findViewById(R.id.img_action);

        boolean reset = false;

        if (!vendor.isEmpty() && price == FAILED) {
            reset = true;
            //Show failed screen
            mImg.setImageResource(R.drawable.ic_cross);
            mImg.setAnimation(null);
            mAction.setText(getString(R.string.result_failed));
            mReceipt.setText(vendor);
        } else if (!vendor.isEmpty() && price == LOADING) {
            reset = false;
            //Show loading screen
            RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(700);

            mImg.setImageResource(R.drawable.ic_load);
            mImg.startAnimation(anim);
            mAction.setText(getString(R.string.loading));
            mReceipt.setText(vendor);
        } else if (!vendor.isEmpty() && price != NOT_SET) {
            reset = true;
            //Show success
            mImg.setImageResource(R.drawable.ic_tick);
            mImg.setAnimation(null);
            mAction.setText(getString(R.string.result_success));
            mReceipt.setText(vendor + " - £" + String.format("%.2f", price));
        } else {
            reset = false;
            mReceipt.setText("");
            mImg.setAnimation(null);
            mImg.setImageResource(R.drawable.ic_receive);
            mAction.setText(getString(R.string.action_scan));

        }

        if (reset) {
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Update screen
                    try {
                        price = NOT_SET;
                        vendor = "";

                        mReceipt.setText("");
                        mAction.setText(getString(R.string.action_scan));
                        mImg.setImageResource(R.drawable.ic_receive);
                        mImg.setAnimation(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, SUCCESS_DISPLAY_LENGTH);

        }
    }

}
