package org.landm.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.landm.service.HtmlEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class HtmlEmailServiceImpl implements HtmlEmailService {

    private static final Logger log = LoggerFactory.getLogger(HtmlEmailServiceImpl.class);

    private static final String FROM = "noreply@izdajemiznajmljujem.com";
    private static final String SITE_NAME = "Izdajem Iznajmljujem";
    private static final String COLOR_PRIMARY = "#813181";
    private static final String COLOR_BTN_HOVER = "#6a276a";

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

    public HtmlEmailServiceImpl(JavaMailSender mailSender, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
    }

    private String t(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private String htmlLang() {
        Locale l = LocaleContextHolder.getLocale();
        return "en".equalsIgnoreCase(l.getLanguage()) ? "en" : "sr";
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    @Override
    public void sendVerificationEmail(String to, String firstname, String verifyLink) {
        String subject = t("email.verification.subject", SITE_NAME);
        String body = greeting(firstname)
                + paragraph(t("email.verification.p1"))
                + paragraph(t("email.verification.p2"))
                + ctaButton(verifyLink, t("email.verification.cta"))
                + paragraph(t("email.verification.p3"));
        send(to, subject, wrap(body));
    }

    @Override
    public void sendPasswordResetEmail(String to, String firstname, String resetLink) {
        String subject = t("email.password_reset.subject", SITE_NAME);
        String body = greeting(firstname)
                + paragraph(t("email.password_reset.p1"))
                + paragraph(t("email.password_reset.p2"))
                + ctaButton(resetLink, t("email.password_reset.cta"));
        send(to, subject, wrap(body));
    }

    @Override
    public void sendContractRequestEmail(String ownerEmail, String ownerName, String lesseeName,
                                         String adTitle, String contractsUrl) {
        String subject = t("email.contract_request.subject", adTitle);
        String body = greeting(ownerName)
                + paragraph(t("email.contract_request.p1", "<strong>" + esc(lesseeName) + "</strong>"))
                + highlightBox(adTitle)
                + paragraph(t("email.contract_request.p2"))
                + ctaButton(contractsUrl, t("email.contract_request.cta"));
        send(ownerEmail, subject, wrap(body));
    }

    @Override
    public void sendContractAcceptedEmail(String lesseeEmail, String lesseeName, String adTitle,
                                          String contractsUrl) {
        String subject = t("email.contract_accepted.subject", adTitle);
        String body = greeting(lesseeName)
                + paragraph(t("email.contract_accepted.p1"))
                + highlightBox(adTitle)
                + paragraph(t("email.contract_accepted.p2"))
                + ctaButton(contractsUrl, t("email.contract_accepted.cta"));
        send(lesseeEmail, subject, wrap(body));
    }

    @Override
    public void sendContractRejectedEmail(String lesseeEmail, String lesseeName, String adTitle,
                                          String browseUrl) {
        String subject = t("email.contract_rejected.subject", adTitle);
        String body = greeting(lesseeName)
                + paragraph(t("email.contract_rejected.p1"))
                + highlightBox(adTitle)
                + paragraph(t("email.contract_rejected.p2"))
                + ctaButton(browseUrl, t("email.contract_rejected.cta"));
        send(lesseeEmail, subject, wrap(body));
    }

    @Override
    public void sendCreditAddedEmail(String to, String firstname, String amount, String newBalance,
                                     String description, String myAdsUrl) {
        String subject = t("email.credit_added.subject", SITE_NAME);
        String noteLine = (description != null && !description.isBlank())
                ? paragraph(t("email.credit_added.note", "<em>" + esc(description) + "</em>"))
                : "";
        String body = greeting(firstname)
                + paragraph(t("email.credit_added.p1", "<strong>" + esc(amount) + " RSD</strong>"))
                + noteLine
                + balanceRow(newBalance)
                + paragraph(t("email.credit_added.p2"))
                + ctaButton(myAdsUrl, t("email.credit_added.cta"));
        send(to, subject, wrap(body));
    }

    @Override
    public void sendAdExpiryReminderEmail(String to, String firstname, String adTitle,
                                          String expiryDate, String adUrl, String myAdsUrl) {
        String subject = t("email.ad_expiry.subject", adTitle);
        String body = greeting(firstname)
                + paragraph(t("email.ad_expiry.p1", "<strong>" + esc(expiryDate) + "</strong>"))
                + highlightBox(adTitle)
                + paragraph(t("email.ad_expiry.p2"))
                + twoButtons(myAdsUrl, t("email.ad_expiry.cta1"), adUrl, t("email.ad_expiry.cta2"));
        send(to, subject, wrap(body));
    }

    @Override
    public void sendVerificationApprovedEmail(String to, String firstname, String profileUrl) {
        String subject = t("email.verify_approved.subject", SITE_NAME);
        String body = greeting(firstname)
                + paragraph(t("email.verify_approved.p1"))
                + paragraph(t("email.verify_approved.p2"))
                + paragraph(t("email.verify_approved.p3"))
                + ctaButton(profileUrl, t("email.verify_approved.cta"));
        send(to, subject, wrap(body));
    }

    @Override
    public void sendVerificationRejectedEmail(String to, String firstname, String reason, String verifyUrl) {
        String subject = t("email.verify_rejected.subject", SITE_NAME);
        String body = greeting(firstname)
                + paragraph(t("email.verify_rejected.p1"))
                + paragraph(t("email.verify_rejected.reason_label"))
                + highlightBox(reason)
                + paragraph(t("email.verify_rejected.p2"))
                + ctaButton(verifyUrl, t("email.verify_rejected.cta"));
        send(to, subject, wrap(body));
    }

    // -------------------------------------------------------------------------
    // Private helpers — HTML building blocks
    // -------------------------------------------------------------------------

    private String greeting(String firstname) {
        return "<p style=\"margin:0 0 16px;font-size:16px;color:#222;\">"
                + t("email.common.greeting", "<strong>" + esc(firstname) + "</strong>")
                + "</p>";
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
                + "<span style=\"font-size:13px;color:#888;display:block;margin-bottom:4px;\">"
                + t("email.common.new_balance_label") + "</span>"
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

    private String wrap(String innerHtml) {
        return "<!DOCTYPE html>"
                + "<html lang=\"" + htmlLang() + "\">"
                + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + SITE_NAME + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f4f4;padding:32px 0;\">"
                + "<tr><td align=\"center\">"

                + "<table width=\"580\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:580px;width:100%;background:#fff;border-radius:10px;"
                + "overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"

                + "<tr><td style=\"background:" + COLOR_PRIMARY + ";padding:28px 40px;text-align:center;\">"
                + "<span style=\"font-size:22px;font-weight:700;color:#fff;letter-spacing:.5px;\">"
                + SITE_NAME + "</span>"
                + "</td></tr>"

                + "<tr><td style=\"padding:32px 40px;\">"
                + innerHtml
                + "</td></tr>"

                + "<tr><td style=\"padding:0 40px;\">"
                + "<hr style=\"border:none;border-top:1px solid #eee;margin:0;\">"
                + "</td></tr>"

                + "<tr><td style=\"padding:20px 40px;text-align:center;\">"
                + "<p style=\"margin:0;font-size:12px;color:#aaa;line-height:1.6;\">"
                + "&copy; " + SITE_NAME + " &middot; " + t("email.common.footer_note")
                + "</p></td></tr>"

                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

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
