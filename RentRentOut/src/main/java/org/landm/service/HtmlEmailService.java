package org.landm.service;

public interface HtmlEmailService {
    void sendVerificationEmail(String to, String firstname, String verifyLink);
    void sendPasswordResetEmail(String to, String firstname, String resetLink);
    void sendContractRequestEmail(String ownerEmail, String ownerName, String lesseeName, String adTitle, String contractsUrl);
    void sendContractAcceptedEmail(String lesseeEmail, String lesseeName, String adTitle, String contractsUrl);
    void sendContractRejectedEmail(String lesseeEmail, String lesseeName, String adTitle, String browseUrl);
    void sendCreditAddedEmail(String to, String firstname, String amount, String newBalance, String description, String myAdsUrl);
    void sendAdExpiryReminderEmail(String to, String firstname, String adTitle, String expiryDate, String adUrl, String myAdsUrl);
    void sendVerificationApprovedEmail(String to, String firstname, String profileUrl);
    void sendVerificationRejectedEmail(String to, String firstname, String reason, String verifyUrl);
}
