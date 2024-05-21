package com.armaninvestment.parsparandreporterapplication;

import com.armaninvestment.parsparandreporterapplication.entities.InvoiceStatus;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.repositories.InvoiceStatusRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class ParsparandReporterApplication {
    private final YearRepository yearRepository;
    private final InvoiceStatusRepository invoiceStatusRepository;

    public static void main(String[] args) {
        SpringApplication.run(ParsparandReporterApplication.class, args);
    }
    @Bean
    public CommandLineRunner init() {
        List<Year> years = List.of(
                new Year(1400L),
                new Year(1401L),
                new Year(1402L),
                new Year(1403L)
        );
        InvoiceStatus invoiceStatus = new InvoiceStatus();
        invoiceStatus.setName("سند حسابداری");

        return args -> {
            if (yearRepository.count() == 0){
                yearRepository.saveAll(years);
            }

            if (invoiceStatusRepository.count() == 0){
                invoiceStatusRepository.save(invoiceStatus);
            }

        };
    }

}
