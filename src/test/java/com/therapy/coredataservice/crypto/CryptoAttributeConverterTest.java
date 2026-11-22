package com.therapy.coredataservice.crypto;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoAttributeConverterTest {

    private static final String TEST_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final String OTHER_KEY = Base64.getEncoder().encodeToString("abcdefghijklmnopqrstuvwxyz012345".getBytes());

    private final CryptoAttributeConverter converter = new CryptoAttributeConverter(new CryptoService(TEST_KEY));

    @Test
    void hebrewTextIsEncryptedToUnreadableValueAndDecryptedBackExactly() {
        String plain = "נועם כהן, כיתה ג', חרדה חברתית בהפסקות 🚀";

        String dbValue = converter.convertToDatabaseColumn(plain);

        assertThat(dbValue).isNotNull().isNotEqualTo(plain);
        assertThat(dbValue).doesNotContain("נועם").doesNotContain("כהן");
        assertThat(Base64.getDecoder().decode(dbValue)).hasSizeGreaterThan(12 + 16);
        assertThat(converter.convertToEntityAttribute(dbValue)).isEqualTo(plain);
    }

    @Test
    void sameTextProducesDifferentCipherTextEachTimeBecauseOfRandomIv() {
        String plain = "מגיב מצוין להומור עדין";

        String first = converter.convertToDatabaseColumn(plain);
        String second = converter.convertToDatabaseColumn(plain);

        assertThat(first).isNotEqualTo(second);
        assertThat(converter.convertToEntityAttribute(first)).isEqualTo(plain);
        assertThat(converter.convertToEntityAttribute(second)).isEqualTo(plain);
    }

    @Test
    void nullIsKeptAsNullInBothDirections() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void emptyStringRoundTrips() {
        String dbValue = converter.convertToDatabaseColumn("");
        assertThat(dbValue).isNotEmpty();
        assertThat(converter.convertToEntityAttribute(dbValue)).isEmpty();
    }

    @Test
    void tamperedCipherTextFailsIntegrityCheck() {
        byte[] payload = Base64.getDecoder().decode(converter.convertToDatabaseColumn("סוד"));
        payload[payload.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(payload);

        assertThatThrownBy(() -> converter.convertToEntityAttribute(tampered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("integrity");
    }

    @Test
    void valueEncryptedWithDifferentKeyCannotBeDecrypted() {
        String dbValue = new CryptoService(OTHER_KEY).encrypt("פרטים אישיים");

        assertThatThrownBy(() -> converter.convertToEntityAttribute(dbValue))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void keyThatIsNot32BytesIsRejected() {
        String shortKey = Base64.getEncoder().encodeToString("too-short".getBytes());

        assertThatThrownBy(() -> new CryptoService(shortKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void invalidBase64InDbIsRejected() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("not base64 !!!"))
                .isInstanceOf(IllegalStateException.class);
    }
}
