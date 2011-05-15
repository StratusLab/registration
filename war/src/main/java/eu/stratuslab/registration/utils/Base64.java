package eu.stratuslab.registration.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

public class Base64 {

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    public static byte[] encode(byte[] b) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = null;
        try {
            os = MimeUtility.encode(baos, "base64");
            os.write(b);
            os.flush();
        } catch (MessagingException e) {
            LOGGER.severe(e.getMessage());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        } finally {
            closeReliably(os);
        }
        return baos.toByteArray();
    }

    public static byte[] decode(String s) {
        return decode(s.getBytes());
    }

    public static byte[] decode(byte[] b) {
        byte[] buffer = new byte[1024];

        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {

            is = MimeUtility.decode(bais, "base64");

            for (int n = is.read(buffer); n > 0; n = is.read(buffer)) {
                baos.write(buffer, 0, n);
            }

        } catch (MessagingException e) {
            LOGGER.severe(e.getMessage());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        } finally {
            closeReliably(is);
            closeReliably(baos);
        }

        return baos.toByteArray();
    }

    private static void closeReliably(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
        }

    }

}