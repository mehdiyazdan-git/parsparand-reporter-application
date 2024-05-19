package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReturnedSearch;
import com.armaninvestment.parsparandreporterapplication.services.ReturnedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/returneds")
@RequiredArgsConstructor
public class ReturnedController {
    private final ReturnedService returnedService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<ReturnedDto>> getAllReturnedsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ReturnedSearch search) {
        Page<ReturnedDto> returneds = returnedService.findReturnedByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(returneds);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<ReturnedDto> getReturnedById(@PathVariable Long id){
        return ResponseEntity.ok(returnedService.getReturnedById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<ReturnedDto> createReturned(@RequestBody ReturnedDto returnedDto){
        return ResponseEntity.ok(returnedService.createReturned(returnedDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<ReturnedDto> updateReturned(@PathVariable Long id, @RequestBody ReturnedDto returnedDto){
        return ResponseEntity.ok(returnedService.updateReturned(id, returnedDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteReturned(@PathVariable Long id){
        returnedService.deleteReturned(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-returneds.xlsx")
    public ResponseEntity<byte[]> downloadAllReturnedsExcel() throws IOException {
        byte[] excelData = returnedService.exportReturnedsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_returneds.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importReturnedsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = returnedService.importReturnedsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import returners from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
