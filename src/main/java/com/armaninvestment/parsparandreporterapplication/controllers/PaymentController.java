package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.PaymentDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.PaymentSearch;
import com.armaninvestment.parsparandreporterapplication.services.PaymentService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<PaymentDto>> getAllPaymentsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
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
    public ResponseEntity<PaymentDto> createPayment(@RequestBody PaymentDto paymentDto){
        return ResponseEntity.ok(paymentService.createPayment(paymentDto));
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
    public ResponseEntity<byte[]> downloadAllPaymentsExcel() throws IOException {
        byte[] excelData = paymentService.exportPaymentsToExcel();
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import payments from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
