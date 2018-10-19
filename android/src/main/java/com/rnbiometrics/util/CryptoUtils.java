package com.rnbiometrics.util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class CryptoUtils {

    public static KeyStore getKeystore(String keystoreType) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        keyStore.load(null);
        return keyStore;
    }

    public static Signature getSignature(String alias, String signatureAlgorithm, String keystoreType) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException {
        Signature signature = Signature.getInstance(signatureAlgorithm);
        KeyStore keyStore = getKeystore(keystoreType);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
        signature.initSign(privateKey);
        return signature;
    }

}
