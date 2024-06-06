package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import com.armaninvestment.parsparandreporterapplication.mappers.ReturnedMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.ReturnedRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReturnedSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ReturnedSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnedService {
    private final ReturnedRepository returnedRepository;
    private final ReturnedMapper returnedMapper;

    public Page<ReturnedDto> findReturnedByCriteria(ReturnedSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Returned> specification = ReturnedSpecification.bySearchCriteria(search);
        return returnedRepository.findAll(specification, pageRequest)
                .map(returnedMapper::toDto);
    }

    public ReturnedDto createReturned(ReturnedDto returnedDto) {
        var returnedEntity = returnedMapper.toEntity(returnedDto);
        var savedReturned = returnedRepository.save(returnedEntity);
        return returnedMapper.toDto(savedReturned);
    }

    public ReturnedDto getReturnedById(Long id) {
        var returnedEntity = returnedRepository.findById(id).orElseThrow();
        return returnedMapper.toDto(returnedEntity);
    }

    public ReturnedDto updateReturned(Long id, ReturnedDto returnedDto) {
        var returnedEntity = returnedRepository.findById(id).orElseThrow();
        Returned partialUpdate = returnedMapper.partialUpdate(returnedDto, returnedEntity);
        var updatedReturned = returnedRepository.save(partialUpdate);
        return returnedMapper.toDto(updatedReturned);
    }

    public void deleteReturned(Long id) {
        returnedRepository.deleteById(id);
    }

    public String importReturnedsFromExcel(MultipartFile file) throws IOException {
        List<ReturnedDto> returnedDtos = ExcelDataImporter.importData(file, ReturnedDto.class);
        List<Returned> returneds = returnedDtos.stream().map(returnedMapper::toEntity).collect(Collectors.toList());
        returnedRepository.saveAll(returneds);
        return returneds.size() + " returneds have been imported successfully.";
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
            throw new RuntimeException("Failed to export returneds to Excel", e);
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
