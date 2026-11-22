package com.therapy.coredataservice.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ממיר JPA שקוף: מצפין שדה String לפני כתיבה ל-DB ומפענח אותו בקריאה.
 * שימוש בישות: {@code @Convert(converter = CryptoAttributeConverter.class)}.
 * <p>
 * בזמן ריצה רגיל Hibernate מקבל את המופע מ-Spring עם ה-{@link CryptoService} המוגדר.
 * אם Hibernate יוצר את הממיר מחוץ ל-Spring (למשל בטסט slice בלי ה-Bean),
 * נבנה CryptoService מתוך משתנה הסביבה DATA_ENCRYPTION_KEY או מפתח הפיתוח המוגדר ב-application.yml.
 */
@Component
@Converter
public class CryptoAttributeConverter implements AttributeConverter<String, String> {

    /** זהה לערך ברירת המחדל ב-application.yml – לפיתוח ובדיקות בלבד. */
    static final String DEV_FALLBACK_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final CryptoService cryptoService;

    @Autowired
    public CryptoAttributeConverter(ObjectProvider<CryptoService> cryptoServiceProvider) {
        this.cryptoService = cryptoServiceProvider.getIfAvailable(CryptoAttributeConverter::fallbackCryptoService);
    }

    public CryptoAttributeConverter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public CryptoAttributeConverter() {
        this(fallbackCryptoService());
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : cryptoService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : cryptoService.decrypt(dbData);
    }

    private static CryptoService fallbackCryptoService() {
        String envKey = System.getenv("DATA_ENCRYPTION_KEY");
        return new CryptoService(envKey != null && !envKey.isBlank() ? envKey : DEV_FALLBACK_KEY);
    }
}
