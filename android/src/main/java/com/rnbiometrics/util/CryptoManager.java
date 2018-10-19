package com.rnbiometrics.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.RSAKeyGenParameterSpec;

public class CryptoManager {
    private String keyAlias;
    private String keyStoreType;
    private String signatureAlgorithm;
    private KeyPair keyPair;

    public CryptoManager(String keyAlias, String keyStoreType, String signatureAlgorithm) {
        this.keyAlias = keyAlias;
        this.keyStoreType = keyStoreType;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public boolean deleteSignature() {
        try {
            getKeyStore().deleteEntry(keyAlias);
            return true;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            return false;
        }
    }

    public String sign(FingerprintManagerCompat.CryptoObject cryptoObject, String payload) throws SignatureException {
        Signature cryptoSignature = cryptoObject.getSignature();
        cryptoSignature.update(payload.getBytes());
        byte[] signed = cryptoSignature.sign();
        String signedString = Base64.encodeToString(signed, Base64.DEFAULT);
        return signedString.replaceAll("\r", "").replaceAll("\n", "");
    }

    public Signature getSignature() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, IOException {
        return CryptoUtils.getSignature(keyAlias, signatureAlgorithm, keyStoreType);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void InitializeKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException {
        getKeyStore().deleteEntry(keyAlias);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                keyStoreType);
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                .setUserAuthenticationRequired(true)
                .build();
        keyPairGenerator.initialize(keyGenParameterSpec);

        keyPair = keyPairGenerator.generateKeyPair();
    }

    public KeyStore getKeyStore() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return CryptoUtils.getKeystore(keyStoreType);
    }

    public String getPublicKeyBase64EncodedString() {

        PublicKey publicKey = keyPair.getPublic();
        byte[] encodedPublicKey = publicKey.getEncoded();
        String publicKeyString = Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);
        return publicKeyString.replaceAll("\r", "").replaceAll("\n", "");
    }
}
