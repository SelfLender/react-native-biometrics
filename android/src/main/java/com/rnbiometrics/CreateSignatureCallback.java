package com.rnbiometrics;

import static com.rnbiometrics.ReactNativeBiometrics.biometricKeyAlias;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class CreateSignatureCallback extends BiometricPrompt.AuthenticationCallback {
    private Promise promise;
    private String payload;
    private boolean allowDeviceCredentials;

    public CreateSignatureCallback(Promise promise, String payload, boolean allowDeviceCredentials) {
        super();
        this.promise = promise;
        this.payload = payload;
        this.allowDeviceCredentials = allowDeviceCredentials;
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED ) {
            WritableMap resultMap = new WritableNativeMap();
            resultMap.putBoolean("success", false);
            resultMap.putString("error", "User cancellation");
            this.promise.resolve(resultMap);
        } else {
            this.promise.reject(errString.toString(), errString.toString());
        }
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        try {
            Signature cryptoSignature = getSignature(result);
            cryptoSignature.update(this.payload.getBytes());
            byte[] signed = cryptoSignature.sign();
            String signedString = Base64.encodeToString(signed, Base64.DEFAULT);
            signedString = signedString.replaceAll("\r", "").replaceAll("\n", "");

            WritableMap resultMap = new WritableNativeMap();
            resultMap.putBoolean("success", true);
            resultMap.putString("signature", signedString);
            promise.resolve(resultMap);
        } catch (Exception e) {
            promise.reject("Error creating signature: " + e.getMessage(), "Error creating signature");
        }
    }

    @Nullable
    private Signature getSignature(@NonNull BiometricPrompt.AuthenticationResult result) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException {
        if (this.allowDeviceCredentials) {
            Signature signature = Signature.getInstance("SHA256withRSA");
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(biometricKeyAlias, null);
            signature.initSign(privateKey);
            return signature;
        }

        BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
        return cryptoObject.getSignature();
    }
}
