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
        return Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash(rawPassword, salt));
    }

    public boolean check(char[] rawPassword, String stored) throws Exception
    {
        String[] saltAndHash = stored.split("\\$");
        if (saltAndHash.length != 2)
        {
            throw new IllegalStateException("The stored password must have the form 'salt$hash'");
        }
        byte[] salt = Base64.getDecoder().decode(saltAndHash[0]);
        byte[] hash = Base64.getDecoder().decode(saltAndHash[1]);
        byte[] hashOfInput = hash(rawPassword, salt);
        return slowEquals(hashOfInput, hash);
    }

    private byte[] hash(char[] rawPassword, byte[] salt) throws Exception
    {
        if (rawPassword.length == 0)
            throw new IllegalArgumentException("Empty passwords are not supported.");
        SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(rawPassword, salt, iterations, desiredKeyLen);
        return f.generateSecret(spec).getEncoded();
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        // according to suggestion
        // https://tersesystems.com/blog/2015/12/17/the-right-way-to-use-securerandom/ 
        //SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
        {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
