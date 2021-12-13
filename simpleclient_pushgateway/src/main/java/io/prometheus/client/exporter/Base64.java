package io.prometheus.client.exporter;

import javax.xml.bind.DatatypeConverter;

/**
 * This class delegates to either javax.xml.bind.DatatypeConverter (for Java &lt; 8) or java.util.Base64 (Java 8+)
 * to perform Base64 encoding of a String.
 *
 * This code requires Java 8+ for compilation.
 */
public class Base64 {

    private static final boolean HAS_JAVA_UTIL_BASE64 = hasJavaUtilBase64();

    private static boolean hasJavaUtilBase64() {
        try {
            Class.forName("java.util.Base64");
            // Java 8+
            return true;
        } catch (Throwable t) {
            // Java < 8
            return false;
        }
    }

    private Base64() {}

    /**
     * Encodes a byte[] to a String using Base64.
     *
     * Passing a null argument will cause a NullPointerException to be thrown.
     *
     * @param src string to be encoded
     * @return String in Base64 encoding
     */
    @SuppressWarnings("all")
    public static String encodeToString(byte[] src) {
        if (src == null) {
            throw new NullPointerException();
        }

        if (HAS_JAVA_UTIL_BASE64) {
            return java.util.Base64.getEncoder().encodeToString(src);
        } else {
            return DatatypeConverter.printBase64Binary(src);
        }
    }
}
