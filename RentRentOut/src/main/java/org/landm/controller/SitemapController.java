package org.landm.controller;

import org.landm.repository.AdRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SitemapController {

    // path → [changefreq, priority]
    private static final Map<String, String[]> STATIC_PAGES = new LinkedHashMap<>();
    static {
        STATIC_PAGES.put("",                  new String[]{"daily",   "1.0"});
        STATIC_PAGES.put("/ads",              new String[]{"daily",   "0.9"});
        STATIC_PAGES.put("/how-it-works",     new String[]{"monthly", "0.7"});
        STATIC_PAGES.put("/contact",          new String[]{"monthly", "0.6"});
        STATIC_PAGES.put("/terms-of-service", new String[]{"monthly", "0.4"});
        STATIC_PAGES.put("/privacy-policy",   new String[]{"monthly", "0.4"});
    }

    private final AdRepository adRepository;

    @Value("${app.frontend.base-url:https://izdajemiznajmljujem.com}")
    private String baseUrl;

    public SitemapController(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        String today = LocalDate.now().toString();
        List<Object[]> ads = adRepository.findAllActiveIdsWithDate();

        StringBuilder sb = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                """);

        for (Map.Entry<String, String[]> entry : STATIC_PAGES.entrySet()) {
            String[] meta = entry.getValue();
            sb.append("  <url>\n");
            sb.append("    <loc>").append(baseUrl).append(entry.getKey()).append("</loc>\n");
            sb.append("    <lastmod>").append(today).append("</lastmod>\n");
            sb.append("    <changefreq>").append(meta[0]).append("</changefreq>\n");
            sb.append("    <priority>").append(meta[1]).append("</priority>\n");
            sb.append("  </url>\n");
        }

        for (Object[] row : ads) {
            Long id = (Long) row[0];
            LocalDateTime createdAt = (LocalDateTime) row[1];
            String lastmod = createdAt != null ? createdAt.toLocalDate().toString() : today;

            sb.append("  <url>\n");
            sb.append("    <loc>").append(baseUrl).append("/ads/").append(id).append("</loc>\n");
            sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("</urlset>");
        return sb.toString();
    }
}
