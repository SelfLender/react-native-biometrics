package com.rnbiometrics;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.security.Signature;

import javax.crypto.Cipher;

public class EncryptDataCallback extends BiometricPrompt.AuthenticationCallback {
    private Promise promise;
    private String payload;

    public EncryptDataCallback(Promise promise, String payload) {
        super();
        this.promise = promise;
        this.payload = payload;
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
            BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
            Cipher cryptoCipher = cryptoObject.getCipher();
            byte[] encrypted = cryptoCipher.doFinal(this.payload.getBytes());
            String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT)
                    .replaceAll("\r", "")
                    .replaceAll("\n", "");
            String encodedIV = Base64.encodeToString(cryptoCipher.getIV(), Base64.DEFAULT);

            WritableMap resultMap = new WritableNativeMap();
            resultMap.putBoolean("success", true);
            resultMap.putString("encrypted", encryptedString);
            resultMap.putString("iv", encodedIV);
            promise.resolve(resultMap);
        } catch (Exception e) {
            promise.reject("Error encrypting data: " + e.getMessage(), "Error encrypting data");
        }
    }
}
