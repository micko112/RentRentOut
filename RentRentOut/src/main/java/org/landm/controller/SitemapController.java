package org.landm.controller;

import org.landm.repository.AdRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class SitemapController {

    private static final List<String> STATIC_PATHS = List.of(
        "",
        "/ads",
        "/privacy-policy",
        "/terms-of-service",
        "/login",
        "/register"
    );

    private final AdRepository adRepository;

    @Value("${app.frontend.base-url:https://izdajemiznajmljujem.com}")
    private String baseUrl;

    public SitemapController(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        String today = LocalDate.now().toString();
        List<Long> adIds = adRepository.findAllActiveIds();

        StringBuilder sb = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                """);

        for (String path : STATIC_PATHS) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(baseUrl).append(path).append("</loc>\n");
            sb.append("    <lastmod>").append(today).append("</lastmod>\n");
            sb.append("    <changefreq>daily</changefreq>\n");
            sb.append("    <priority>").append(path.isEmpty() ? "1.0" : "0.8").append("</priority>\n");
            sb.append("  </url>\n");
        }

        for (Long id : adIds) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(baseUrl).append("/ads/").append(id).append("</loc>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.6</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("</urlset>");
        return sb.toString();
    }
}
