package com.armaninvestment.parsparandreporterapplication.utils;
public class NetworkUtils {

    /**
     * Extracts the hostname from a fully qualified domain name (FQDN).
     *
     * @param fqdn The full domain name, e.g., "Sadrag-pc.pishgaman.local".
     * @return The extracted hostname, e.g., "Sadrag-pc".
     */
    public static String extractHostname(String fqdn) {
        if (fqdn == null || fqdn.isEmpty()) {
            return ""; // Return an empty string if input is null or empty
        }
        int firstDotIndex = fqdn.indexOf('.');
        if (firstDotIndex == -1) {
            return fqdn; // Return the original string if no dot is found
        }
        return fqdn.substring(0, firstDotIndex);
    }
}

