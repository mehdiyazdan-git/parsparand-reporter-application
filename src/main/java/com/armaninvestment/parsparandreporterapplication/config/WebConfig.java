package com.armaninvestment.parsparandreporterapplication.config;

import com.armaninvestment.parsparandreporterapplication.converters.StringToAdjustmentTypeConverter;
import com.armaninvestment.parsparandreporterapplication.converters.StringToProductTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToProductTypeConverter stringToProductTypeConverter;
    private final StringToPaymentSubjectConverter stringToPaymentSubjectConverter;

    private final StringToAdjustmentTypeConverter stringToAdjustmentTypeConverter;

    public WebConfig(StringToProductTypeConverter stringToProductTypeConverter,
                     StringToPaymentSubjectConverter stringToPaymentSubjectConverter,
                     StringToAdjustmentTypeConverter stringToAdjustmentTypeConverter) {
        this.stringToProductTypeConverter = stringToProductTypeConverter;
        this.stringToPaymentSubjectConverter = stringToPaymentSubjectConverter;
        this.stringToAdjustmentTypeConverter = stringToAdjustmentTypeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToProductTypeConverter);
        registry.addConverter(stringToPaymentSubjectConverter);
        registry.addConverter(stringToAdjustmentTypeConverter);
    }
}
