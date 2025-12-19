package com.celalabaci.payment.google;

import com.celalabaci.exception.InvalidGoogleConfigException;
import com.celalabaci.exception.MessageType; // Hata çözümü için eklendi
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
// Bu @Configuration sınıfının sadece 'google.play.enabled=true' ise yüklenmesini sağlar.
@ConditionalOnProperty(name = "google.play.enabled", havingValue = "true")
public class GooglePaymentConfig {

    private static final Logger logger = LoggerFactory.getLogger(GooglePaymentConfig.class);

    // Uygulama adınızı buraya yazabilirsiniz
    private static final String APPLICATION_NAME = "SuperVPNProject";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Bu dosya yolu, loglarda göründüğü gibi projenin kök dizinini işaret ediyor.
    private static final String SERVICE_ACCOUNT_PATH = "./google-service-account.json";

    @Bean
    public AndroidPublisher androidPublisher() {
        logger.info("Google AndroidPublisher servisi başlatılıyor...");
        logger.info("Kullanılan Service Account JSON yolu: " + SERVICE_ACCOUNT_PATH);

        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            Credential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACCOUNT_PATH))
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

            return new AndroidPublisher.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (FileNotFoundException e) {
            String errorMessage = "Google Service Account JSON dosyası okunamadı veya geçersiz: " + SERVICE_ACCOUNT_PATH;
            logger.error("Google AndroidPublisher servisi başlatılamadı! JSON dosyası bulunamadı: {}", SERVICE_ACCOUNT_PATH, e);
            // HATA DÜZELTMESİ: Üçüncü parametre (e) kaldırıldı, çünkü constructor (MessageType, String) bekliyor.
            throw new InvalidGoogleConfigException(MessageType.INVALID_GOOGLE_CONFIG, errorMessage);

        } catch (IOException | GeneralSecurityException e) {
            String errorMessage = "Google Play API yapılandırması eksik veya hatalı.: " + e.getMessage();
            logger.error("Google AndroidPublisher servisi başlatılamadı! API erişimi veya güvenlik hatası.", e);
            // HATA DÜZELTMESİ: Üçüncü parametre (e) kaldırıldı, çünkü constructor (MessageType, String) bekliyor.
            throw new InvalidGoogleConfigException(MessageType.INVALID_GOOGLE_CONFIG, errorMessage);
        }
    }
}

