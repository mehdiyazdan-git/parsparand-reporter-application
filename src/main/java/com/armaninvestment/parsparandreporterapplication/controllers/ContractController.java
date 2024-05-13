package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ContractDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.ContractSearch;
import com.armaninvestment.parsparandreporterapplication.services.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @GetMapping(path = {"/contracts", ""})
    public ResponseEntity<Page<ContractDto>> getAllContractsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ContractSearch search) {
        Page<ContractDto> contracts = contractService.findContractByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<ContractDto> getContractById(@PathVariable Long id){
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PostMapping(path = {"/",""})
    public ResponseEntity<ContractDto> createContract(@RequestBody ContractDto contractDto){
        return ResponseEntity.ok(contractService.createContract(contractDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<ContractDto> updateContract(@PathVariable Long id, @RequestBody ContractDto contractDto){
        return ResponseEntity.ok(contractService.updateContract(id, contractDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteContract(@PathVariable Long id){
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-contracts.xlsx")
    public ResponseEntity<byte[]> downloadAllContractsExcel() throws IOException {
        byte[] excelData = contractService.exportContractsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_contracts.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importContractsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = contractService.importContractsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import contracts from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
