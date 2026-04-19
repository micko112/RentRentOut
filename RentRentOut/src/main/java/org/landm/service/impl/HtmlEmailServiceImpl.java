package org.landm.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.landm.service.HtmlEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class HtmlEmailServiceImpl implements HtmlEmailService {

    private static final Logger log = LoggerFactory.getLogger(HtmlEmailServiceImpl.class);

    private static final String FROM = "izdajemiznajmljujem.rs@gmail.com";
    private static final String SITE_NAME = "Izdajem Iznajmljujem";
    private static final String COLOR_PRIMARY = "#813181";
    private static final String COLOR_BTN_HOVER = "#6a276a";

    private final JavaMailSender mailSender;

    public HtmlEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    @Override
    public void sendVerificationEmail(String to, String firstname, String verifyLink) {
        String subject = "Potvrdite vašu email adresu — " + SITE_NAME;
        String body = greeting(firstname)
                + paragraph("Hvala što ste se registrovali! Da biste počeli da koristite platformu, molimo vas da potvrdite svoju email adresu klikom na dugme ispod.")
                + paragraph("Link važi <strong>24 sata</strong>.")
                + ctaButton(verifyLink, "Potvrdi email adresu")
                + paragraph("Ukoliko niste kreirali nalog, možete ignorisati ovaj email.");
        send(to, subject, wrap(body));
    }

    @Override
    public void sendPasswordResetEmail(String to, String firstname, String resetLink) {
        String subject = "Resetovanje lozinke — " + SITE_NAME;
        String body = greeting(firstname)
                + paragraph("Primili smo zahtev za resetovanje lozinke za vaš nalog. Kliknite na dugme ispod da postavite novu lozinku.")
                + paragraph("Link važi <strong>24 sata</strong>. Ukoliko niste tražili reset lozinke, možete ignorisati ovaj email — vaša lozinka ostaje nepromenjena.")
                + ctaButton(resetLink, "Resetuj lozinku");
        send(to, subject, wrap(body));
    }

    @Override
    public void sendContractRequestEmail(String ownerEmail, String ownerName, String lesseeName,
                                         String adTitle, String contractsUrl) {
        String subject = "Nova zahtev za iznajmljivanje — " + adTitle;
        String body = greeting(ownerName)
                + paragraph("Korisnik <strong>" + esc(lesseeName) + "</strong> je poslao zahtev za iznajmljivanje vašeg oglasa:")
                + highlightBox(adTitle)
                + paragraph("Prijavite se na platformu da prihvatite ili odbijete zahtev.")
                + ctaButton(contractsUrl, "Pregledaj zahtev");
        send(ownerEmail, subject, wrap(body));
    }

    @Override
    public void sendContractAcceptedEmail(String lesseeEmail, String lesseeName, String adTitle,
                                          String contractsUrl) {
        String subject = "Vaš zahtev je prihvaćen — " + adTitle;
        String body = greeting(lesseeName)
                + paragraph("Odlične vesti! Vaš zahtev za iznajmljivanje oglasa je <strong>prihvaćen</strong>:")
                + highlightBox(adTitle)
                + paragraph("Kontaktirajte vlasnika oglasa porukama unutar platforme kako biste dogovorili detalje preuzimanja.")
                + ctaButton(contractsUrl, "Pogledaj ugovor");
        send(lesseeEmail, subject, wrap(body));
    }

    @Override
    public void sendContractRejectedEmail(String lesseeEmail, String lesseeName, String adTitle,
                                          String browseUrl) {
        String subject = "Vaš zahtev je odbijen — " + adTitle;
        String body = greeting(lesseeName)
                + paragraph("Nažalost, vaš zahtev za iznajmljivanje oglasa je <strong>odbijen</strong>:")
                + highlightBox(adTitle)
                + paragraph("Na platformi vas čekaju stotine drugih oglasa. Pronađite ono što vam treba!")
                + ctaButton(browseUrl, "Pregledaj oglase");
        send(lesseeEmail, subject, wrap(body));
    }

    @Override
    public void sendCreditAddedEmail(String to, String firstname, String amount, String newBalance,
                                     String description, String myAdsUrl) {
        String subject = "Kredit je dodat na vaš nalog — " + SITE_NAME;
        String noteLine = (description != null && !description.isBlank())
                ? paragraph("Napomena: <em>" + esc(description) + "</em>")
                : "";
        String body = greeting(firstname)
                + paragraph("Na vaš nalog je dodato <strong>" + esc(amount) + " RSD</strong> kredita.")
                + noteLine
                + balanceRow(newBalance)
                + paragraph("Kredit možete iskoristiti za promociju vaših oglasa — istaknite oglas na vrhu pretrage i privucite više zakupaca.")
                + ctaButton(myAdsUrl, "Promoviši oglas");
        send(to, subject, wrap(body));
    }

    @Override
    public void sendAdExpiryReminderEmail(String to, String firstname, String adTitle,
                                          String expiryDate, String adUrl, String myAdsUrl) {
        String subject = "Vaš oglas ističe za 3 dana — " + adTitle;
        String body = greeting(firstname)
                + paragraph("Vaš oglas ističe <strong>" + esc(expiryDate) + "</strong>:")
                + highlightBox(adTitle)
                + paragraph("Oglas možete <strong>besplatno obnoviti</strong> na stranici Moji oglasi kako bi ostao vidljiv zakupcima.")
                + twoButtons(myAdsUrl, "Obnovi oglas", adUrl, "Pogledaj oglas");
        send(to, subject, wrap(body));
    }

    @Override
    public void sendVerificationApprovedEmail(String to, String firstname, String profileUrl) {
        String subject = "Vaš nalog je verifikovan — " + SITE_NAME;
        String body = greeting(firstname)
                + paragraph("Odlične vesti! Vaš identitet je uspešno <strong>verifikovan</strong>.")
                + paragraph("Od sada pored vašeg imena stoji oznaka <strong>Verifikovan</strong>, "
                    + "što povećava poverenje drugih korisnika i šanse za uspešne dogovore.")
                + paragraph("Vaši dokumenti su bezbedno obrisani sa naših servera nakon pregleda — "
                    + "čuvamo samo informaciju da ste verifikovani.")
                + ctaButton(profileUrl, "Pogledaj profil");
        send(to, subject, wrap(body));
    }

    @Override
    public void sendVerificationRejectedEmail(String to, String firstname, String reason, String verifyUrl) {
        String subject = "Zahtev za verifikaciju je odbijen — " + SITE_NAME;
        String body = greeting(firstname)
                + paragraph("Nažalost, vaš zahtev za verifikaciju identiteta je <strong>odbijen</strong>.")
                + paragraph("Razlog:")
                + highlightBox(reason)
                + paragraph("Vaši dokumenti su obrisani sa naših servera. Možete ponovo pokrenuti "
                    + "proces verifikacije sa ispravnim dokumentima u bilo kom trenutku.")
                + ctaButton(verifyUrl, "Pokreni ponovo");
        send(to, subject, wrap(body));
    }

    // -------------------------------------------------------------------------
    // Private helpers — HTML building blocks
    // -------------------------------------------------------------------------

    private String greeting(String firstname) {
        return "<p style=\"margin:0 0 16px;font-size:16px;color:#222;\">Poštovani/a <strong>"
                + esc(firstname) + "</strong>,</p>";
    }

    private String paragraph(String html) {
        return "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:#444;\">"
                + html + "</p>";
    }

    private String highlightBox(String text) {
        return "<div style=\"margin:0 0 20px;padding:14px 18px;background:#f5ecff;"
                + "border-left:4px solid " + COLOR_PRIMARY + ";border-radius:4px;"
                + "font-size:15px;font-weight:600;color:#333;\">"
                + esc(text) + "</div>";
    }

    private String balanceRow(String balance) {
        return "<div style=\"margin:0 0 20px;padding:16px 20px;background:#f9f9f9;"
                + "border-radius:8px;text-align:center;\">"
                + "<span style=\"font-size:13px;color:#888;display:block;margin-bottom:4px;\">Novo stanje kredita</span>"
                + "<span style=\"font-size:28px;font-weight:700;color:" + COLOR_PRIMARY + ";\">"
                + esc(balance) + " RSD</span></div>";
    }

    private String ctaButton(String url, String label) {
        return "<div style=\"text-align:center;margin:24px 0;\">"
                + "<a href=\"" + esc(url) + "\" style=\"display:inline-block;padding:13px 32px;"
                + "background:" + COLOR_PRIMARY + ";color:#fff;text-decoration:none;"
                + "font-size:15px;font-weight:600;border-radius:8px;\">"
                + esc(label) + "</a></div>";
    }

    private String twoButtons(String url1, String label1, String url2, String label2) {
        String btn1 = "<a href=\"" + esc(url1) + "\" style=\"display:inline-block;padding:12px 24px;"
                + "background:" + COLOR_PRIMARY + ";color:#fff;text-decoration:none;"
                + "font-size:14px;font-weight:600;border-radius:8px;margin:4px;\">"
                + esc(label1) + "</a>";
        String btn2 = "<a href=\"" + esc(url2) + "\" style=\"display:inline-block;padding:12px 24px;"
                + "background:#fff;color:" + COLOR_PRIMARY + ";text-decoration:none;"
                + "font-size:14px;font-weight:600;border-radius:8px;margin:4px;"
                + "border:2px solid " + COLOR_PRIMARY + ";\">"
                + esc(label2) + "</a>";
        return "<div style=\"text-align:center;margin:24px 0;\">" + btn1 + btn2 + "</div>";
    }

    /**
     * Wraps inner body HTML in a complete, email-client-safe HTML document
     * with purple header, white card content, and gray footer.
     */
    private String wrap(String innerHtml) {
        return "<!DOCTYPE html>"
                + "<html lang=\"sr\">"
                + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + SITE_NAME + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f4f4;padding:32px 0;\">"
                + "<tr><td align=\"center\">"

                // Card
                + "<table width=\"580\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:580px;width:100%;background:#fff;border-radius:10px;"
                + "overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"

                // Header
                + "<tr><td style=\"background:" + COLOR_PRIMARY + ";padding:28px 40px;text-align:center;\">"
                + "<span style=\"font-size:22px;font-weight:700;color:#fff;letter-spacing:.5px;\">"
                + SITE_NAME + "</span>"
                + "</td></tr>"

                // Body
                + "<tr><td style=\"padding:32px 40px;\">"
                + innerHtml
                + "</td></tr>"

                // Divider
                + "<tr><td style=\"padding:0 40px;\">"
                + "<hr style=\"border:none;border-top:1px solid #eee;margin:0;\">"
                + "</td></tr>"

                // Footer
                + "<tr><td style=\"padding:20px 40px;text-align:center;\">"
                + "<p style=\"margin:0;font-size:12px;color:#aaa;line-height:1.6;\">"
                + "© " + SITE_NAME + " · Ovaj email je poslat automatski, molimo ne odgovarajte na njega."
                + "</p></td></tr>"

                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    /** Escapes HTML special characters to prevent injection. */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
