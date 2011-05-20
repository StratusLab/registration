package eu.stratuslab.registration.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

public final class HashUtils {

    public static final String SSHA_PREFIX = "{SSHA}";

    public static final int SALT_LENGTH = 8;

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    private HashUtils() {

    }

    public static String sshaHash(String clearTextPassword) {

        byte[] salt = generateSalt();
        return sshaHash(clearTextPassword, salt);
    }

    public static String sshaHash(String clearTextPassword, byte[] salt) {

        try {

            byte[] password = convertPasswordToBytes(clearTextPassword);

            byte[] passwordAndSalt = fuseByteArrays(password, salt);

            byte[] digest = sha1Digest(passwordAndSalt);

            byte[] digestAndSalt = fuseByteArrays(digest, salt);

            String encodedDigestAndSalt = new String(Base64
                    .encode(digestAndSalt));

            return SSHA_PREFIX + encodedDigestAndSalt;

        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe(e.getMessage());
            return "";
        }
    }

    public static boolean comparePassword(String clearTextPassword,
            String sshaHashedPassword) {

        if (clearTextPassword == null || sshaHashedPassword == null) {
            return false;
        }

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

        String rehashedPassword = sshaHash(clearTextPassword, salt);

        return sshaHashedPassword.equals(rehashedPassword);
    }

    public static byte[] sha1Digest(byte[] bytes)
            throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(bytes);
        return digest.digest();
    }

    public static byte[] convertPasswordToBytes(String clearTextPassword) {
        return clearTextPassword.getBytes();
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom r = new SecureRandom();
        r.nextBytes(salt);
        return salt;
    }

    public static byte[] fuseByteArrays(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

}
