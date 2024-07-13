package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReturnedSearch;
import com.armaninvestment.parsparandreporterapplication.services.ReturnedService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
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
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(ReturnedController.class);

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<ReturnedDto>> getAllReturnedsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ReturnedSearch search) {
        Page<ReturnedDto> returneds = returnedService.findReturnedByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(returneds);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<?> getReturnedById(@PathVariable Long id){
        try {
            return ResponseEntity.ok(returnedService.getReturnedById(id));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createReturned(@RequestBody ReturnedDto returnedDto){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(returnedService.createReturned(returnedDto));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<?> updateReturned(@PathVariable Long id, @RequestBody ReturnedDto returnedDto){
        try {
            return ResponseEntity.ok(returnedService.updateReturned(id, returnedDto));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<?> deleteReturned(@PathVariable Long id){
        try {
            returnedService.deleteReturned(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){

            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/download-all-returneds.xlsx")
    public ResponseEntity<byte[]> downloadAllReturnedsExcel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ReturnedSearch search,
            @RequestParam(defaultValue = "false") boolean exportAll
    ) throws IOException {
        search.setPage(page);
        search.setSize(size);
        search.setSortBy(sortBy);
        search.setOrder(order);
        byte[] excelData = returnedService.exportReturnedsToExcel(search, exportAll);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
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

            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import returners from Excel file: " + e.getMessage());
        } catch (Exception e) {

            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
