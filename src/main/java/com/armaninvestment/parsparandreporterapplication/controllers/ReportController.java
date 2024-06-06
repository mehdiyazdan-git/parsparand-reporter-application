package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.CompanyReportDTO;
import com.armaninvestment.parsparandreporterapplication.dtos.ReportDto;
import com.armaninvestment.parsparandreporterapplication.dtos.SalesByYearGroupByMonth;
import com.armaninvestment.parsparandreporterapplication.repositories.ReportRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import com.armaninvestment.parsparandreporterapplication.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<ReportDto>> getAllReportsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ReportSearch search) {
        Page<ReportDto> reports = reportService.findAll(page, size, sortBy, order, search);
        return ResponseEntity.ok(reports);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<ReportDto> getReportById(@PathVariable Long id){
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<ReportDto> createReport(@RequestBody ReportDto reportDto){
        return ResponseEntity.ok(reportService.createReport(reportDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<ReportDto> updateReport(@PathVariable Long id, @RequestBody ReportDto reportDto){
        return ResponseEntity.ok(reportService.updateReport(id, reportDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteReport(@PathVariable Long id){
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-reports.xlsx")
    public ResponseEntity<byte[]> downloadAllReportsExcel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ReportSearch search,
            @RequestParam(defaultValue = "true") boolean exportAll
    ) {
        search.setPage(page);
        search.setSize(size);
        search.setSortBy(sortBy);
        search.setOrder(order);
        byte[] excelData = reportService.exportReportsToExcel(search,exportAll);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_reports.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importReportsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = reportService.importReportsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import reports from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/by-product/{year}/{month}/{productType}")
    public ResponseEntity<List<CompanyReportDTO>> getMonthlyReportByProduct(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @PathVariable String productType) {

        List<Object[]> resultSet = reportRepository.getReport(year, month, productType);
        List<CompanyReportDTO> list = resultSet.stream().map(obj -> {
            CompanyReportDTO dto = new CompanyReportDTO();
            dto.setId((Long) obj[0]);
            dto.setCustomerName((String) obj[1]);
            dto.setTotalQuantity((Long) obj[2]);
            dto.setTotalAmount((Long) obj[3]);
            dto.setCumulativeTotalQuantity((Long) obj[4]);
            dto.setCumulativeTotalAmount((Long) obj[5]);
            dto.setAvgUnitPrice((Long) obj[6]);
            return dto;
        }).toList();
        return ResponseEntity.ok(list);
    }
    @GetMapping(path = "/sales-by-year/{yearName}/{productType}")
    public ResponseEntity<List<SalesByYearGroupByMonth>> getSalesByYearGroupByMonth(
            @PathVariable("yearName") Short yearName,
            @PathVariable("productType") String productType) {
        return ResponseEntity.ok(reportService.findSalesByYearGroupByMonth(yearName, productType));
    }
}
