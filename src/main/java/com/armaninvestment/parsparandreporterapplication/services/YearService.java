package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.YearDto;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporterapplication.mappers.YearMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.YearSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.YearSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YearService {
    private final YearRepository yearRepository;
    private final YearMapper yearMapper;
    private final PaymentRepository paymentRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final ContractRepository contractRepository;
    private final ReportRepository reportRepository;
    private final InvoiceRepository invoiceRepository;

    public Page<YearDto> findYearByCriteria(YearSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Year> specification = YearSpecification.bySearchCriteria(search);
        return yearRepository.findAll(specification, pageRequest)
                .map(yearMapper::toDto);
    }

    public YearDto createYear(YearDto yearDto) {
        var yearEntity = yearMapper.toEntity(yearDto);
        var savedYear = yearRepository.save(yearEntity);
        return yearMapper.toDto(savedYear);
    }

    public YearDto getYearById(Long id) {
        var yearEntity = yearRepository.findById(id).orElseThrow();
        return yearMapper.toDto(yearEntity);
    }

    public YearDto updateYear(Long id, YearDto yearDto) {
        var yearEntity = yearRepository.findById(id).orElseThrow();
        Year partialUpdate = yearMapper.partialUpdate(yearDto, yearEntity);
        var updatedYear = yearRepository.save(partialUpdate);
        return yearMapper.toDto(updatedYear);
    }

    public void deleteYear(Long id) {
        Optional<Year> optionalYear = yearRepository.findById(id);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + id + "یافت نشد.");
        }
        Year year = optionalYear.get();

        if (paymentRepository.existsByYearId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون پرداخت‌های مرتبط دارد.");
        }
        if (warehouseReceiptRepository.existsByYearId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون آیتم‌های حواله های مرتبط دارد.");
        }
        if (contractRepository.existsByYearId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون قراردادهای مرتبط دارد.");
        }
        if (reportRepository.existsByYearId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون گزارش های مرتبط دارد.");
        }
        if (invoiceRepository.existsByYearId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون فاکتور های مرتبط دارد.");
        }
    }

    public String importYearsFromExcel(MultipartFile file) throws IOException {
        List<YearDto> yearDtos = ExcelDataImporter.importData(file, YearDto.class);
        List<Year> years = yearDtos.stream().map(yearMapper::toEntity).collect(Collectors.toList());
        yearRepository.saveAll(years);
        return years.size() + " سال با موفقیت از اکسل وارد شد.";
    }

    public byte[] exportYearsToExcel() throws IOException {
        List<YearDto> yearDtos = yearRepository.findAll().stream().map(yearMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(yearDtos, YearDto.class);
    }
}
