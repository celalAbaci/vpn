package com.celalabaci.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Oluşturduğumuz RateLimitingInterceptor'ı Spring'e kaydetmek
 * ve sadece belirli yollara (path) uygulamak için kullanılır.
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // RateLimitingInterceptor'ı kaydet
        registry.addInterceptor(rateLimitingInterceptor)
                // Bu interceptor'ın SADECE auth endpoint'leri için çalışmasını sağla
                .addPathPatterns("/api/v1/auth/**");
    }
}
