package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceMapper;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

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

    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {

        if (invoiceRepository.existsByInvoiceNumberAndYearId(invoiceDto.getInvoiceNumber(), invoiceDto.getYearId())) {
            throw new IllegalStateException("یک صورت‌حساب با این شماره صورت‌حساب برای سال مالی مورد نظر قبلاً ثبت شده است.");
        }

        var invoiceEntity = invoiceMapper.toEntity(invoiceDto);
        var savedInvoice = invoiceRepository.save(invoiceEntity);
        return invoiceMapper.toDto(savedInvoice);
    }


    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        var existingInvoice = invoiceRepository.findById(id).orElseThrow(() -> new IllegalStateException("صورت‌حساب پیدا نشد."));

        if (invoiceRepository.existsByInvoiceNumberAndYearIdAndIdNot(invoiceDto.getInvoiceNumber(), invoiceDto.getYearId(), id)) {
            throw new IllegalStateException("یک صورت‌حساب دیگر با این شماره صورت‌حساب برای سال مالی مورد نظر وجود دارد.");
        }

        Invoice partialUpdate = invoiceMapper.partialUpdate(invoiceDto, existingInvoice);
        var updatedInvoice = invoiceRepository.save(partialUpdate);
        return invoiceMapper.toDto(updatedInvoice);
    }


    public void deleteInvoice(Long id) {

        if (!invoiceRepository.existsById(id)) {
            throw new IllegalStateException("صورت‌حساب پیدا نشد.");
        }
        invoiceRepository.deleteById(id);
    }

    public String importInvoicesFromExcel(MultipartFile file) throws IOException {
        List<InvoiceDto> invoiceDtos = ExcelDataImporter.importData(file, InvoiceDto.class);
        List<Invoice> invoices = invoiceDtos.stream().map(invoiceMapper::toEntity).collect(Collectors.toList());
        invoiceRepository.saveAll(invoices);
        return invoices.size() + " invoices have been imported successfully.";
    }

    public byte[] exportInvoicesToExcel() throws IOException {
        List<InvoiceDto> invoiceDtos = invoiceRepository.findAll().stream().map(invoiceMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(invoiceDtos, InvoiceDto.class);
    }
}
