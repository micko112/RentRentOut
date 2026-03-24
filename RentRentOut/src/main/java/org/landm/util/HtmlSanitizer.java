package org.landm.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlSanitizer {

    private HtmlSanitizer() {}

    /**
     * Uklanja sve HTML i JS tagove iz korisničkog unosa.
     * Štiti od XSS napada.
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
    }
}
