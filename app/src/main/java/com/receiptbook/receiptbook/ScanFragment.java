package com.receiptbook.receiptbook;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    private final int SUCCESS_DISPLAY_LENGTH = 5000;
    public static final double NOT_SET = -1.0d;

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
        }catch(Exception e){
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

        //Show success
        if (!vendor.isEmpty() && price != NOT_SET) {
            mImg.setImageResource(R.drawable.ic_tick);
            mAction.setText(getString(R.string.result_success));
            mReceipt.setText(vendor + " - £" + Double.toString(price));
        } else {
            mReceipt.setText("");
            mImg.setImageResource(R.drawable.ic_receive);
            mAction.setText(getString(R.string.action_scan));
        }

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
                    mImg.setImageResource(R.drawable.ic_receive);
                    mAction.setText(getString(R.string.action_scan));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, SUCCESS_DISPLAY_LENGTH);

    }

}
