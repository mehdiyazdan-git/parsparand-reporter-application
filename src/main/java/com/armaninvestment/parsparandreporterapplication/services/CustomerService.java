package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.CustomerDto;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporterapplication.mappers.CustomerMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.CustomerSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.CustomerSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReportItemRepository reportItemRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;

    public Page<CustomerDto> findCustomerByCriteria(CustomerSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Customer> specification = CustomerSpecification.bySearchCriteria(search);
        return customerRepository.findAll(specification, pageRequest)
                .map(customerMapper::toDto);
    }
    public CustomerDto getCustomerById(Long id) {
        var customerEntity = customerRepository.findById(id).orElseThrow();
        return customerMapper.toDto(customerEntity);
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {

        if (customerRepository.existsByName(customerDto.getName())) {
            throw new IllegalStateException("یک مشتری با همین نام قبلاً ثبت شده است.");
        }
        if (customerRepository.existsByCustomerCode(customerDto.getCustomerCode())) {
            throw new IllegalStateException("یک مشتری با همین کد مشتری قبلاً ثبت شده است.");
        }
        var customerEntity = customerMapper.toEntity(customerDto);
        var savedCustomer = customerRepository.save(customerEntity);
        return customerMapper.toDto(savedCustomer);
    }


    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        var existingCustomer = customerRepository.findById(id).orElseThrow(() -> new IllegalStateException("مشتری پیدا نشد."));

        if (customerRepository.existsByNameAndIdNot(customerDto.getName(), id)) {
            throw new IllegalStateException("یک مشتری دیگر با همین نام وجود دارد.");
        }
        if (customerRepository.existsByCustomerCodeAndIdNot(customerDto.getCustomerCode(), id)) {
            throw new IllegalStateException("یک مشتری دیگر با همین کد مشتری وجود دارد.");
        }
        Customer partialUpdate = customerMapper.partialUpdate(customerDto, existingCustomer);
        var updatedCustomer = customerRepository.save(partialUpdate);
        return customerMapper.toDto(updatedCustomer);
    }


    public void deleteCustomer(Long id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            throw new EntityNotFoundException("مشتری ای با شناسه " + id + "یافت نشد.");
        }
        Long customerId = optionalCustomer.get().getId();

        if (reportItemRepository.existsByCustomerId(customerId)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون آیتم‌های گزارش مرتبط دارد.");
        }
        if (warehouseReceiptRepository.existsByCustomerId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون رسیدهای انبار مرتبط دارد.");
        }
        if (contractRepository.existsByCustomerId(customerId)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون قراردادها مرتبط دارد.");
        }
        if (invoiceRepository.existsByCustomerId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون فاکتورهای مرتبط دارد.");
        }
        if (paymentRepository.existsByCustomerId(customerId)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون پرداخت‌های مرتبط دارد.");
        }
        customerRepository.deleteById(customerId);
    }

    public String importCustomersFromExcel(MultipartFile file) throws IOException {
        List<CustomerDto> customerDtos = ExcelDataImporter.importData(file, CustomerDto.class);
        List<Customer> customers = customerDtos.stream().map(customerMapper::toEntity).collect(Collectors.toList());
        customerRepository.saveAll(customers);
        return customers.size() + " مشتری با موفقیت اضافه شد.";
    }

    public byte[] exportCustomersToExcel() throws IOException {
        List<CustomerDto> customerDtos = customerRepository.findAll().stream().map(customerMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(customerDtos, CustomerDto.class);
    }
}
