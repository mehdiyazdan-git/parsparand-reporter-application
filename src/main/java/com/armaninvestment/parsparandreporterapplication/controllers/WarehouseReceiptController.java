package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptSelect;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import com.armaninvestment.parsparandreporterapplication.services.WarehouseReceiptService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/warehouse-receipts")
@RequiredArgsConstructor
public class WarehouseReceiptController {
    private final WarehouseReceiptService warehouseReceiptService;
    //log4j
        private static final Logger logger = LoggerFactory.getLogger(WarehouseReceiptController.class);

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<WarehouseReceiptDto>> getAllWarehouseReceiptsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            WarehouseReceiptSearch search) {

        // اگر سال درخواست شده مشخص نباشد از سال جاری استفاده می شود
        if (search.getJalaliYear() == null) {
            search.setJalaliYear(warehouseReceiptService.getCurrentYear());
        }
        Page<WarehouseReceiptDto> warehouseReceipts = warehouseReceiptService.findAll(page, size, sortBy, order, search);
        return ResponseEntity.ok(warehouseReceipts);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<WarehouseReceiptDto> getWarehouseReceiptById(@PathVariable Long id){
        return ResponseEntity.ok(warehouseReceiptService.getWarehouseReceiptById(id));
    }

    @GetMapping(path = "/select")
    public ResponseEntity<List<WarehouseReceiptSelect>> findAllWarehouseReceiptSelect(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Long yearId
            ) {
       try {
           return ResponseEntity.ok(warehouseReceiptService.findAllWarehouseReceiptSelect(searchQuery,yearId));
       }catch (Exception e){
               logger.error("Error occurred while fetching warehouse receipts: ", e);
           return ResponseEntity.badRequest().body(null);
       }
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<WarehouseReceiptDto> createWarehouseReceipt(@RequestBody WarehouseReceiptDto warehouseReceiptDto){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(warehouseReceiptService.createWarehouseReceipt(warehouseReceiptDto));
        }catch (Exception e){
            logger.error("Error occurred while creating warehouse receipt: ", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<WarehouseReceiptDto> updateWarehouseReceipt(@PathVariable Long id, @RequestBody WarehouseReceiptDto warehouseReceiptDto){
        return ResponseEntity.ok(warehouseReceiptService.updateWarehouseReceipt(id, warehouseReceiptDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteWarehouseReceipt(@PathVariable Long id){
        warehouseReceiptService.deleteWarehouseReceipt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-warehouse-receipts.xlsx")
    public ResponseEntity<byte[]> downloadAllWarehouseReceiptsExcel(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "5", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String order,
            @RequestParam(defaultValue = "false", required = false) boolean exportAll,
            WarehouseReceiptSearch search
    ) throws IllegalAccessException {
        boolean _exportAll = Boolean.parseBoolean(String.valueOf(exportAll));
        int _size = Integer.parseInt(String.valueOf(size));
        int _page = Integer.parseInt(String.valueOf(page));
        search.setPage(_page);
        search.setSize(_size);
        search.setSortBy(sortBy);
        search.setOrder(order);

        byte[] excelData = warehouseReceiptService.exportWarehouseReceiptsToExcel(search, _exportAll);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_warehouse_receipts.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importWarehouseReceiptsFromExcel(@RequestParam("file") MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        try {
            String list = warehouseReceiptService.importWarehouseReceiptsFromExcel(file);
            return ResponseEntity.ok().headers(headers).body(list);
        } catch (IOException e) {
            logger.error("Error occurred while importing warehouse receipts from Excel: ",e);
            String errorMessage = "خطا در بارگذاری: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(errorMessage);
        } catch (Exception e) {
            logger.error("Error occurred while importing warehouse receipts from Excel: ", e);
            String errorMessage = "خطا در پردازش فایل لکسل: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .headers(headers)
                    .body(errorMessage);
        }
    }
}
