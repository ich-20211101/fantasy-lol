package com.fantasylol.backend.util;

public final class PlayerNameSanitizer {

    private PlayerNameSanitizer() {}

    public static String sanitize(String name) {
        if (name != null && name.contains("(")) {
            return name.substring(0, name.indexOf("(")).trim();
        }
        return name;
    }

}
