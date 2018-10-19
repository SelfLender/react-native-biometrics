package com.rnbiometrics;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by brandon on 4/9/18.
 */

public interface ReactNativeBiometricsCallback {

    void onAuthenticated(FingerprintManagerCompat.CryptoObject cryptoObject);

    void onCancel();

    void onError();
}
