package app.Util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashUtil {

    public static String hashString(String input) {
        return BCrypt.withDefaults().hashToString(12, input.toCharArray());
    }

    public static boolean verifyHashString(String input, String hashStr) {
        return !BCrypt.verifyer().verify(input.toCharArray(), hashStr).verified;
    }
}
