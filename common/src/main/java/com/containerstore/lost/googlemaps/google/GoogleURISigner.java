package com.containerstore.lost.googlemaps.google;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class GoogleURISigner {
    private static final Logger LOG = Logger.getLogger(GoogleURISigner.class);

    // TODO: as of now we do not have a Google account. We need to get the key set up before using the
    // TODO: Google mapping domain in production. This is a Google-provided class for adding information to our
    // TODO: URL based on our private key and signature.

    // Note: Generally, you should store your private key someplace safe
    // and read them into your code
    //private static String keyString = "YOUR_PRIVATE_KEY";

    // The URL shown in these examples must be already
    // URL-encoded. In practice, you will likely have code
    // which assembles your URL from user or web service input
    // and plugs those values into its parameters.
    //private static String urlString = "YOUR_URL_TO_SIGN";

    // This variable stores the binary key, which is computed from the string (Base64) key
    private byte[] key;

    public GoogleURISigner(String keyString) throws IOException {
        String fixedKey = keyString;
        // Convert the key from 'web safe' base 64 to binary
        fixedKey = fixedKey.replace('-', '+');
        fixedKey = fixedKey.replace('_', '/');
        this.key = Base64.decode(fixedKey);
    }

    public String signRequest(String path, String query) throws NoSuchAlgorithmException,
            InvalidKeyException, UnsupportedEncodingException, URISyntaxException {

        // Retrieve the proper URL components to sign
        String resource = path + '?' + query;

        // Get an HMAC-SHA1 signing key from the raw key bytes
        SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1");

        // Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1 key
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(sha1Key);

        // compute the binary signature for the request
        byte[] sigBytes = mac.doFinal(resource.getBytes());

        // base 64 encode the binary signature
        String signature = Base64.encode(sigBytes);

        // convert the signature to 'web safe' base 64
        signature = signature.replace('+', '-');
        signature = signature.replace('/', '_');

        LOG.info("Signed Google request: " + resource + "&signature=" + signature);
        return resource + "&signature=" + signature;
    }
}
