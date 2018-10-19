package com.rnbiometrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.rnbiometrics.util.CryptoManager;

/**
 * Created by brandon on 4/5/18.
 */

public class ReactNativeBiometrics extends ReactContextBaseJavaModule {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_STORE_TYPE = "AndroidKeyStore";
    private static String BIOMETRIC_KEY_ALIAS = "biometric_key";
    private CryptoManager cryptoManager = new CryptoManager(BIOMETRIC_KEY_ALIAS,
            KEY_STORE_TYPE, SIGNATURE_ALGORITHM);


    public ReactNativeBiometrics(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ReactNativeBiometrics";
    }

    @ReactMethod
    public void isSensorAvailable(Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ReactApplicationContext reactApplicationContext = getReactApplicationContext();
                FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(getReactApplicationContext());
                boolean isHardwareDetected = fingerprintManager.isHardwareDetected();
                boolean hasFingerprints = fingerprintManager.hasEnrolledFingerprints();

                KeyguardManager keyguardManager = (KeyguardManager) reactApplicationContext.getSystemService(Context.KEYGUARD_SERVICE);
                boolean hasProtectedLockScreen = keyguardManager.isKeyguardSecure();

                if (isHardwareDetected && hasFingerprints && hasProtectedLockScreen) {
                    promise.resolve("TouchID");
                } else {
                    promise.resolve(null);
                }
            } else {
                promise.resolve(null);
            }
        } catch (Exception e) {
            promise.reject("Error detecting fingerprint availability: " + e.getMessage(), "Error detecting fingerprint availability");
        }
    }

    @ReactMethod
    public void createKeys(String title, Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ReactNativeBiometricsDialog dialog = new ReactNativeBiometricsDialog();
                dialog.init(title, null, getCreationCallback(promise));
                Activity activity = getCurrentActivity();
                dialog.show(activity.getFragmentManager(), "fingerprint_dialog");
            } else {
                promise.reject("cannot generate keys on android versions below 6.0", "cannot generate keys on android versions below 6.0");
            }
        } catch (Exception e) {
            promise.reject("error generating public private keys: " + e.getMessage(), "error generating public private keys");
        }
    }

    @ReactMethod
    public void deleteKeys(Promise promise) {
        if (cryptoManager.deleteSignature()) {
            promise.resolve(true);
        } else {
            promise.reject("Error deleting biometric key from keystore", "Error deleting biometric key from keystore");
        }
    }

    @ReactMethod
    public void createSignature(String title, String payload, Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat
                        .CryptoObject(cryptoManager.getSignature());

                ReactNativeBiometricsDialog dialog = new ReactNativeBiometricsDialog();
                dialog.init(title, cryptoObject, getSignatureCallback(payload, promise));

                Activity activity = getCurrentActivity();
                dialog.show(activity.getFragmentManager(), "fingerprint_dialog");
            } else {
                promise.reject("cannot generate keys on android versions below 6.0", "cannot generate keys on android versions below 6.0");
            }
        } catch (Exception e) {
            promise.reject("error signing payload: " + e.getMessage(), "error generating signature");
        }
    }


    private ReactNativeBiometricsCallback getSignatureCallback(final String payload, final Promise promise) {
        return new ReactNativeBiometricsCallback() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onAuthenticated(FingerprintManagerCompat.CryptoObject cryptoObject) {
                try {

                    promise.resolve(cryptoManager.sign(cryptoObject, payload));
                } catch (Exception e) {
                    promise.reject("error creating signature: " + e.getMessage(), "error creating signature");
                }
            }

            @Override
            public void onCancel() {
                promise.reject("User cancelled fingerprint authorization", "User cancelled fingerprint authorization");
            }

            @Override
            public void onError() {
                promise.reject("error detecting fingerprint", "error detecting fingerprint");
            }
        };
    }

    private ReactNativeBiometricsCallback getCreationCallback(final Promise promise) {
        return new ReactNativeBiometricsCallback() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onAuthenticated(FingerprintManagerCompat.CryptoObject cryptoObject) {
                try {
                    cryptoManager.InitializeKeyPair();
                    promise.resolve(cryptoManager.getPublicKeyBase64EncodedString());
                } catch (Exception e) {
                    promise.reject("error generating public private keys: " + e.getMessage(), "error generating public private keys");
                }
            }

            @Override
            public void onCancel() {
                promise.reject("User cancelled fingerprint authorization", "User cancelled fingerprint authorization");
            }

            @Override
            public void onError() {
                promise.reject("error generating public private keys", "error generating public private keys");
            }
        };
    }
}
