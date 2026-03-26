package org.landm.entity.Enums;

public enum TransactionType {
    /** Admin ručno dodaje kredit (verifikacija uplate) */
    TOPUP_ADMIN,
    /** Kupovina promocije */
    PROMOTION_PURCHASE,
    /** Ručna korekcija od strane admina */
    ADMIN_ADJUSTMENT
}
