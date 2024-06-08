package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.*;
import com.armaninvestment.parsparandreporterapplication.entities.Contract;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporterapplication.mappers.CustomerMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.CustomerSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.CustomerSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
    private final WarehouseReceiptItemRepository warehouseReceiptItemRepository;

    public Page<CustomerDto> findCustomerByCriteria(CustomerSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Customer> specification = CustomerSpecification.bySearchCriteria(search);
        return customerRepository.findAll(specification, pageRequest)
                .map(customerMapper::toDto);
    }
    public List<CustomerSelect> findAllCustomerSelect(String searchParam,String sortBy,String order) {
        Specification<Customer> specification = CustomerSpecification.getSelectSpecification(searchParam);
        Sort sort = Sort.by(Sort.Direction.fromString(order.toLowerCase()), sortBy);
        return customerRepository
                .findAll(specification, sort)
                .stream()
                .map(customerMapper::toSelectDto)
                .collect(Collectors.toList());
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

    public ClientSummaryResult getClientSummaryByCustomerId(Long customerId) {
        List<Object[]> objects = warehouseReceiptItemRepository.getClientSummaryByCustomerId(customerId);
        List<ClientSummaryDTO> list = new ArrayList<>();
        for (Object[] obj : objects) {
            ClientSummaryDTO dto = new ClientSummaryDTO();
            dto.setContractNumber((String) obj[0]);
            dto.setAdvancedPayment((Double) obj[1]);
            dto.setPerformanceBound((Double) obj[2]);
            dto.setInsuranceDeposit((Double) obj[3]);
            dto.setSalesAmount((Double) obj[4]);
            dto.setSalesQuantity((Double) obj[5]);
            dto.setVat((Double) obj[6]);
            list.add(dto);
        }

        PaymentReportDto totalPaymentByCustomerId = getPaymentGroupBySubjectFilterByCustomerId(customerId);

        double performanceBoundCoefficient = 0d;
        double insuranceDepositCoefficient = 0d;

        Optional<Contract> optionalContract = contractRepository.findLastContractByCustomerId(customerId);
        if (optionalContract.isPresent()) {
            Contract contract = optionalContract.get();
            performanceBoundCoefficient = (contract.getPerformanceBond() != null) ? contract.getPerformanceBond() : 0d;
            insuranceDepositCoefficient = (contract.getInsuranceDeposit() != null) ? contract.getInsuranceDeposit() : 0d;
        }

        NotInvoicedReportDto notInvoicedReportDto = getNotInvoicedByCustomerId(customerId,insuranceDepositCoefficient,performanceBoundCoefficient);
        AdjustmentReportDto adjustmentReportDto =  getAdjustmentByCustomerId(customerId,insuranceDepositCoefficient,performanceBoundCoefficient);

        return new ClientSummaryResult(
                list,
                notInvoicedReportDto,
                adjustmentReportDto,
                totalPaymentByCustomerId
        );
    }
    @Data
    private static class NotInvoiced {
        private Double amount;
        private Double quantity;

        public NotInvoiced(Double amount, Double quantity) {
            this.amount = amount;
            this.quantity = quantity;
        }
    }
    private NotInvoicedReportDto getNotInvoicedByCustomerId(Long customerId, Double insuranceDeposit, Double performanceBound) {
        List<Object[]> objectList = warehouseReceiptRepository.getNotInvoicedAmountByCustomerId(customerId);
        NotInvoiced notInvoiced = objectList.stream().map(obj -> new NotInvoiced((Double) obj[0], (Double) obj[1])).toList().get(0);
        Double amount = notInvoiced.getAmount();
        Double quantity = notInvoiced.getQuantity();
        if (amount != null && amount > 0) {
            Double vat = (double) Math.round(amount * 0.09);
            Long performance = Math.round(amount * performanceBound);
            Long insurance = Math.round(amount * insuranceDeposit);

            return new NotInvoicedReportDto(amount, quantity, vat, insurance, performance);
        }
        return new NotInvoicedReportDto(0d,0d, 0d, 0L, 0L);
    }

    private AdjustmentReportDto getAdjustmentByCustomerId(Long customerId, Double insuranceDeposit, Double performanceBound) {
        Double adjustments = (Double) warehouseReceiptItemRepository.getAdjustmentsByCustomerId(customerId);

        if (adjustments != null && adjustments != 0) {
            Double vat = (double) Math.round(adjustments * 0.09);
            Long insuranceDepositValue = Math.round(adjustments * insuranceDeposit);
            Long performanceBoundValue = Math.round(adjustments * performanceBound);

            return new AdjustmentReportDto(
                    adjustments,
                    vat,
                    insuranceDepositValue,
                    performanceBoundValue
            );
        }
        return new AdjustmentReportDto(0d, 0d, 0L, 0L);
    }

    private PaymentReportDto getPaymentGroupBySubjectFilterByCustomerId(Long customerId) {
        List<Object[]> objectList = paymentRepository.getPaymentGroupBySubjectFilterByCustomerId(customerId);

        PaymentReportDto paymentReportDto = new PaymentReportDto();

        for (Object[] row : objectList) {
            Long paymentSubject = (Long) row[0];
            Double sum = (Double) row[1];

            if (paymentSubject == 1) {
                paymentReportDto.setProductPayment(sum);
            } else if (paymentSubject == 2) {
                paymentReportDto.setInsuranceDepositPayment(sum);
            } else if (paymentSubject == 3) {
                paymentReportDto.setPerformanceBoundPayment(sum);
            } else if (paymentSubject == 4) {
                paymentReportDto.setAdvancedPayment(sum);
            }
        }

        return paymentReportDto;
    }
}
