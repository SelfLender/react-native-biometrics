package com.rnbiometrics;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Created by brandon on 4/9/18.
 */

public interface ReactNativeBiometricsCallback {

    void onAuthenticated(FingerprintManager.CryptoObject cryptoObject);

    void onCancel();

    void onError();
}
