package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.PaymentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Payment;
import com.armaninvestment.parsparandreporterapplication.mappers.PaymentMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.PaymentRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.PaymentSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.PaymentSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public Page<PaymentDto> findPaymentByCriteria(PaymentSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Payment> specification = PaymentSpecification.bySearchCriteria(search);
        return paymentRepository.findAll(specification, pageRequest)
                .map(paymentMapper::toDto);
    }

    public PaymentDto createPayment(PaymentDto paymentDto) {
        var paymentEntity = paymentMapper.toEntity(paymentDto);
        var savedPayment = paymentRepository.save(paymentEntity);
        return paymentMapper.toDto(savedPayment);
    }

    public PaymentDto getPaymentById(Long id) {
        var paymentEntity = paymentRepository.findById(id).orElseThrow();
        return paymentMapper.toDto(paymentEntity);
    }

    public PaymentDto updatePayment(Long id, PaymentDto paymentDto) {
        var paymentEntity = paymentRepository.findById(id).orElseThrow();
        Payment partialUpdate = paymentMapper.partialUpdate(paymentDto, paymentEntity);
        var updatedPayment = paymentRepository.save(partialUpdate);
        return paymentMapper.toDto(updatedPayment);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public String importPaymentsFromExcel(MultipartFile file) throws IOException {
        List<PaymentDto> paymentDtos = ExcelDataImporter.importData(file, PaymentDto.class);
        List<Payment> payments = paymentDtos.stream().map(paymentMapper::toEntity).collect(Collectors.toList());
        paymentRepository.saveAll(payments);
        return payments.size() + " payments have been imported successfully.";
    }

    public byte[] exportPaymentsToExcel() throws IOException {
        List<PaymentDto> paymentDtos = paymentRepository.findAll().stream().map(paymentMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(paymentDtos, PaymentDto.class);
    }
}
