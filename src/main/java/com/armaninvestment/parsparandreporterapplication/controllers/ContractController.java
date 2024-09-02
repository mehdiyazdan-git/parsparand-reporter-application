package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ContractDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ContractSelectDto;
import com.armaninvestment.parsparandreporterapplication.exceptions.ConflictException;
import com.armaninvestment.parsparandreporterapplication.searchForms.ContractSearch;
import com.armaninvestment.parsparandreporterapplication.services.ContractService;
import com.armaninvestment.parsparandreporterapplication.utils.FileMediaType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;
    // log4j
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ContractController.class);

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<ContractDto>> getAllContractsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ContractSearch search) {
        Page<ContractDto> contracts = contractService.findAll(page, size, sortBy, order, search);
        return ResponseEntity.ok(contracts);
    }
    @GetMapping(path = "/select")
    public ResponseEntity<List<ContractSelectDto>> findAllContractSelect(
            @RequestParam(required = false) String searchQuery) {
        List<ContractSelectDto> users = contractService.findAllContractSelect(searchQuery);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<ContractDto> getContractById(@PathVariable Long id){
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createContract(@RequestBody ContractDto contractDto) {
        try {
            ContractDto createdContract = contractService.createContract(contractDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
        } catch (ConflictException e) {
            logger.error("Conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطایی در سرور رخ داد.");
        }
    }




    @PutMapping(path = {"/{id}"})
    public ResponseEntity<?> updateContract(@PathVariable Long id, @RequestBody ContractDto contractDto) {
        try {
            ContractDto updatedContract = contractService.updateContract(id, contractDto);
            return ResponseEntity.ok(updatedContract);
        } catch (EntityNotFoundException e) {
            logger.error("Contract not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Invalid state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error(String.format("An error occurred: %s", e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطایی در سرور رخ داد.");
        }
    }


    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteContract(@PathVariable Long id){
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-contracts.xlsx")
    public ResponseEntity<byte[]> downloadAllContractsExcel(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "5", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String order,
            @RequestParam(defaultValue = "false", required = false) boolean exportAll,
            ContractSearch search
    ) throws IllegalAccessException {
        boolean _exportAll = Boolean.parseBoolean(String.valueOf(exportAll));
        int _size = Integer.parseInt(String.valueOf(size));
        int _page = Integer.parseInt(String.valueOf(page));
        search.setPage(_page);
        search.setSize(_size);
        search.setSortBy(sortBy);
        search.setOrder(order);
        byte[] excelBytes = contractService.exportContractsToExcel(search,_exportAll);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contracts.xlsx");
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> importContractsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = contractService.importContractsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import contracts from Excel file: " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
