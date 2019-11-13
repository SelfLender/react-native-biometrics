package com.rnbiometrics;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Promise;

public class SimplePromptCallback extends BiometricPrompt.AuthenticationCallback {
    private Promise promise;

    public SimplePromptCallback(Promise promise) {
        super();
        this.promise = promise;
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        this.promise.reject("Error authenticating biometrics" , "Error authenticating biometrics");
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        this.promise.resolve(true);
    }
}
