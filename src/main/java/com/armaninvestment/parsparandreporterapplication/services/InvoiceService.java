package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.InvoiceSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.*;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final ContractRepository contractRepository;
    private final ProductRepository productRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;

    public Page<InvoiceDto> findInvoiceByCriteria(InvoiceSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(search);
        return invoiceRepository.findAll(specification, pageRequest)
                .map(invoiceMapper::toDto);
    }

    public InvoiceDto getInvoiceById(Long id) {
        var invoiceEntity = invoiceRepository.findById(id).orElseThrow();
        return invoiceMapper.toDto(invoiceEntity);
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        validateInvoiceUniqueness(invoiceDto);

        var invoiceEntity = invoiceMapper.toEntity(invoiceDto);
        invoiceEntity.setContract(contractRepository.findById(invoiceDto.getContractId()).orElseThrow());
        invoiceEntity.setCustomer(customerRepository.findById(invoiceDto.getCustomerId()).orElseThrow());
        invoiceEntity.setYear(yearRepository.findById(invoiceDto.getYearId()).orElseThrow());
        var savedInvoice = invoiceRepository.save(invoiceEntity);
        return invoiceMapper.toDto(savedInvoice);
    }

    @Transactional
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        var existingInvoice = invoiceRepository.findById(id).orElseThrow(() -> new IllegalStateException("صورت‌حساب پیدا نشد."));

        validateInvoiceUniquenessForUpdate(invoiceDto, id);

        var updatedInvoice = invoiceMapper.toEntity(invoiceDto);
        return invoiceMapper.toDto(invoiceRepository.save(updatedInvoice));
    }

    private void validateInvoiceUniqueness(InvoiceDto invoiceDto) {
        if (invoiceRepository.existsByInvoiceNumberAndYearId(invoiceDto.getInvoiceNumber(), invoiceDto.getYearId())) {
            throw new IllegalStateException("یک صورت‌حساب با این شماره صورت‌حساب برای سال مالی مورد نظر قبلاً ثبت شده است.");
        }
        validateReceiptIdUniqueness(invoiceDto);
    }

    private void validateInvoiceUniquenessForUpdate(InvoiceDto invoiceDto, Long id) {
        if (invoiceRepository.existsByInvoiceNumberAndYearIdAndIdNot(invoiceDto.getInvoiceNumber(),invoiceDto.getYearId(), id)) {
            throw new IllegalStateException("یک صورت‌حساب دیگر با این شماره صورت‌حساب برای سال مالی مورد نظر وجود دارد.");
        }
        validateReceiptIdUniqueness(invoiceDto);
    }

    private void validateReceiptIdUniqueness(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                if (invoiceItemRepository.existsByWarehouseReceiptId(invoiceItemDto.getWarehouseReceiptId())){
                    throw new IllegalStateException("برای این شماره حواله قبلا فاکتور صادر شده است.");
                }
            });
        }
    }



    public byte[] exportInvoicesToExcel() throws IOException {
        List<InvoiceDto> invoiceDtos = invoiceRepository.findAll().stream().map(invoiceMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(invoiceDtos, InvoiceDto.class);
    }
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalStateException("صورت‌حساب پیدا نشد.");
        }
        invoiceRepository.deleteById(id);
    }

    public String importInvoicesFromExcel(MultipartFile file) throws IOException {
        Map<Long, InvoiceDto> invoicesMap = new HashMap<>();

        // Fetch all necessary data once
        Map<String, Customer> customersMap = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getCustomerCode, customer -> customer, (existing, replacement) -> existing));
        Map<Long, Year> yearsMap = yearRepository.findAll().stream()
                .collect(Collectors.toMap(Year::getName, year -> year, (existing, replacement) -> existing));
        Map<String, Contract> contractsMap = contractRepository.findAll().stream()
                .collect(Collectors.toMap(Contract::getContractNumber, contract -> contract, (existing, replacement) -> existing));
        Map<String, Product> productsMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getProductCode, product -> product, (existing, replacement) -> existing));
        Map<String, WarehouseReceipt> warehouseReceiptsMap = warehouseReceiptRepository.findAll().stream()
                .collect(Collectors.toMap(
                        wr -> wr.getWarehouseReceiptNumber() + "-" + wr.getWarehouseReceiptDate(),
                        wr -> wr,
                        (existing, replacement) -> existing
                ));

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();

            // Skip the header row
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNum = 1;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNum++;
                try {
                    // invoiceNumber, issuedDate, dueDate, salesType, contractNumber, customerCode, advancedPayment, insuranceDeposit, performanceBound, yearName, quantity, unitPrice, productCode, warehouseReceiptNumber, warehouseReceiptDate
                    Long invoiceNumber = getCellLongValue(currentRow, 0, rowNum);
                    LocalDate issuedDate = convertToDate(getCellStringValue(currentRow, 1, rowNum));
                    LocalDate dueDate = convertToDate(getCellStringValue(currentRow, 2, rowNum));
                    String salesType = getCellStringValue(currentRow, 3, rowNum);
                    String contractNumber = getCellStringValue(currentRow, 4, rowNum);
                    String customerCode = getCellStringValue(currentRow, 5, rowNum);
                    Long advancedPayment = getCellLongValue(currentRow, 6, rowNum);
                    Long insuranceDeposit = getCellLongValue(currentRow, 7, rowNum);
                    Long performanceBound = getCellLongValue(currentRow, 8, rowNum);
                    Long yearName = getCellLongValue(currentRow, 9, rowNum);
                    Integer quantity = getCellIntValue(currentRow, 10, rowNum);
                    Long unitPrice = getCellLongValue(currentRow, 11, rowNum);
                    String productCode = getCellStringValue(currentRow, 12, rowNum);
                    Long warehouseReceiptNumber = getCellLongValue(currentRow, 13, rowNum);
                    LocalDate warehouseReceiptDate = convertToDate(getCellStringValue(currentRow, 14, rowNum));

                    Year year = Optional.ofNullable(yearsMap.get(yearName)).orElseThrow(() -> new IllegalStateException("سال با نام " + yearName + " یافت نشد."));
                    Customer customer = Optional.ofNullable(customersMap.get(customerCode)).orElseThrow(() -> new IllegalStateException("مشتری با کد " + customerCode + " یافت نشد."));
                    Product product = Optional.ofNullable(productsMap.get(productCode)).orElseThrow(() -> new IllegalStateException("محصول با کد " + productCode + " یافت نشد."));


                    String receiptKey = warehouseReceiptNumber + "-" + warehouseReceiptDate;
                    WarehouseReceipt warehouseReceipt = Optional.ofNullable(warehouseReceiptsMap.get(receiptKey))
                            .orElseThrow(() -> new IllegalStateException("رسید انبار با شماره " + warehouseReceiptNumber + " و تاریخ " + warehouseReceiptDate + " یافت نشد."));

                    InvoiceDto invoiceDto = invoicesMap.computeIfAbsent(invoiceNumber, k -> {
                        InvoiceDto dto = new InvoiceDto();
                        dto.setInvoiceNumber(invoiceNumber);
                        dto.setIssuedDate(issuedDate);
                        dto.setDueDate(dueDate);
                        dto.setSalesType(salesType);
                        dto.setCustomerId(customer.getId());
                        if (contractNumber != null && !contractNumber.isEmpty()){
                            Contract contract = Optional.ofNullable(contractsMap.get(contractNumber)).orElseThrow(() -> new IllegalStateException("قرارداد با شماره " + contractNumber + " یافت نشد."));
                            dto.setContractId(contract.getId());
                        }
                        dto.setAdvancedPayment(advancedPayment);
                        dto.setInsuranceDeposit(insuranceDeposit);
                        dto.setPerformanceBound(performanceBound);
                        dto.setYearId(year.getId());
                        dto.setInvoiceItems(new LinkedHashSet<>());
                        return dto;
                    });

                    InvoiceItemDto itemDto = new InvoiceItemDto();
                    itemDto.setQuantity(quantity);
                    itemDto.setUnitPrice(unitPrice);
                    itemDto.setProductId(product.getId());
                    itemDto.setWarehouseReceiptId(warehouseReceipt.getId());
                    invoiceDto.getInvoiceItems().add(itemDto);

                } catch (Exception e) {
                    throw new RuntimeException("خطا در ردیف " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<Invoice> invoices = invoicesMap.values().stream()
                .map(invoiceMapper::toEntity)
                .collect(Collectors.toList());

        invoiceRepository.saveAll(invoices);
        return invoices.size() + " فاکتور با موفقیت وارد شدند.";
    }

}
