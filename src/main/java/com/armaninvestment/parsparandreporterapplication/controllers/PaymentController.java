package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.PaymentDto;
import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubject;
import com.armaninvestment.parsparandreporterapplication.searchForms.PaymentSearch;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import com.armaninvestment.parsparandreporterapplication.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    // log4j
        private static final Logger logger = LogManager.getLogger(PaymentController.class);

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<PaymentDto>> getAllPaymentsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            PaymentSearch search) {
        Page<PaymentDto> payments = paymentService.findPaymentByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(payments);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id){
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createPayment(@RequestBody PaymentDto paymentDto){
        try {
            return ResponseEntity.ok(paymentService.createPayment(paymentDto));
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<PaymentDto> updatePayment(@PathVariable Long id, @RequestBody PaymentDto paymentDto){
        return ResponseEntity.ok(paymentService.updatePayment(id, paymentDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deletePayment(@PathVariable Long id){
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-payments.xlsx")
    public ResponseEntity<byte[]> downloadAllPaymentsExcel(
            @RequestParam(defaultValue = "0",required = false) int page,
            @RequestParam(defaultValue = "5",required = false) int size,
            @RequestParam(defaultValue = "id",required = false) String sortBy,
            @RequestParam(defaultValue = "ASC",required = false) String order,
            @RequestParam(defaultValue = "false",required = false) boolean exportAll,
            PaymentSearch search
    ) throws IllegalAccessException {
        boolean _exportAll = Boolean.parseBoolean(String.valueOf(exportAll));
        int _size = Integer.parseInt(String.valueOf(size));
        int _page = Integer.parseInt(String.valueOf(page));
        search.setPage(_page);
        search.setSize(_size);
        search.setSortBy(sortBy);
        search.setOrder(order);

        System.out.println(search);

        byte[] excelData = paymentService.exportPaymentsToExcel(search, _exportAll);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_payments.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importPaymentsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = paymentService.importPaymentsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {

            logger.error("Failed to import payments from Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import payments from Excel file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to import payments from Excel file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
