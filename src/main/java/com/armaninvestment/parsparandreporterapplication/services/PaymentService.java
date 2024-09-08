package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.PaymentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Payment;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubject;
import com.armaninvestment.parsparandreporterapplication.mappers.PaymentMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.PaymentRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.PaymentSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.PaymentSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.CustomPageImpl;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.convertToDate;
import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.getCellStringValue;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;

    public Page<PaymentDto> findPaymentByCriteria(PaymentSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Payment> specification = PaymentSpecification.bySearchCriteria(search);
        Page<PaymentDto> dtoPage = paymentRepository.findAll(specification, pageRequest).map(paymentMapper::toDto);
        double overallTotalAmount = paymentRepository.findAll(specification, PageRequest.of(0, Integer.MAX_VALUE, sort)).getContent()
                .stream().mapToDouble(Payment::getPaymentAmount).sum();
        CustomPageImpl<PaymentDto> pageImpel = new CustomPageImpl<>(dtoPage.getContent(), pageRequest, dtoPage.getTotalElements());
        pageImpel.setOverallTotalAmount(overallTotalAmount);
        return pageImpel;
    }

    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {
        var paymentEntity = paymentMapper.toEntity(paymentDto);

        Optional<Customer> customer = customerRepository.findById(paymentDto.getCustomerId());
        Long _year = Long.valueOf(DateConvertor.convertGregorianToJalali(paymentDto.getPaymentDate()).substring(0, 4));
        Optional<Year> year = yearRepository.findByName(_year);

        customer.ifPresent(c -> {
            paymentEntity.setCustomer(c);
            c.getPayments().add(paymentEntity);
        });
        year.ifPresent(y -> {
            paymentEntity.setYear(y);
            y.getPayments().add(paymentEntity);

        });

        var savedPayment = paymentRepository.save(paymentEntity);
        return paymentMapper.toDto(savedPayment);
    }

    public PaymentDto getPaymentById(Long id) {
        var paymentEntity = paymentRepository.findById(id).orElseThrow();
        return paymentMapper.toDto(paymentEntity);
    }

    public PaymentDto updatePayment(Long id, PaymentDto paymentDto) {
        var paymentEntity = paymentRepository.findById(id).orElseThrow();
        Payment partialUpdate = paymentMapper.partialUpdate(paymentDto, paymentEntity);

        Customer customer = customerRepository.findById(paymentDto.getCustomerId()).orElseThrow();
        Year year = yearRepository.findById(paymentDto.getYearId()).orElseThrow();

        partialUpdate.setCustomer(customer);
        partialUpdate.setYear(year);

        var updatedPayment = paymentRepository.save(partialUpdate);
        return paymentMapper.toDto(updatedPayment);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public String importPaymentsFromExcel(MultipartFile file) throws IOException {
        List<PaymentDto> paymentDtos = new ArrayList<>();

        Map<String, Customer> customersMap = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getCustomerCode, customer -> customer, (existing, replacement) -> existing));
        Map<Long, Year> yearsMap = yearRepository.findAll().stream()
                .collect(Collectors.toMap(Year::getName, year -> year, (existing, replacement) -> existing));

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
                    LocalDate paymentDate = convertToDate(getCellStringValue(currentRow, 0, rowNum));
                    String paymentDescription = getCellStringValue(currentRow, 1, rowNum);
                    String customerCode = getCellStringValue(currentRow, 2, rowNum);
                    Double paymentAmount = Double.parseDouble(Objects.requireNonNull(getCellStringValue(currentRow, 3, rowNum)));
                    PaymentSubject paymentSubject = PaymentSubject.valueOf(getCellStringValue(currentRow, 4, rowNum));

                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new IllegalStateException("مشتری با کد " + customerCode + " یافت نشد."));
                    long yearName = Long.parseLong(Objects.requireNonNull(getCellStringValue(currentRow, 0, rowNum)).substring(0, 4));
                    Year year = Optional.ofNullable(yearsMap.get(yearName))
                            .orElseThrow(() -> new IllegalStateException("سال با نام " + yearName + " یافت نشد."));

                    PaymentDto paymentDto = new PaymentDto();
                    paymentDto.setPaymentDate(paymentDate);
                    paymentDto.setPaymentDescription(paymentDescription);
                    paymentDto.setCustomerId(customer.getId());
                    paymentDto.setPaymentAmount(paymentAmount);
                    paymentDto.setPaymentSubject(paymentSubject);
                    paymentDto.setYearId(year.getId());

                    paymentDtos.add(paymentDto);

                } catch (Exception e) {
                    throw new IllegalStateException("خطا در ردیف " + rowNum + ": " + e.getMessage());
                }
            }
            List<Payment> payments = paymentDtos.stream()
                        .map(paymentDto -> {
                            Payment payment = paymentMapper.toEntity(paymentDto);
                            payment.setCustomer(customerRepository.findById(paymentDto.getCustomerId()).orElseThrow());
                            payment.setYear(yearRepository.findById(paymentDto.getYearId()).orElseThrow());
                            return payment;
                        })
                        .toList();

            paymentRepository.saveAll(payments);
        }

        return "No payments have been imported.";

    }

    public byte[] exportPaymentsToExcel(PaymentSearch search, boolean exportAll) throws IllegalAccessException {
        List<PaymentDto> payments;

        if (exportAll) {
            payments = findAll(search);
        } else {
            Page<PaymentDto> paginatedPayments = findPage(search.getPage(), search.getSize(), search.getSortBy(), search.getOrder(), search);
            payments = paginatedPayments.getContent();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payments");
            // set direction to rtl
            sheet.setRightToLeft(true);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Initialize total
            double totalPaymentAmount = 0;

            // Create data rows
            int rowNum = 1;
            for (PaymentDto payment : payments) {
                Row row = sheet.createRow(rowNum++);
                populatePaymentRow(payment, row);
                totalPaymentAmount += payment.getPaymentAmount() != null ? payment.getPaymentAmount() : 0.0;
            }

            // Create subtotal row
            Row subtotalRow = sheet.createRow(rowNum);
            createSubtotalRow(subtotalRow, totalPaymentAmount);

            // Adjust column widths
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to a byte array output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export payments to Excel", e);
        }
    }

    private void createSubtotalRow(Row subtotalRow, double totalPaymentAmount) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle footerCellStyle = cellStyleHelper.getFooterCellStyle(subtotalRow.getSheet().getWorkbook());

        int cellNum = 0;
        subtotalRow.createCell(cellNum++).setCellValue("جمع کل"); // Subtotal label
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue(totalPaymentAmount);
        subtotalRow.createCell(cellNum++).setCellValue("");

        // Merge cells [0, 3]
        subtotalRow.getSheet().addMergedRegion(new CellRangeAddress(subtotalRow.getRowNum(), subtotalRow.getRowNum(), 0, 3));

        for (int i = 0; i < subtotalRow.getLastCellNum(); i++) {
            Cell cell = subtotalRow.getCell(i);
            if (cell != null) {
                cell.setCellStyle(footerCellStyle);
            }
        }
        subtotalRow.getCell(4).setCellStyle(footerCellStyle);
    }

    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "تاریخ", "توضیحات", "کد مشتری", "نام مشتری", "مبلغ (ریال)", "موضوع پرداخت"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private String convertToPaymentSubjectPersianCaption(String paymentSubject) {
        return switch (paymentSubject) {
            case "PRODUCT" -> "محصول";
            case "INSURANCEDEPOSIT" -> "سپرده بیمه";
            case "PERFORMANCEBOUND" -> "حسن انجام کار";
            case "ADVANCEDPAYMENT" -> "پیش پرداخت";
            default -> "نامشخص";
        };
    }

    private void populatePaymentRow(PaymentDto payment, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(payment.getPaymentDate() != null ? DateConvertor.convertGregorianToJalali(payment.getPaymentDate()) : "");
        row.createCell(cellNum++).setCellValue(payment.getPaymentDescription() != null ? payment.getPaymentDescription() : "");
        row.createCell(cellNum++).setCellValue(payment.getCustomerId() != null ? payment.getCustomerId() : 0);
        row.createCell(cellNum++).setCellValue(payment.getCustomerName() != null ? payment.getCustomerName() : "");
        row.createCell(cellNum++).setCellValue(payment.getPaymentAmount() != null ? payment.getPaymentAmount() : 0.0);
        row.createCell(cellNum++).setCellValue(payment.getPaymentSubject() != null ? convertToPaymentSubjectPersianCaption(payment.getPaymentSubject().name()) : "");

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());

        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            cell.setCellStyle(defaultCellStyle);
        }
        row.getCell(4).setCellStyle(monatoryCellStyle);
    }

    private List<PaymentDto> findAll(PaymentSearch search) {
        Specification<Payment> paymentSpecification = PaymentSpecification.bySearchCriteria(search);
        List<Payment> payments = paymentRepository.findAll(paymentSpecification);
        return payments.stream().map(paymentMapper::toDto).collect(Collectors.toList());
    }

    private Page<PaymentDto> findPage(int page, int size, String sortBy, String order, PaymentSearch search) {
        var direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, direction, sortBy);
        Specification<Payment> spec = PaymentSpecification.bySearchCriteria(search);
        Page<Payment> paginatedPayments = paymentRepository.findAll(spec, pageable);
        return paginatedPayments.map(paymentMapper::toDto);
    }


}
