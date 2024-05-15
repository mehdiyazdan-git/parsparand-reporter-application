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

@RestController
@RequestMapping("/api/adjustments")
@RequiredArgsConstructor
public class AdjustmentController {
    private final AdjustmentService adjustmentService;

    @GetMapping
    public ResponseEntity<Page<AdjustmentDto>> getAllAdjustmentsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            AdjustmentSearch search) {
        Page<AdjustmentDto> adjustments = adjustmentService.findAdjustmentByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdjustmentDto> getAdjustmentById(@PathVariable Long id) {
        return ResponseEntity.ok(adjustmentService.getAdjustmentById(id));
    }

    @PostMapping
    public ResponseEntity<AdjustmentDto> createAdjustment(@RequestBody AdjustmentDto adjustmentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adjustmentService.createAdjustment(adjustmentDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdjustmentDto> updateAdjustment(@PathVariable Long id, @RequestBody AdjustmentDto adjustmentDto) {
        return ResponseEntity.ok(adjustmentService.updateAdjustment(id, adjustmentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdjustment(@PathVariable Long id) {
        adjustmentService.deleteAdjustment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-adjustments.xlsx")
    public ResponseEntity<byte[]> downloadAllAdjustmentsExcel() throws IOException {
        byte[] excelData = adjustmentService.exportAdjustmentsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_adjustments.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importAdjustmentsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String result = adjustmentService.importAdjustmentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import adjustments from Excel file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
