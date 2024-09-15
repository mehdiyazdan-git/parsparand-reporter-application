package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.VATRateDto;
import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import com.armaninvestment.parsparandreporterapplication.mappers.VATRateMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.VATRateRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.VATRateSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.VATRateSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VATService {

    private final VATRateRepository vatRateRepository;
    private final VATRateMapper vatRateMapper;

    public Page<VATRateDto> findVATRatesByCriteria(VATRateSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<VATRate> specification = VATRateSpecification.bySearchCriteria(search);
        return vatRateRepository.findAll(specification, pageRequest)
                .map(vatRateMapper::toDto);
    }

    /**
     * Finds VAT rates by a general search parameter (e.g., rate or effective date).
     *
     * @param searchParam the search string (could be rate or effective date)
     * @return a list of VATRateDto objects
     */
    public List<VATRateDto> vatRateSelect(String searchParam) {
        Specification<VATRate> specification = VATRateSpecification.getSelectSpecification(searchParam);
        return vatRateRepository
                .findAll(specification)
                .stream()
                .map(vatRateMapper::toDto)
                .collect(Collectors.toList());
    }

    public VATRate getApplicableVATRate(LocalDate date) {
        return vatRateRepository.findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(date)
                .orElseThrow(() -> new RuntimeException(String.format("No VAT rate found for date: %s", date)));
    }

    public Double calculateVAT(Double amount, VATRate vatRate) {
        return Math.round(amount * vatRate.getRate() / 100) / 100.0;
    }

    public VATRate createVATRate(VATRate vatRate) {
        return vatRateRepository.save(vatRate);
    }

    public VATRate updateVATRate(Long id, VATRate vatRate) {
        VATRate existingVATRate = vatRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("VAT rate not found with id: %s", id)));

        existingVATRate.setRate(vatRate.getRate());
        existingVATRate.setEffectiveFrom(vatRate.getEffectiveFrom());
        return vatRateRepository.save(existingVATRate);
    }

    public List<VATRate> findAllVATRates() {
        return vatRateRepository.findAll();
    }

    public VATRate findVATRateById(Long id) {
        return vatRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("VAT rate not found with id: %s", id)));
    }

    public void deleteVATRate(Long id) {
        vatRateRepository.deleteById(id);
    }

}
