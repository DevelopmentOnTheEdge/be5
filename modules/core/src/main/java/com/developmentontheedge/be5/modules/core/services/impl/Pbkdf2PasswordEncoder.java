package com.developmentontheedge.be5.modules.core.services.impl;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Pbkdf2PasswordEncoder
{
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int iterations = 65536;
    private static final int desiredKeyLen = 128;

    public String encode(char[] rawPassword) throws Exception
    {
        byte[] salt = getSalt();
        return Base64.getEncoder().encodeToString(salt) + "$" + hash(rawPassword, salt);
    }

    public boolean check(char[] rawPassword, String stored) throws Exception
    {
        String[] saltAndHash = stored.split("\\$");
        if (saltAndHash.length != 2)
        {
            throw new IllegalStateException("The stored password must have the form 'salt$hash'");
        }
        String hashOfInput = hash(rawPassword, Base64.getDecoder().decode(saltAndHash[0]));
        return hashOfInput.equals(saltAndHash[1]);
    }

    private String hash(char[] rawPassword, byte[] salt) throws Exception
    {
        if (rawPassword.length == 0)
            throw new IllegalArgumentException("Empty passwords are not supported.");
        SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(rawPassword, salt, iterations, desiredKeyLen);
        byte[] encoded = f.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
}
