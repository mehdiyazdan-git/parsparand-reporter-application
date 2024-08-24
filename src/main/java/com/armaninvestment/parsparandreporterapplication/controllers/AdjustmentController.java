package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.AdjustmentSearch;
import com.armaninvestment.parsparandreporterapplication.services.AdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@CrossOrigin
@RestController
@RequestMapping("/api/adjustments")
@RequiredArgsConstructor
public class AdjustmentController {
    private final AdjustmentService adjustmentService;
    // implement log4j
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AdjustmentController.class);

    @GetMapping
    public ResponseEntity<Page<AdjustmentDto>> getAllAdjustmentsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            AdjustmentSearch search) {
        Page<AdjustmentDto> adjustments = adjustmentService.findAdjustmentByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdjustmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adjustmentService.getAdjustmentById(id));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createAdjustment(@RequestBody AdjustmentDto adjustmentDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(adjustmentService.createAdjustment(adjustmentDto));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdjustment(@PathVariable Long id, @RequestBody AdjustmentDto adjustmentDto) {
        try {
            return ResponseEntity.ok(adjustmentService.updateAdjustment(id, adjustmentDto));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdjustment(@PathVariable Long id) {
        try {
            adjustmentService.deleteAdjustment(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/download-all-adjustments.xlsx")
    public ResponseEntity<byte[]> downloadAllAdjustmentsExcel() throws IOException {
        byte[] excelData = adjustmentService.exportAdjustmentsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_adjustments.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> importAdjustmentsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String result = adjustmentService.importAdjustmentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import adjustments from Excel file: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error processing Excel file: " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
