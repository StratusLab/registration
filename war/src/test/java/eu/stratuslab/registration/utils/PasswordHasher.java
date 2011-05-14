package eu.stratuslab.registration.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHasher {

    public static final String SSHA_PREFIX = "{SSHA}";

    public static final int SALT_LENGTH = 8;

    public String hashPasswordWithSSHA(String clearTextPassword)
            throws NoSuchAlgorithmException {

        byte[] salt = generateSalt();
        return hashPasswordWithSSHA(clearTextPassword, salt);
    }

    public String hashPasswordWithSSHA(String clearTextPassword, byte[] salt)
            throws NoSuchAlgorithmException {

        byte[] password = convertPasswordToBytes(clearTextPassword);

        byte[] passwordAndSalt = fuseByteArrays(password, salt);

        byte[] digest = sha1Digest(passwordAndSalt);

        byte[] digestAndSalt = fuseByteArrays(digest, salt);

        String encodedDigestAndSalt = Base64.encodeBytes(digestAndSalt);

        return SSHA_PREFIX + encodedDigestAndSalt;
    }

    public boolean comparePassword(String clearTextPassword,
            String sshaHashedPassword) throws NoSuchAlgorithmException {

        if (!sshaHashedPassword.startsWith(SSHA_PREFIX)) {
            throw new IllegalArgumentException(
                    "hashed password does not start with '" + SSHA_PREFIX + "'");
        }

        String encodedDigestAndSalt = sshaHashedPassword.substring(SSHA_PREFIX
                .length());

        byte[] digestAndSalt = Base64.decode(encodedDigestAndSalt);

        byte[] salt = new byte[SALT_LENGTH];

        System.arraycopy(digestAndSalt, digestAndSalt.length - SALT_LENGTH,
                salt, 0, SALT_LENGTH);

        String rehashedPassword = hashPasswordWithSSHA(clearTextPassword, salt);

        return sshaHashedPassword.equals(rehashedPassword);
    }

    public byte[] sha1Digest(byte[] bytes) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(bytes);
        return digest.digest();
    }

    public byte[] convertPasswordToBytes(String clearTextPassword) {
        return clearTextPassword.getBytes();
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom r = new SecureRandom();
        r.nextBytes(salt);
        return salt;
    }

    public byte[] fuseByteArrays(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

}
