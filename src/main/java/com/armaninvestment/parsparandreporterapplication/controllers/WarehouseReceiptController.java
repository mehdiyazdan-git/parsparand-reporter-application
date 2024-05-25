package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ProductSelectDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptSelect;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import com.armaninvestment.parsparandreporterapplication.services.WarehouseReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
        Page<WarehouseReceiptDto> warehouseReceipts = warehouseReceiptService.findWarehouseReceiptByCriteria(search, page, size, sortBy, order);
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
             e.printStackTrace();
           return ResponseEntity.badRequest().body(null);
       }
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<WarehouseReceiptDto> createWarehouseReceipt(@RequestBody WarehouseReceiptDto warehouseReceiptDto){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(warehouseReceiptService.createWarehouseReceipt(warehouseReceiptDto));
        }catch (Exception e){
            e.printStackTrace();
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
    public ResponseEntity<byte[]> downloadAllWarehouseReceiptsExcel() throws IOException {
        byte[] excelData = warehouseReceiptService.exportWarehouseReceiptsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_warehouse_receipts.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importWarehouseReceiptsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = warehouseReceiptService.importWarehouseReceiptsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import warehouse receipts from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
