package org.landm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class PhoneNumberConverterTest {

    private static final String TEST_KEY = "0123456789abcdef0123456789abcdef";

    private PhoneNumberConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PhoneNumberConverter();
        converter.setKey(TEST_KEY);
    }

    @Test
    void convertToDatabaseColumn_nullInput_returnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }


    @Test
    void roundTrip_originalNumberIsReturned() {
        String original = "+381641234567";
        String encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(original);
        assertThat(encrypted).isNotEqualTo(original); // proof it was encrypted
    }



    @Test
    void encryption_sameInput_producesDifferentCiphertexts() {
        // Random IV must produce different ciphertexts for the same plaintext
        String a = converter.convertToDatabaseColumn("+381641234567");
        String b = converter.convertToDatabaseColumn("+381641234567");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void backwardCompatibility_plaintextFromDb_isReturnedUnchanged() {
        // Legacy numbers in the database are plaintext (pre-encryption) — must be returned untouched
        String legacy = "0641234567";
        assertThat(converter.convertToEntityAttribute(legacy)).isEqualTo(legacy);
    }
}
