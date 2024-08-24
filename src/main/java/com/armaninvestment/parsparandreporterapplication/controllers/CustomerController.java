package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ClientSummaryResult;
import com.armaninvestment.parsparandreporterapplication.dtos.CustomerDto;
import com.armaninvestment.parsparandreporterapplication.dtos.CustomerSelect;
import com.armaninvestment.parsparandreporterapplication.searchForms.CustomerSearch;
import com.armaninvestment.parsparandreporterapplication.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
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
    //log4j
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(CustomerController.class);

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Page<CustomerDto>> getAllCustomersByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) Boolean bigCustomer, // Add this line to handle bigCustomer explicitly
            CustomerSearch search) {

        // Set the bigCustomer field in the search object
        search.setBigCustomer(bigCustomer != null ? bigCustomer : false);

        Page<CustomerDto> customers = customerService.findCustomerByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(customers);
    }
    @GetMapping(path = "/select")
    public ResponseEntity<List<CustomerSelect>> findAllCustomerSelect(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "bigCustomer",required = false) String sortBy,
            @RequestParam(defaultValue = "DESC",required = false) String order
    ) {
        List<CustomerSelect> customers = customerService.findAllCustomerSelect(searchQuery,sortBy,order);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/summary")
    public ClientSummaryResult getClientSummary(@RequestParam(required = false) Long customerId) {
        return customerService.getClientSummaryByCustomerId(customerId);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id){
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerDto customerDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(customerDto));
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

    @PostMapping("/upload")
    public ResponseEntity<?> importCustomersFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = customerService.importCustomersFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            logger.error("Failed to import customers from Excel file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import customers from Excel file: " + e.getMessage());
        } catch (Exception e) {

            logger.error("Failed to import customers from Excel file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
