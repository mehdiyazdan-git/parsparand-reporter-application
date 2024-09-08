package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import com.armaninvestment.parsparandreporterapplication.repositories.VATRateRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;


@RequiredArgsConstructor
public class VATServiceImpl implements VATService {

    private final VATRateRepository vatRateRepository;
    @Override
    public VATRate getApplicableVATRate(LocalDate date) {
        return vatRateRepository.findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(date)
                .orElseThrow(() -> new RuntimeException(String.format("No VAT rate found for date: %s", date)));
    }
    @Override
    public Double calculateVAT(Double amount, VATRate vatRate) {
        return Math.round(amount * vatRate.getRate() / 100) / 100.0;
    }
    @Override
    public VATRate createVATRate(VATRate vatRate) {
        return vatRateRepository.save(vatRate);
    }
    @Override
    public VATRate updateVATRate(Long id, VATRate vatRate) {
        VATRate existingVATRate = vatRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("VAT rate not found with id: %s", id)));

        existingVATRate.setRate(vatRate.getRate());
        existingVATRate.setEffectiveFrom(vatRate.getEffectiveFrom());
        existingVATRate.setEffectiveTo(vatRate.getEffectiveTo());
        return vatRateRepository.save(existingVATRate);
    }
    @Override
    public List<VATRate> findAllVATRates() {
        return vatRateRepository.findAll();
    }
    @Override
    public VATRate findVATRateById(Long id) {
        return vatRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("VAT rate not found with id: %s", id)));
    }
    @Override
    public void deleteVATRate(Long id) {
        vatRateRepository.deleteById(id);
    }

}
