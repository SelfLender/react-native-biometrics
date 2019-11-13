package com.rnbiometrics;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.AuthenticationCallback;
import androidx.biometric.BiometricPrompt.PromptInfo;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
                BiometricManager biometricManager = BiometricManager.from(reactApplicationContext);
                int canAuthenticate = biometricManager.canAuthenticate();

                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    WritableMap resultMap = Arguments.createMap();
                    resultMap.putBoolean("available", true);
                    resultMap.putString("biometryType", "biometrics");
                    promise.resolve(resultMap);
                } else {
                    WritableMap resultMap = Arguments.createMap();
                    resultMap.putBoolean("available", false);

                    switch (canAuthenticate) {
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            resultMap.putString("error", "BIOMETRIC_ERROR_NO_HARDWARE");
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            resultMap.putString("error", "BIOMETRIC_ERROR_HW_UNAVAILABLE");
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            resultMap.putString("error", "BIOMETRIC_ERROR_NONE_ENROLLED");
                            break;
                    }

                    promise.resolve(resultMap);
                }
            } else {
                WritableMap resultMap = Arguments.createMap();
                resultMap.putBoolean("available", false);
                resultMap.putString("error", "UNSUPPORTED_ANDROID_VERSION");
                promise.resolve(resultMap);
            }
        } catch (Exception e) {
            promise.reject("Error detecting biometrics availability: " + e.getMessage(), "Error detecting biometrics availability");
        }
    }

    @ReactMethod
    public void createKeys(String title, Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            } else {
                promise.reject("Cannot generate keys on android versions below 6.0", "Cannot generate keys on android versions below 6.0");
            }
        } catch (Exception e) {
            promise.reject("Error generating public private keys: " + e.getMessage(), "Error generating public private keys");
        }
    }

    @ReactMethod
    public void deleteKeys(Promise promise) {
        if (biometricKeyExists()) {
            boolean deletionSuccessful = deleteBiometricKey();

            if (deletionSuccessful) {
                promise.resolve(true);
            } else {
                promise.reject("Error deleting biometric key from keystore", "Error deleting biometric key from keystore");
            }
        } else {
            promise.resolve(false);
        }
    }

    @ReactMethod
    public void createSignature(final String title, final String payload, final Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiThreadUtil.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Signature signature = Signature.getInstance("SHA256withRSA");
                                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                                keyStore.load(null);

                                PrivateKey privateKey = (PrivateKey) keyStore.getKey(biometricKeyAlias, null);
                                signature.initSign(privateKey);

                                BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(signature);

                                AuthenticationCallback authCallback = new CreateSignatureCallback(promise, payload);
                                FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
                                Executor executor = Executors.newSingleThreadExecutor();
                                BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, authCallback);

                                PromptInfo promptInfo = new PromptInfo.Builder()
                                        .setDeviceCredentialAllowed(false)
                                        .setNegativeButtonText("Cancel")
                                        .setTitle(title)
                                        .build();
                                biometricPrompt.authenticate(promptInfo, cryptoObject);
                            } catch (Exception e) {
                                promise.reject("Error signing payload: " + e.getMessage(), "Error generating signature");
                            }
                        }
                    });
        } else {
            promise.reject("Cannot generate keys on android versions below 6.0", "Cannot generate keys on android versions below 6.0");
        }
    }

    @ReactMethod
    public void simplePrompt(final String title, final Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiThreadUtil.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                AuthenticationCallback authCallback = new SimplePromptCallback(promise);
                                FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
                                Executor executor = Executors.newSingleThreadExecutor();
                                BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, authCallback);

                                PromptInfo promptInfo = new PromptInfo.Builder()
                                        .setDeviceCredentialAllowed(false)
                                        .setNegativeButtonText("Cancel")
                                        .setTitle(title)
                                        .build();
                                biometricPrompt.authenticate(promptInfo);
                            } catch (Exception e) {
                                promise.reject("Error displaying local biometric prompt: " + e.getMessage(), "Error displaying local biometric prompt");
                            }
                        }
                    });
        } else {
            promise.reject("Cannot display biometric prompt on android versions below 6.0", "Cannot display biometric prompt on android versions below 6.0");
        }
    }

    protected boolean biometricKeyExists() {
      try {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        return keyStore.containsAlias(biometricKeyAlias);
      } catch (Exception e) {
        return false;
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
}
