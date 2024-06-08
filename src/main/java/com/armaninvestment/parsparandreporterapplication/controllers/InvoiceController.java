package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.entities.InvoiceSelectDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<InvoiceDto>> getAllInvoicesByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            InvoiceSearch search
    ) {
        Page<InvoiceDto> invoices = invoiceService.findAll(page, size, sortBy, order, search);
        return ResponseEntity.ok(invoices);
    }
    @GetMapping("/select")
    public ResponseEntity<?> searchInvoices(
            @RequestParam String searchQuery,
            @RequestParam Integer jalaliYear) {

        try {
            List<InvoiceSelectDto> invoices = invoiceService.searchInvoiceByDescriptionKeywords(searchQuery, jalaliYear);
            return ResponseEntity.ok(invoices);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("در جستجو فاکتور خطایی رخ داده است");
        }
    }

    @GetMapping("/download-all-invoices.xlsx")
    public ResponseEntity<byte[]> downloadAllInvoicesExcel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            InvoiceSearch search,
            @RequestParam(defaultValue = "false") boolean exportAll
    ) {
        search.setPage(page);
        search.setSize(size);
        search.setSortBy(sortBy);
        search.setOrder(order);
        byte[] excelData = invoiceService.exportInvoicesToExcel(search,exportAll);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_invoices.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id){
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createInvoice(@RequestBody InvoiceDto invoiceDto){
        try {
            return ResponseEntity.ok(invoiceService.createInvoice(invoiceDto));
        }catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("در ایجاد فاکتور خطایی رخ داده است");
        }
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody InvoiceDto invoiceDto){
        try {
            InvoiceDto updateInvoice = invoiceService.updateInvoice(id, invoiceDto);
            return ResponseEntity.status(HttpStatus.OK).body(updateInvoice);
        }catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("در ویرایش فاکتور خطایی رخ داده است");
        }
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id){
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body("در حذف فاکتور خطایی رخ داده است");
        }
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
