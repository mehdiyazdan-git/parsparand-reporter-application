package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public interface VATService {

    VATRate getApplicableVATRate(LocalDate date);

    Double calculateVAT(Double amount, VATRate vatRate);

    VATRate createVATRate(VATRate vatRate);

    VATRate updateVATRate(Long id, VATRate vatRate);

    List<VATRate> findAllVATRates();

    VATRate findVATRateById(Long id);

    void deleteVATRate(Long id);
}