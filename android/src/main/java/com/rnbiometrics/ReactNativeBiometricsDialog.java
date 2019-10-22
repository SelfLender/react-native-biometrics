package com.rnbiometrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.rnbiometrics.R;
import com.facebook.react.bridge.ReadableMap;

/**
 * Created by brandon on 4/6/18.
 */

@TargetApi(Build.VERSION_CODES.M)
public class ReactNativeBiometricsDialog extends DialogFragment implements ReactNativeBiometricsCallback {

    protected String title;
    protected ReadableMap params;
    protected FingerprintManager.CryptoObject cryptoObject;
    protected ReactNativeBiometricsCallback biometricAuthCallback;

    protected ReactNativeBiometricsHelper biometricAuthenticationHelper;
    protected Activity activity;
    protected Button cancelButton;
    protected TextView descTextView;
    protected TextView statusTextView;


    public void init(String title,ReadableMap params, FingerprintManager.CryptoObject cryptoObject, ReactNativeBiometricsCallback callback) {
        this.title = title;
        this.params = params;
        this.cryptoObject = cryptoObject;
        this.biometricAuthCallback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BiometricsDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(title);
        View view = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        if (params.hasKey("fingerprint_message")) {
            descTextView = (TextView) view.findViewById(R.id.fingerprint_description);
            descTextView.setText(params.getString("fingerprint_message"));
        }
        if (params.hasKey("fingerprint_hint")) {
            statusTextView = (TextView) view.findViewById(R.id.fingerprint_status);
            statusTextView.setText(params.getString("fingerprint_hint"));
        }
       
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        if (params.hasKey("fingerprint_cancel")) {
            cancelButton.setText(params.getString("fingerprint_cancel"));
        } else {
            cancelButton.setText(R.string.fingerprint_cancel);
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
                onCancel();
            }
        });

        biometricAuthenticationHelper = new ReactNativeBiometricsHelper(
                activity.getSystemService(FingerprintManager.class),
                (ImageView) view.findViewById(R.id.fingerprint_icon),
                (TextView) view.findViewById(R.id.fingerprint_status),
                params,
                this
        );

        return view;
    }

    // DialogFragment lifecycle methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        biometricAuthenticationHelper.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        biometricAuthenticationHelper.startListening(cryptoObject);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onCancel();
    }

    // ReactNativeBiometricsCallback methods
    @Override
    public void onAuthenticated(FingerprintManager.CryptoObject cryptoObject) {
        dismissAllowingStateLoss();
        if (biometricAuthCallback != null) {
            biometricAuthCallback.onAuthenticated(cryptoObject);
        }
    }

    @Override
    public void onCancel() {
        if (biometricAuthCallback != null) {
            biometricAuthCallback.onCancel();
        }
    }

    @Override
    public void onError() {
        dismissAllowingStateLoss();
        if (biometricAuthCallback != null) {
            biometricAuthCallback.onError();
        }
    }
}
