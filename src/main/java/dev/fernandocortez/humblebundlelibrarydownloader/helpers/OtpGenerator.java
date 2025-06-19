package dev.fernandocortez.humblebundlelibrarydownloader.helpers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OtpGenerator {

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param hex The hexadecimal string.
     * @return The byte array.
     */
    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Computes the HMAC-SHA1 digest of a message using a given key.
     *
     * @param message The message in hexadecimal string format.
     * @param key The key in hexadecimal string format.
     * @return The HMAC-SHA1 digest as a hexadecimal string.
     */
    private static String computeHmacSha1(String message, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexToBytes(key), "HmacSHA1");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(hexToBytes(message));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error computing HMAC-SHA1: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Base32 string to a hexadecimal string. This implementation adheres to RFC 4648 for
     * Base32 encoding.
     *
     * @param base32 The Base32 string.
     * @return The hexadecimal string.
     */
    private static String base32toHex(String base32) {
        final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        // Remove padding characters and convert to uppercase
        String normalizedBase32 = base32.toUpperCase().replaceAll("=+$", "");

        StringBuilder bits = new StringBuilder();
        for (char c : normalizedBase32.toCharArray()) {
            int value = base32Chars.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }
            bits.append(String.format("%5s", Integer.toBinaryString(value)).replace(' ', '0'));
        }

        StringBuilder hex = new StringBuilder();
        // Process bits in chunks of 8
        for (int i = 0; i < bits.length(); i += 8) {
            String chunk;
            if (i + 8 <= bits.length()) {
                chunk = bits.substring(i, i + 8);
            } else {
                // Pad with zeros if the last chunk is less than 8 bits
                chunk = bits.substring(i) + "0".repeat(8 - (bits.length() - i));
            }
            if (!chunk.isEmpty()) {
                hex.append(String.format("%02x", Integer.parseInt(chunk, 2)));
            }
        }
        return hex.toString();
    }

    /**
     * Generates a Hash-based One-Time Password (HOTP).
     *
     * @param key The secret key (Base32 encoded).
     * @param counter The counter value.
     * @return The generated HOTP.
     */
    private static String generateHOTP(String key, long counter) {
        // Convert counter to hex string, padded to 16 characters
        String counterHex = String.format("%016x", counter);

        // Compute HMACdigest
        String digest = computeHmacSha1(counterHex, base32toHex(key));

        // Get byte array
        byte[] bytes = hexToBytes(digest);

        // Truncate
        int offset = bytes[19] & 0xf;
        int v = ((bytes[offset] & 0x7f) << 24) | ((bytes[offset + 1] & 0xff) << 16)
                | ((bytes[offset + 2] & 0xff) << 8) | (bytes[offset + 3] & 0xff);

        return String.format("%06d", v % 1000000);
    }

    /**
     * Calculates the counter value based on the current time and time step.
     *
     * @param now The current time in milliseconds.
     * @param timeStep The time step in seconds.
     * @return The counter value.
     */
    private static long getCounterFromTime(long now, int timeStep) {
        return (long) Math.floor((double) now / 1000 / timeStep);
    }

    /**
     * Generates a Time-based One-Time Password (TOTP).
     *
     * @param key The secret key (Base32 encoded).
     * @param now The current time in milliseconds (defaults to System.currentTimeMillis()).
     * @param timeStep The time step in seconds (defaults to 30).
     * @return The generated TOTP.
     */
    public static String generateTOTP(String key, long now, int timeStep) {
        long counter = getCounterFromTime(now, timeStep);
        return generateHOTP(key, counter);
    }

    /**
     * Generates a Time-based One-Time Password (TOTP) using default values for now and timeStep.
     *
     * @param key The secret key (Base32 encoded).
     * @return The generated TOTP.
     */
    public static String generateTOTP(String key) {
        return generateTOTP(key, System.currentTimeMillis(), 30);
    }
}
