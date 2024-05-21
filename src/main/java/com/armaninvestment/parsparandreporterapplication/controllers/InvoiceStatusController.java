package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceStatusSearch;
import com.armaninvestment.parsparandreporterapplication.services.InvoiceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/invoice-statuses")
@RequiredArgsConstructor
public class InvoiceStatusController {
    private final InvoiceStatusService invoiceStatusService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<InvoiceStatusDto>> getAllInvoiceStatusesByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            InvoiceStatusSearch search) {
        Page<InvoiceStatusDto> invoiceStatuses = invoiceStatusService.findInvoiceStatusByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(invoiceStatuses);
    }
    @GetMapping(path = {"/select"})
    public List<InvoiceStatusDto> invoiceStatusSelect(@RequestParam(required = false) String searchParam) {
        return invoiceStatusService.invoiceStatusSelect(searchParam);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<InvoiceStatusDto> getInvoiceStatusById(@PathVariable Integer id){
        return ResponseEntity.ok(invoiceStatusService.getInvoiceStatusById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<InvoiceStatusDto> createInvoiceStatus(@RequestBody InvoiceStatusDto invoiceStatusDto){
        return ResponseEntity.ok(invoiceStatusService.createInvoiceStatus(invoiceStatusDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<InvoiceStatusDto> updateInvoiceStatus(@PathVariable Integer id, @RequestBody InvoiceStatusDto invoiceStatusDto){
        return ResponseEntity.ok(invoiceStatusService.updateInvoiceStatus(id, invoiceStatusDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteInvoiceStatus(@PathVariable Integer id){
        invoiceStatusService.deleteInvoiceStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-invoice-statuses.xlsx")
    public ResponseEntity<byte[]> downloadAllInvoiceStatusesExcel() throws IOException {
        byte[] excelData = invoiceStatusService.exportInvoiceStatusesToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_invoice_statuses.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importInvoiceStatusesFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = invoiceStatusService.importInvoiceStatusesFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import invoice statuses from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
