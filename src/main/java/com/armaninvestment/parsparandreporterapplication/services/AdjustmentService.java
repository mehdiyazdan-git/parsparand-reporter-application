package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Adjustment;
import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.enums.AdjustmentType;
import com.armaninvestment.parsparandreporterapplication.mappers.AdjustmentMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.AdjustmentRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.InvoiceRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.AdjustmentSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.AdjustmentSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
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
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdjustmentService {
    private final AdjustmentRepository adjustmentRepository;
    private final AdjustmentMapper adjustmentMapper;
    private final YearRepository yearRepository;
    private final InvoiceRepository invoiceRepository;

    public Page<AdjustmentDto> findAdjustmentByCriteria(AdjustmentSearch search, int page, int size, String sortBy, String order) {
        Sort sort;
        if ("totalPrice".equals(sortBy)) {
            sort = Sort.unsorted(); // We'll handle this manually
        } else {
            sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Adjustment> specification = AdjustmentSpecification.bySearchCriteria(search);

        return adjustmentRepository.findAll((root, query, criteriaBuilder) -> {
            query.where(specification.toPredicate(root, query, criteriaBuilder));
            if ("totalPrice".equals(sortBy)) {
                // Add custom sorting logic for totalPrice
                if ("asc".equalsIgnoreCase(order)) {
                    query.orderBy(criteriaBuilder.asc(criteriaBuilder.prod(root.get("unitPrice"), root.get("quantity"))));
                } else {
                    query.orderBy(criteriaBuilder.desc(criteriaBuilder.prod(root.get("unitPrice"), root.get("quantity"))));
                }
            }
            return query.getRestriction();
        }, pageRequest).map(adjustmentMapper::toDto);
    }
    private String calculateJalaliDate(Adjustment adjustment) {
        return DateConvertor.convertGregorianToJalali(adjustment.getAdjustmentDate());
    }
    private String calculateJalaliYear(Adjustment adjustment) {
        return calculateJalaliDate(adjustment).substring(0, 4);
    }
    private Year calculateYear(Adjustment adjustment) {
        Optional<Year> optionalYear = yearRepository.findByName(Long.valueOf(calculateJalaliYear(adjustment)));
        return optionalYear.orElseThrow();
    }


    public AdjustmentDto createAdjustment(AdjustmentDto adjustmentDto) {
        var adjustmentEntity = adjustmentMapper.toEntity(adjustmentDto);
        Year year = DateConvertor.findYearFromLocalDate(adjustmentEntity.getAdjustmentDate());
        adjustmentEntity.setYear(year);
        var savedAdjustment = adjustmentRepository.save(adjustmentEntity);
        return adjustmentMapper.toDto(savedAdjustment);
    }

    private Adjustment findAdjustmentById(Long adjustmentId){
        return adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new IllegalStateException("سند تعدیل با این شناسه یافت نشد."));
    }

    public AdjustmentDto getAdjustmentById(Long id) {
        var adjustmentEntity = findAdjustmentById(id);
        return adjustmentMapper.toDto(adjustmentEntity);
    }

    public AdjustmentDto updateAdjustment(Long id, AdjustmentDto adjustmentDto) {

        var existingAdjustment = findAdjustmentById(id);
        Adjustment partialedUpdate = adjustmentMapper.partialUpdate(adjustmentDto, existingAdjustment);
        Year year = DateConvertor.findYearFromLocalDate(partialedUpdate.getAdjustmentDate());
        partialedUpdate.setYear(year);
        var updatedAdjustment = adjustmentRepository.save(partialedUpdate);
        return adjustmentMapper.toDto(updatedAdjustment);
    }



    public void deleteAdjustment(Long id) {
        if (!adjustmentRepository.existsById(id)) {
            throw new IllegalStateException("سند تعدیل با این شناسه یافت نشد.");
        }
        adjustmentRepository.deleteById(id);
    }

    @Transactional
    public String importAdjustmentsFromExcel(MultipartFile file) throws IOException {
        List<AdjustmentDto> adjustmentDtoList = new ArrayList<>();

        Map<String, Invoice> invoicesMap = invoiceRepository.findAll().stream()
                .collect(Collectors.toMap(
                        i -> i.getInvoiceNumber() + "-" + i.getIssuedDate(),
                        i -> i,
                        (existing, replacement) -> existing
                ));
        BiFunction<Long, LocalDate, Invoice> longLocalDateInvoiceNumberBiFunction = (invoiceNumber, invoiceDate) -> {
            return invoicesMap.get(invoiceNumber + "-" + invoiceDate);
        };

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header row
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                AdjustmentDto adjustmentDto = new AdjustmentDto();

                adjustmentDto.setAdjustmentType(AdjustmentType.fromValue(row.getCell(0).getStringCellValue()));
                adjustmentDto.setDescription(row.getCell(1).getStringCellValue());
                adjustmentDto.setQuantity((long) row.getCell(2).getNumericCellValue());
                adjustmentDto.setUnitPrice(row.getCell(3).getNumericCellValue());
                adjustmentDto.setTotalPrice(row.getCell(4).getNumericCellValue());
                adjustmentDto.setAdjustmentDate(DateConvertor.convertJalaliToGregorian(row.getCell(5).getStringCellValue()));
                adjustmentDto.setAdjustmentNumber((long) row.getCell(6).getNumericCellValue());
                Year year = yearRepository
                        .findByName(Long.valueOf(row.getCell(5)
                                .getStringCellValue().substring(0, 4))).orElseThrow(() -> new IllegalStateException("سال با این شناسه یافت نشد."));
                adjustmentDto.setYearId(year.getId());
                adjustmentDtoList.add(adjustmentDto);
            }
        }

        List<Adjustment> list = adjustmentDtoList.stream().map(adjustmentDto -> {
            Adjustment adjustment = adjustmentMapper.toEntity(adjustmentDto);
            Year year = yearRepository.findById(adjustmentDto.getYearId()).orElseThrow(() -> new IllegalStateException("سال با این شناسه یافت نشد."));
            adjustment.setYear(year);
            year.getAdjustments().add(adjustment);
            Invoice invoice = longLocalDateInvoiceNumberBiFunction.apply(adjustmentDto.getAdjustmentNumber(), adjustmentDto.getAdjustmentDate());
            adjustment.setInvoice(invoice);
            invoice.getAdjustments().add(adjustment);
            return adjustment;
            }).toList();
            adjustmentRepository.saveAll(list);
            return "Imported " + adjustmentDtoList.size() + " adjustment records successfully.";
    }

    public byte[] exportAdjustmentsToExcel() throws IOException {
        List<AdjustmentDto> adjustmentDtos = adjustmentRepository.findAll().stream()
                .map(adjustmentMapper::toDto)
                .toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Adjustments");
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
            for (AdjustmentDto adjustment : adjustmentDtos) {
                Row row = sheet.createRow(rowNum++);
                populateAdjustmentRow(adjustment, row);

                // Sum totals
                totalQuantity += adjustment.getQuantity() != null ? adjustment.getQuantity() : 0;
                totalPrice += adjustment.getUnitPrice() != null ? (adjustment.getUnitPrice() * (adjustment.getQuantity() != null ? adjustment.getQuantity() : 0)) : 0;
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
            throw new RuntimeException("Failed to export adjustments to Excel", e);
        }
    }

    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "شناسه تعدیل", "نوع تعدیل", "شرح", "تعداد", "قیمت واحد",
                "شناسه فاکتور", "تاریخ تعدیل", "شماره تعدیل", "شناسه سال"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void populateAdjustmentRow(AdjustmentDto adjustment, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(adjustment.getId() != null ? adjustment.getId() : 0);
        row.createCell(cellNum++).setCellValue(adjustment.getAdjustmentType() != null ? adjustment.getAdjustmentType().toString() : "");
        row.createCell(cellNum++).setCellValue(adjustment.getDescription() != null ? adjustment.getDescription() : "");
        row.createCell(cellNum++).setCellValue(adjustment.getQuantity() != null ? adjustment.getQuantity() : 0);
        row.createCell(cellNum++).setCellValue(adjustment.getUnitPrice() != null ? adjustment.getUnitPrice() : 0.0);
        row.createCell(cellNum++).setCellValue(adjustment.getInvoiceId() != null ? adjustment.getInvoiceId() : 0);
        row.createCell(cellNum++).setCellValue(adjustment.getAdjustmentDate() != null ? DateConvertor.convertGregorianToJalali(adjustment.getAdjustmentDate()) : "");
        row.createCell(cellNum++).setCellValue(adjustment.getAdjustmentNumber() != null ? adjustment.getAdjustmentNumber() : 0);
        row.createCell(cellNum++).setCellValue(adjustment.getYearId() != null ? adjustment.getYearId() : 0);

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());

        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            cell.setCellStyle(defaultCellStyle);
        }
        row.getCell(4).setCellStyle(monatoryCellStyle);
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
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue(totalQuantity);
        subtotalRow.createCell(cellNum++).setCellValue(totalPrice);

        // Merge cells [0, 5]
        subtotalRow.getSheet().addMergedRegion(new CellRangeAddress(subtotalRow.getRowNum(), subtotalRow.getRowNum(), 0, 5));

        for (int i = 0; i < subtotalRow.getLastCellNum(); i++) {
            Cell cell = subtotalRow.getCell(i);
            if (cell != null) {
                cell.setCellStyle(footerCellStyle);
            }
        }
        subtotalRow.getCell(6).setCellStyle(footerCellStyle);
        subtotalRow.getCell(7).setCellStyle(footerCellStyle);
    }

    private List<AdjustmentDto> findAll() {
        return adjustmentRepository.findAll().stream()
                .map(adjustmentMapper::toDto)
                .collect(Collectors.toList());
    }

}
