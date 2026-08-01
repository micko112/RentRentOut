package org.landm;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Configuration
public class I18nConfig implements WebMvcConfigurer {

    public static final Locale LOCALE_SR = new Locale("sr");
    public static final Locale LOCALE_EN = Locale.ENGLISH;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding(StandardCharsets.UTF_8.name());
        ms.setUseCodeAsDefaultMessage(true);
        ms.setFallbackToSystemLocale(false);
        ms.setDefaultLocale(LOCALE_SR);
        return ms;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(LOCALE_SR);
        resolver.setSupportedLocales(List.of(LOCALE_SR, LOCALE_EN));
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // AcceptHeaderLocaleResolver reads directly from Accept-Language header on every request;
        // no LocaleChangeInterceptor needed.
    }
}
