package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.YearDto;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.YearSearch;
import com.armaninvestment.parsparandreporterapplication.services.YearService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/years")
@RequiredArgsConstructor
public class YearController {
    private final YearService yearService;
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(YearController.class);

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<YearDto>> getAllYearsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            YearSearch search) {
        Page<YearDto> years = yearService.findYearByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(years);
    }
    @GetMapping(path = "/select")
    public ResponseEntity<?> yearSelect() {
        return ResponseEntity.ok(yearService.yearSelect());
    }


    @GetMapping(path = {"/{id}"})
    public ResponseEntity<YearDto> getYearById(@PathVariable Long id){
        return ResponseEntity.ok(yearService.getYearById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<YearDto> createYear(@RequestBody YearDto yearDto){
        return ResponseEntity.ok(yearService.createYear(yearDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<YearDto> updateYear(@PathVariable Long id, @RequestBody YearDto yearDto){
        return ResponseEntity.ok(yearService.updateYear(id, yearDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteYear(@PathVariable Long id){
        yearService.deleteYear(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-years.xlsx")
    public ResponseEntity<byte[]> downloadAllYearsExcel() throws IOException {
        byte[] excelData = yearService.exportYearsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_years.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> importYearsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = yearService.importYearsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {

            logger.error("Error importing years from Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import years from Excel file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing Excel file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
