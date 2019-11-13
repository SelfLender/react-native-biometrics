package com.rnbiometrics;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Promise;

import java.security.Signature;

public class CreateSignatureCallback extends BiometricPrompt.AuthenticationCallback {
    private Promise promise;
    private String payload;

    public CreateSignatureCallback(Promise promise, String payload) {
        super();
        this.promise = promise;
        this.payload = payload;
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        this.promise.reject("Error authenticating biometrics" , "Error authenticating biometrics");
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        try {
            BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
            Signature cryptoSignature = cryptoObject.getSignature();
            cryptoSignature.update(this.payload.getBytes());
            byte[] signed = cryptoSignature.sign();
            String signedString = Base64.encodeToString(signed, Base64.DEFAULT);
            signedString = signedString.replaceAll("\r", "").replaceAll("\n", "");
            promise.resolve(signedString);
        } catch (Exception e) {
            promise.reject("Error creating signature: " + e.getMessage(), "Error creating signature");
        }
    }
}
