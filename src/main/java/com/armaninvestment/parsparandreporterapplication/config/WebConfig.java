package com.armaninvestment.parsparandreporterapplication.config;

import com.armaninvestment.parsparandreporterapplication.converters.StringToProductTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToProductTypeConverter stringToProductTypeConverter;

    public WebConfig(StringToProductTypeConverter stringToProductTypeConverter) {
        this.stringToProductTypeConverter = stringToProductTypeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToProductTypeConverter);
    }
}
