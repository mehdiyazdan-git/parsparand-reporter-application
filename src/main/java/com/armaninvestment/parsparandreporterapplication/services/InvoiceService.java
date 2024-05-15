package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.InvoiceItemRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.InvoiceRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.InvoiceSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemRepository invoiceItemRepository;

    public Page<InvoiceDto> findInvoiceByCriteria(InvoiceSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(search);
        return invoiceRepository.findAll(specification, pageRequest)
                .map(invoiceMapper::toDto);
    }

    public InvoiceDto getInvoiceById(Long id) {
        var invoiceEntity = invoiceRepository.findById(id).orElseThrow();
        return invoiceMapper.toDto(invoiceEntity);
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        validateInvoiceUniqueness(invoiceDto);

        var invoiceEntity = invoiceMapper.toEntity(invoiceDto);
        var savedInvoice = invoiceRepository.save(invoiceEntity);
        return invoiceMapper.toDto(savedInvoice);
    }

    @Transactional
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        var existingInvoice = invoiceRepository.findById(id).orElseThrow(() -> new IllegalStateException("صورت‌حساب پیدا نشد."));

        validateInvoiceUniquenessForUpdate(invoiceDto, id);

        var updatedInvoice = invoiceMapper.toEntity(invoiceDto);
        return invoiceMapper.toDto(invoiceRepository.save(updatedInvoice));
    }

    private void validateInvoiceUniqueness(InvoiceDto invoiceDto) {
        if (invoiceRepository.existsByInvoiceNumberAndYearId(invoiceDto.getInvoiceNumber(), invoiceDto.getYearId())) {
            throw new IllegalStateException("یک صورت‌حساب با این شماره صورت‌حساب برای سال مالی مورد نظر قبلاً ثبت شده است.");
        }
        validateReceiptIdUniqueness(invoiceDto);
    }

    private void validateInvoiceUniquenessForUpdate(InvoiceDto invoiceDto, Long id) {
        if (invoiceRepository.existsByInvoiceNumberAndYearIdAndIdNot(invoiceDto.getInvoiceNumber(),invoiceDto.getYearId(), id)) {
            throw new IllegalStateException("یک صورت‌حساب دیگر با این شماره صورت‌حساب برای سال مالی مورد نظر وجود دارد.");
        }
        validateReceiptIdUniqueness(invoiceDto);
    }

    private void validateReceiptIdUniqueness(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                if (invoiceItemRepository.existsByWarehouseReceiptId(invoiceItemDto.getWarehouseReceiptId())){
                    throw new IllegalStateException("برای این شماره حواله قبلا فاکتور صادر شده است.");
                }
            });
        }
    }
    @Transactional
    public String importInvoicesFromExcel(MultipartFile file) throws IOException {
        List<InvoiceDto> invoiceDtos = ExcelDataImporter.importData(file, InvoiceDto.class);
        Map<String, List<String>> errors = new HashMap<>();

        List<Invoice> invoices = invoiceDtos.stream().filter(invoice -> {
            try {
                validateInvoiceUniqueness(invoice);
                return true;
            } catch (IllegalStateException e) {
                errors.computeIfAbsent("خطاهای اعتبارسنجی", k -> new ArrayList<>()).add(e.getMessage());
                return false;
            }
        }).map(invoiceMapper::toEntity).collect(Collectors.toList());

        invoiceRepository.saveAll(invoices);
        return String.format("%d صورت‌حساب با موفقیت از اکسل وارد شد. تعداد %d خطا رخ داد: %s", invoices.size(), errors.size(), errors);
    }



    public byte[] exportInvoicesToExcel() throws IOException {
        List<InvoiceDto> invoiceDtos = invoiceRepository.findAll().stream().map(invoiceMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(invoiceDtos, InvoiceDto.class);
    }
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalStateException("صورت‌حساب پیدا نشد.");
        }
        invoiceRepository.deleteById(id);
    }
}
