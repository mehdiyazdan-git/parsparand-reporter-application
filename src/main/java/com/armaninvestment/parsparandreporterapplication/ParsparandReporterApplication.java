package com.armaninvestment.parsparandreporterapplication;

import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Product;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.armaninvestment.parsparandreporterapplication.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.ProductRepository;
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
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public static void main(String[] args) {
        SpringApplication.run(ParsparandReporterApplication.class, args);
    }
    @Bean
    public CommandLineRunner init() {
        List<Year> years = List.of(
                new Year(1401L),
                new Year(1402L),
                new Year(1403L)
        );
        List<Customer> customers = List.of(
                new Customer("customer1"),
                new Customer("customer2"),
                new Customer("customer3")

        );

        List<Product> products = List.of(
                new Product("product1", ProductType.MAIN),
                new Product("product2",ProductType.MAIN),
                new Product("product3",ProductType.MAIN)

        );
        return args -> {
            if (yearRepository.count() == 0){
                yearRepository.saveAll(years);
            }

            if (customerRepository.count() == 0){
                customerRepository.saveAll(customers);
            }

            if (productRepository.count() == 0){
                productRepository.saveAll(products);
            }
        };
    }

}
