package com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.util;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by stefan.marx on 22.04.16.
 */
public class KeyStoreWrapper {
    private final KeyStore _ks;
    private final KeyStore.PasswordProtection _keyStorePP;
    private String _location;
    private String _password;

    public KeyStoreWrapper(String location, String password) throws Exception {
        _location = location;
        _password = password;
        _ks = createKeyStore(location,password);
        _keyStorePP = new KeyStore.PasswordProtection(password.toCharArray());



    }

    private static KeyStore createKeyStore(String fileName, String pw) throws Exception {
        File file = new File(fileName);

        final KeyStore keyStore = KeyStore.getInstance("JCEKS");
        if (file.exists()) {
            // .keystore file already exists => load it
            keyStore.load(new FileInputStream(file), pw.toCharArray());
        } else {
            // .keystore file not created yet => create it
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(fileName), pw.toCharArray());
        }

        return keyStore;
    }

    public void setPasswd(String key, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret =
                factory.generateSecret(new PBEKeySpec(
                        password.toCharArray()));

        _ks.setEntry(key, new KeyStore.SecretKeyEntry(
                generatedSecret), _keyStorePP);
    }

    public void store() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        _ks.store(new FileOutputStream(_location),_password.toCharArray());
    }

    public String getPasswd(String key) throws InvalidKeySpecException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        KeyStore.SecretKeyEntry ske =
                (KeyStore.SecretKeyEntry)_ks.getEntry(key, _keyStorePP);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");

        PBEKeySpec keySpec = (PBEKeySpec)factory.getKeySpec(
                ske.getSecretKey(),
                PBEKeySpec.class);

        char[] password = keySpec.getPassword();
        return String.valueOf(password);
    }
}
