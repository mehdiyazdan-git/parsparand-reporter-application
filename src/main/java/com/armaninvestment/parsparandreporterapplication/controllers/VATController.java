package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.VATRateDto;
import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import com.armaninvestment.parsparandreporterapplication.searchForms.VATRateSearch;
import com.armaninvestment.parsparandreporterapplication.services.VATService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@CrossOrigin
@RestController
@RequestMapping(path="/api/vat-rates")
@RequiredArgsConstructor
public class VATController {

    private final VATService vatService;

    /**
     * Retrieves a paginated list of VAT rates based on search criteria.
     *
     * @param page the page number
     * @param size the page size
     * @param sortBy the field to sort by
     * @param order the sort direction (ASC/DESC)
     * @param search the search criteria for filtering VAT rates
     * @return a page of VATRateDto objects
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<VATRateDto>> getAllVATRatesByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            VATRateSearch search) {
        Page<VATRateDto> vatRates = vatService.findVATRatesByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(vatRates);
    }

    /**
     * Retrieves a list of VAT rates based on a general search parameter.
     *
     * @param searchParam the search string (could be rate or effective date)
     * @return a list of VATRateDto objects
     */
    @GetMapping(path = {"/select"})
    public List<VATRateDto> vatRateSelect(@RequestParam(required = false) String searchParam) {
        return vatService.vatRateSelect(searchParam);
    }

    // Get applicable VAT rate for a specific date
    @GetMapping(path="/applicable")
    public ResponseEntity<VATRate> getApplicableVATRate(@RequestParam("date") String date) {
        LocalDate localDate = LocalDate.parse(date);
        VATRate vatRate = vatService.getApplicableVATRate(localDate);
        return ResponseEntity.ok(vatRate);
    }

    // Calculate VAT for a specific amount with a given VAT rate ID
    @GetMapping(path="/calculate")
    public ResponseEntity<Double> calculateVAT(@RequestParam("amount") Double amount, @RequestParam("vatRateId") Long vatRateId) {
        VATRate vatRate = vatService.findVATRateById(vatRateId);
        Double vatAmount = vatService.calculateVAT(amount, vatRate);
        return ResponseEntity.ok(vatAmount);
    }

    // Create a new VAT rate
    @PostMapping(path = {"/",""})
    public ResponseEntity<VATRate> createVATRate(@RequestBody VATRate vatRate) {
        VATRate createdVatRate = vatService.createVATRate(vatRate);
        return ResponseEntity.ok(createdVatRate);
    }

    // Update an existing VAT rate by ID
    @PutMapping(path = "/{id}")
    public ResponseEntity<VATRate> updateVATRate(@PathVariable Long id, @RequestBody VATRate vatRate) {
        VATRate updatedVatRate = vatService.updateVATRate(id, vatRate);
        return ResponseEntity.ok(updatedVatRate);
    }

    // Get a VAT rate by ID
    @GetMapping(path="/{id}")
    public ResponseEntity<VATRate> findVATRateById(@PathVariable Long id) {
        VATRate vatRate = vatService.findVATRateById(id);
        return ResponseEntity.ok(vatRate);
    }

    // Delete a VAT rate by ID
    @DeleteMapping(path="/{id}")
    public ResponseEntity<Void> deleteVATRate(@PathVariable Long id) {
        vatService.deleteVATRate(id);
        return ResponseEntity.noContent().build();
    }
}
