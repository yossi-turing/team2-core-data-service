package com.therapy.coredataservice.crypto;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * מוודא שההצפנה עובדת דרך Hibernate: בעמודה ב-DB נשמר ערך מוצפן, ובישות מתקבל הטקסט המקורי.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CryptoAttributeConverterJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void fieldIsStoredEncryptedInDatabaseAndReadBackDecrypted() {
        String secret = "רקע רגשי: חרדה חברתית, נטייה להימנעות ממשחקים קבוצתיים";
        EncryptedTestRecord record = new EncryptedTestRecord();
        record.secretText = secret;
        entityManager.persist(record);
        entityManager.flush();
        entityManager.clear();

        String rawColumn = jdbcTemplate.queryForObject(
                "SELECT secret_text FROM crypto_test_records WHERE id = ?", String.class, record.id);
        assertThat(rawColumn).isNotEqualTo(secret).doesNotContain("חרדה");

        EncryptedTestRecord loaded = entityManager.find(EncryptedTestRecord.class, record.id);
        assertThat(loaded.secretText).isEqualTo(secret);
    }

    @Entity
    @Table(name = "crypto_test_records")
    static class EncryptedTestRecord {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id;

        @Convert(converter = CryptoAttributeConverter.class)
        @Column(name = "secret_text", columnDefinition = "TEXT")
        String secretText;
    }
}
