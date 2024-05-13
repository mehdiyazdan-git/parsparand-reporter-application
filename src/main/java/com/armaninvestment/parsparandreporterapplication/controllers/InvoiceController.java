package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping(path = {"/invoices", ""})
    public ResponseEntity<Page<InvoiceDto>> getAllInvoicesByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            InvoiceSearch search) {
        Page<InvoiceDto> invoices = invoiceService.findInvoiceByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id){
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<InvoiceDto> createInvoice(@RequestBody InvoiceDto invoiceDto){
        return ResponseEntity.ok(invoiceService.createInvoice(invoiceDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id, @RequestBody InvoiceDto invoiceDto){
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoiceDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id){
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-invoices.xlsx")
    public ResponseEntity<byte[]> downloadAllInvoicesExcel() throws IOException {
        byte[] excelData = invoiceService.exportInvoicesToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_invoices.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importInvoicesFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = invoiceService.importInvoicesFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import invoices from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
