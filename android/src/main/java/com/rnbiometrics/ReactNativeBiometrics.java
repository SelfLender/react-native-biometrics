package com.rnbiometrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * Created by brandon on 4/5/18.
 */

public class ReactNativeBiometrics extends ReactContextBaseJavaModule {

    protected String biometricKeyAlias = "biometric_key";

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
                FingerprintManager fingerprintManager = reactApplicationContext.getSystemService(FingerprintManager.class);
                Boolean isHardwareDetected = fingerprintManager.isHardwareDetected();
                Boolean hasFingerprints = fingerprintManager.hasEnrolledFingerprints();

                KeyguardManager keyguardManager = (KeyguardManager) reactApplicationContext.getSystemService(Context.KEYGUARD_SERVICE);
                Boolean hasProtectedLockscreen = keyguardManager.isKeyguardSecure();

                if (isHardwareDetected && hasFingerprints && hasProtectedLockscreen) {
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
        boolean deletionSuccessful = deleteBiometricKey();
        if (deletionSuccessful) {
            promise.resolve(true);
        } else {
            promise.reject("Error deleting biometric key from keystore", "Error deleting biometric key from keystore");
        }
    }

    @ReactMethod
    public void createSignature(String title, String payload, Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Signature signature = Signature.getInstance("SHA256withRSA");
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);

                PrivateKey privateKey = (PrivateKey) keyStore.getKey(biometricKeyAlias, null);
                signature.initSign(privateKey);

                FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(signature);

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

    protected boolean deleteBiometricKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            keyStore.deleteEntry(biometricKeyAlias);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected ReactNativeBiometricsCallback getSignatureCallback(final String payload, final Promise promise) {
        return new ReactNativeBiometricsCallback() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onAuthenticated(FingerprintManager.CryptoObject cryptoObject) {
                try {
                    Signature cryptoSignature = cryptoObject.getSignature();
                    cryptoSignature.update(payload.getBytes());
                    byte[] signed = cryptoSignature.sign();
                    String signedString = Base64.encodeToString(signed, Base64.DEFAULT);
                    signedString = signedString.replaceAll("\r", "").replaceAll("\n", "");
                    promise.resolve(signedString);
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

    protected ReactNativeBiometricsCallback getCreationCallback(final Promise promise) {
        return new ReactNativeBiometricsCallback() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onAuthenticated(FingerprintManager.CryptoObject cryptoObject) {
                try {
                    deleteBiometricKey();
                    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                    KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(biometricKeyAlias, KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                            .setUserAuthenticationRequired(true)
                            .build();
                    keyPairGenerator.initialize(keyGenParameterSpec);

                    KeyPair keyPair = keyPairGenerator.generateKeyPair();
                    PublicKey publicKey = keyPair.getPublic();
                    byte[] encodedPublicKey = publicKey.getEncoded();
                    String publicKeyString = Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);
                    publicKeyString = publicKeyString.replaceAll("\r", "").replaceAll("\n", "");
                    promise.resolve(publicKeyString);
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
                promise.reject("error generating public private keys" , "error generating public private keys");
            }
        };
    }
}
