package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.mappers.ReturnedMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.ReturnedRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReturnedSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ReturnedSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.*;

@Service
@RequiredArgsConstructor
public class ReturnedService {
    private final ReturnedRepository returnedRepository;
    private final ReturnedMapper returnedMapper;
    private final YearRepository yearRepository;
    private final CustomerRepository customerRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public Page<ReturnedDto> findReturnedByCriteria(ReturnedSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Returned> specification = ReturnedSpecification.bySearchCriteria(search);
        return returnedRepository.findAll(specification, pageRequest)
                .map(returnedMapper::toDto);
    }

    public ResponseEntity<?> createReturned(ReturnedDto returnedDto) {
        try {
            var returnedEntity = returnedMapper.toEntity(returnedDto);
            if (returnedEntity.getReturnedDate() != null) {
                returnedEntity.setReturnedDate(returnedDto.getReturnedDate());
                String year = DateConvertor.convertGregorianToJalali(returnedDto.getReturnedDate()).substring(0, 4);
                Optional<Year> optionalYear = yearRepository.findByName(Long.valueOf(year));
                optionalYear.ifPresent(returnedEntity::setYear);
            }
            return ResponseEntity.ok(returnedMapper.toDto(returnedEntity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public ReturnedDto getReturnedById(Long id) {
        var returnedEntity = returnedRepository.findById(id).orElseThrow();
        return returnedMapper.toDto(returnedEntity);
    }

    public ReturnedDto updateReturned(Long id, ReturnedDto returnedDto) {

        var returned = returnedRepository.findById(id).orElseThrow();
        Returned partialUpdate = returnedMapper.partialUpdate(returnedDto, returned);

        Optional<Customer> optionalCustomer = customerRepository.findById(returnedDto.getCustomerId());
        Optional<Year> optionalYear = yearRepository.findByName(Long.valueOf(returned.getJalaliYear()));

        optionalYear.ifPresent(partialUpdate::setYear);
        optionalCustomer.ifPresent(partialUpdate::setCustomer);

        return returnedMapper.toDto(returnedRepository.save(partialUpdate));
    }

    public void deleteReturned(Long id) {
        returnedRepository.deleteById(id);
    }

    @Transactional
    public String importReturnedsFromExcel(MultipartFile file) throws IOException {
        Map<Long, ReturnedDto> returnedMap = new LinkedHashMap<>();

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
                    Long returnedNumber = getCellLongValue(currentRow, 0, rowNum);
                    LocalDate returnedDate = convertToDate(getCellStringValue(currentRow, 1, rowNum));
                    String returnedDescription = getCellStringValue(currentRow, 2, rowNum);
                    Double unitPrice = getCellDoubleValue(currentRow, 3, rowNum);
                    Long quantity = getCellLongValue(currentRow, 4, rowNum);
                    String customerCode = getCellStringValue(currentRow, 5, rowNum);
                    Long yearName = getCellLongValue(currentRow, 6, rowNum);


                    List<ReturnedDto> list = returnedMap.values().stream().peek(
                                                returnedDto -> {
                                                    returnedDto.setReturnedNumber(returnedNumber);
                                                    returnedDto.setReturnedDate(returnedDate);
                                                    returnedDto.setReturnedDescription(returnedDescription);
                                                    returnedDto.setUnitPrice(unitPrice);
                                                    returnedDto.setQuantity(quantity);
                                                    returnedDto.setCustomerId(customersMap.get(customerCode).getId());
                                                    returnedDto.setYearId(yearsMap.get(yearName).getId());
                                                }).toList();

                    List<Returned> returnedList = list.stream().map(
                                                returnedDto -> {
                                                    Returned entity = returnedMapper.toEntity(returnedDto);
                                                    if (returnedDto.getCustomerId() != null) {
                                                        entity.setCustomer(entityManager.find(Customer.class, returnedDto.getCustomerId()));
                                                    }
                                                    if (returnedDto.getYearId() != null) {
                                                        entity.setYear(entityManager.find(Year.class, returnedDto.getYearId()));
                                                    }
                                                    return entity;
                                                }).toList();

                    returnedRepository.saveAll(returnedList);


                } catch (Exception e) {
                    throw new RuntimeException("Error in row " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<Returned> returneds = returnedMap.values().stream()
                .map(returnedDto -> {
                    Returned entity = returnedMapper.toEntity(returnedDto);
                    if (returnedDto.getCustomerId() != null) {
                        entity.setCustomer(entityManager.find(Customer.class, returnedDto.getCustomerId()));
                    }
                    if (returnedDto.getYearId() != null) {
                        entity.setYear(entityManager.find(Year.class, returnedDto.getYearId()));
                    }
                    return entity;
                })
                .collect(Collectors.toList());

        returnedRepository.saveAll(returneds);

        return returneds.size() + " returneds successfully imported.";
    }



    public byte[] exportReturnedsToExcel(ReturnedSearch search, boolean exportAll) throws IOException {
        List<ReturnedDto> returneds;

        if (exportAll) {
            returneds = findAll(search);
        } else {
            Page<ReturnedDto> paginatedReturneds = findPage(search.getPage(), search.getSize(), search.getSortBy(), search.getOrder(), search);
            returneds = paginatedReturneds.getContent();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Returneds");
            // set direction to rtl
            sheet.setRightToLeft(true);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Initialize totals
            long totalQuantity = 0;
            double totalPrice = 0;

            // Create data rows
            int rowNum = 1;
            for (ReturnedDto returned : returneds) {
                Row row = sheet.createRow(rowNum++);
                populateReturnedRow(returned, row);

                // Sum totals
                totalQuantity += returned.getQuantity() != null ? returned.getQuantity() : 0;
                totalPrice += returned.getUnitPrice() != null ? (returned.getUnitPrice() * (returned.getQuantity() != null ? returned.getQuantity() : 0)) : 0;
            }

            // Create subtotal row
            Row subtotalRow = sheet.createRow(rowNum);
            createSubtotalRow(subtotalRow, totalQuantity, totalPrice);

            // Adjust column widths
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to a byte array output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export returned to Excel", e);
        }
    }

    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "شناسه برگشتی", "تاریخ برگشتی", "شرح برگشتی", "شماره برگشتی", "قیمت واحد",
                "شناسه مشتری",  "تعداد"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void populateReturnedRow(ReturnedDto returned, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(returned.getId() != null ? returned.getId() : 0);
        row.createCell(cellNum++).setCellValue(returned.getReturnedDate() != null ? DateConvertor.convertGregorianToJalali(returned.getReturnedDate()) : "");
        row.createCell(cellNum++).setCellValue(returned.getReturnedDescription() != null ? returned.getReturnedDescription() : "");
        row.createCell(cellNum++).setCellValue(returned.getReturnedNumber() != null ? returned.getReturnedNumber() : 0);
        row.createCell(cellNum++).setCellValue(returned.getUnitPrice() != null ? returned.getUnitPrice() : 0.0);
        row.createCell(cellNum++).setCellValue(returned.getCustomerId() != null ? returned.getCustomerId() : 0);
        row.createCell(cellNum++).setCellValue(returned.getQuantity() != null ? returned.getQuantity() : 0);

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());

        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            cell.setCellStyle(defaultCellStyle);
        }
        row.getCell(4).setCellStyle(monatoryCellStyle);
        row.getCell(8).setCellStyle(monatoryCellStyle);
    }

    private void createSubtotalRow(Row subtotalRow, long totalQuantity, double totalPrice) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle footerCellStyle = cellStyleHelper.getFooterCellStyle(subtotalRow.getSheet().getWorkbook());

        int cellNum = 0;
        subtotalRow.createCell(cellNum++).setCellValue("جمع کل"); // Subtotal label
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue(totalQuantity);
        subtotalRow.createCell(cellNum++).setCellValue(totalPrice);

        // Merge cells [0, 6]
        subtotalRow.getSheet().addMergedRegion(new CellRangeAddress(subtotalRow.getRowNum(), subtotalRow.getRowNum(), 0, 4));

        for (int i = 0; i < subtotalRow.getLastCellNum(); i++) {
            Cell cell = subtotalRow.getCell(i);
            if (cell != null) {
                cell.setCellStyle(footerCellStyle);
            }
        }
        subtotalRow.getCell(5).setCellStyle(footerCellStyle);
        subtotalRow.getCell(6).setCellStyle(footerCellStyle);
    }

    private List<ReturnedDto> findAll(ReturnedSearch search) {
        Specification<Returned> returnedSpecification = ReturnedSpecification.bySearchCriteria(search);
        List<Returned> returneds = returnedRepository.findAll(returnedSpecification);
        return returneds.stream().map(returnedMapper::toDto).collect(Collectors.toList());
    }

    private Page<ReturnedDto> findPage(int page, int size, String sortBy, String order, ReturnedSearch search) {
        var direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, direction, sortBy);
        Specification<Returned> spec = ReturnedSpecification.bySearchCriteria(search);
        Page<Returned> paginatedReturneds = returnedRepository.findAll(spec, pageable);
        return paginatedReturneds.map(returnedMapper::toDto);
    }

}
