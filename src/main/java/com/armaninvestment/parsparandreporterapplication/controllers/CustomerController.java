package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.CustomerDto;
import com.armaninvestment.parsparandreporterapplication.dtos.CustomerSelect;
import com.armaninvestment.parsparandreporterapplication.searchForms.CustomerSearch;
import com.armaninvestment.parsparandreporterapplication.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping(path = {"/customers", "/customers/"})
    public ResponseEntity<Page<CustomerDto>> getAllCustomersByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            CustomerSearch search) {
        Page<CustomerDto> customers = customerService.findCustomerByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(customers);
    }
    @GetMapping(path = "/select")
    public ResponseEntity<List<CustomerSelect>> findAllCustomerSelect(
            @RequestParam(required = false) String searchQuery) {
        List<CustomerSelect> customers = customerService.findAllCustomerSelect(searchQuery);
        return ResponseEntity.ok(customers);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id){
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerDto customerDto){
        return ResponseEntity.ok(customerService.createCustomer(customerDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto customerDto){
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id){
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-customers.xlsx")
    public ResponseEntity<byte[]> downloadAllCustomersExcel() throws IOException {
        byte[] excelData = customerService.exportCustomersToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_customers.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCustomersFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = customerService.importCustomersFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import customers from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
