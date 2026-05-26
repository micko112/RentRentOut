package org.landm.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlSanitizerTest {

    // ─── sanitize() — strips ALL HTML/JS ─────────────────────────────────

    @Test
    void sanitize_nullInput_returnsNull() {
        assertThat(HtmlSanitizer.sanitize(null)).isNull();
    }

    @Test
    void sanitize_plainText_isReturnedUnchanged() {
        String input = "Pozdrav, ovo je obican tekst.";
        assertThat(HtmlSanitizer.sanitize(input)).isEqualTo(input);
    }

    @Test
    void sanitize_scriptTag_isStripped() {
        // Classic XSS attack vector
        String malicious = "<script>alert('XSS')</script>Hello";
        String result = HtmlSanitizer.sanitize(malicious);
        assertThat(result).doesNotContain("<script>");
        assertThat(result).doesNotContain("alert");
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    void sanitize_imgWithOnerror_isStripped() {
        // Image-based XSS
        String malicious = "<img src=x onerror=alert('XSS')>";
        String result = HtmlSanitizer.sanitize(malicious);
        assertThat(result).doesNotContain("<img");
        assertThat(result).doesNotContain("onerror");
    }

    @Test
    void sanitize_allHtmlTags_areStripped() {
        String input = "<b>bold</b> and <i>italic</i> and <a href='evil.com'>link</a>";
        String result = HtmlSanitizer.sanitize(input);
        assertThat(result).doesNotContain("<");
        assertThat(result).doesNotContain(">");
        // Text content remains
        assertThat(result).contains("bold").contains("italic").contains("link");
    }

    // ─── sanitizeRichText() — allows limited safe tags ───────────────────

    @Test
    void sanitizeRichText_allowedTags_arePreserved() {
        String input = "<strong>important</strong> text with <em>emphasis</em>";
        String result = HtmlSanitizer.sanitizeRichText(input);
        assertThat(result).contains("<strong>").contains("</strong>");
        assertThat(result).contains("<em>").contains("</em>");
    }

    @Test
    void sanitizeRichText_scriptTag_isStrippedEvenInRichText() {
        // Script must NEVER be allowed, even in rich text mode
        String malicious = "<strong>safe</strong><script>alert('XSS')</script>";
        String result = HtmlSanitizer.sanitizeRichText(malicious);
        assertThat(result).doesNotContain("<script>");
        assertThat(result).doesNotContain("alert");
        assertThat(result).contains("<strong>safe</strong>");
    }
}
