package com.rnbiometrics;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.nio.charset.Charset;

import javax.crypto.Cipher;

public class DecryptDataCallback extends BiometricPrompt.AuthenticationCallback {
    private Promise promise;
    private String payload;

    public DecryptDataCallback(Promise promise, String payload) {
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
            byte[] decrypted = cryptoCipher.doFinal(Base64.decode(payload, Base64.DEFAULT));
            String encoded = new String(decrypted, Charset.forName("UTF-8"));

            WritableMap resultMap = new WritableNativeMap();
            resultMap.putBoolean("success", true);
            resultMap.putString("decrypted", encoded);
            promise.resolve(resultMap);
        } catch (Exception e) {
            promise.reject("Error decrypting data: " + e.getMessage(), "Error decrypting data");
        }
    }
}
