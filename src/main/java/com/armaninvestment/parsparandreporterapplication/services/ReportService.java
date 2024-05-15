package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.ReportDto;
import com.armaninvestment.parsparandreporterapplication.entities.Report;
import com.armaninvestment.parsparandreporterapplication.entities.ReportItem;
import com.armaninvestment.parsparandreporterapplication.exceptions.ReportDateAlreadyExistsException;
import com.armaninvestment.parsparandreporterapplication.mappers.ReportMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.ReportRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.ReportItemRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ReportSpecification;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportItemRepository reportItemRepository;
    private final ReportMapper reportMapper;

    public Page<ReportDto> findReportByCriteria(ReportSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Report> specification = ReportSpecification.bySearchCriteria(search);
        return reportRepository.findAll(specification, pageRequest)
                .map(reportMapper::toDto);
    }

    public ReportDto createReport(ReportDto reportDto) {
        LocalDate reportDate = reportDto.getReportDate();
        if (reportRepository.existsByReportDate(reportDate)) {
            throw new IllegalStateException("یک گزارش با همین تاریخ قبلاً ثبت شده است.");
        }
        var reportEntity = reportMapper.toEntity(reportDto);
        var savedReport = reportRepository.save(reportEntity);
        return reportMapper.toDto(savedReport);
    }

    public ReportDto getReportById(Long id) {
        var reportEntity = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد."));
        return reportMapper.toDto(reportEntity);
    }

    public ReportDto updateReport(Long id, ReportDto reportDto) {
        var existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد."));

        LocalDate reportDate = reportDto.getReportDate();
        if (reportRepository.existsByReportDateAndIdNot(reportDate, id)) {
            throw new IllegalStateException("یک گزارش دیگر با همین تاریخ وجود دارد.");
        }

        reportMapper.partialUpdate(reportDto, existingReport);
        var updatedReport = reportRepository.save(existingReport);
        return reportMapper.toDto(updatedReport);
    }

    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد.");
        }
        reportRepository.deleteById(id);
    }

    public String importReportsFromExcel(MultipartFile file) throws IOException {
        List<ReportDto> reportDtos = ExcelDataImporter.importData(file, ReportDto.class);
        List<Report> reports = reportDtos.stream()
                .map(reportMapper::toEntity)
                .collect(Collectors.toList());

        for (Report report : reports) {
            if (reportRepository.existsByReportDate(report.getReportDate())) {
                throw new ReportDateAlreadyExistsException("گزارش با تاریخ " + report.getReportDate() + " قبلاً ثبت شده است.");
            }
            for (ReportItem reportItem : report.getReportItems()) {
                if (reportItemRepository.existsByWarehouseReceiptId(reportItem.getWarehouseReceipt().getId()))  {
                    throw new IllegalStateException("یک آیتم گزارش با این شماره رسید انبار قبلاً ثبت شده است.");
                }
            }
        }

        reportRepository.saveAll(reports);
        return reports.size() + " گزارش با موفقیت وارد شد.";
    }

    public byte[] exportReportsToExcel() throws IOException {
        List<ReportDto> reportDtos = reportRepository.findAll().stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(reportDtos, ReportDto.class);
    }
}
