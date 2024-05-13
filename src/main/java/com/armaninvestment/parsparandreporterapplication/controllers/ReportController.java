package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReportDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import com.armaninvestment.parsparandreporterapplication.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping(path = {"/reports", ""})
    public ResponseEntity<Page<ReportDto>> getAllReportsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ReportSearch search) {
        Page<ReportDto> reports = reportService.findReportByCriteria(search, page, size, sortBy, order);
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
    public ResponseEntity<byte[]> downloadAllReportsExcel() throws IOException {
        byte[] excelData = reportService.exportReportsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
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
}
