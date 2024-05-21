package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporterapplication.entities.InvoiceStatus;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceStatusMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.InvoiceStatusRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceStatusSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.InvoiceStatusSpecification;
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
public class InvoiceStatusService {
    private final InvoiceStatusRepository invoiceStatusRepository;
    private final InvoiceStatusMapper invoiceStatusMapper;

    public Page<InvoiceStatusDto> findInvoiceStatusByCriteria(InvoiceStatusSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<InvoiceStatus> specification = InvoiceStatusSpecification.bySearchCriteria(search);
        return invoiceStatusRepository.findAll(specification, pageRequest)
                .map(invoiceStatusMapper::toDto);
    }
    public List<InvoiceStatusDto> invoiceStatusSelect(String searchParam) {
        Specification<InvoiceStatus> specification = InvoiceStatusSpecification.getSelectSpecification(searchParam);
        return invoiceStatusRepository
                .findAll(specification)
                .stream()
                .map(invoiceStatusMapper::toDto)
                .collect(Collectors.toList());
    }


    public InvoiceStatusDto createInvoiceStatus(InvoiceStatusDto invoiceStatusDto) {
        var invoiceStatusEntity = invoiceStatusMapper.toEntity(invoiceStatusDto);
        var savedInvoiceStatus = invoiceStatusRepository.save(invoiceStatusEntity);
        return invoiceStatusMapper.toDto(savedInvoiceStatus);
    }

    public InvoiceStatusDto getInvoiceStatusById(Integer id) {
        var invoiceStatusEntity = invoiceStatusRepository.findById(id).orElseThrow();
        return invoiceStatusMapper.toDto(invoiceStatusEntity);
    }

    public InvoiceStatusDto updateInvoiceStatus(Integer id, InvoiceStatusDto invoiceStatusDto) {
        var invoiceStatusEntity = invoiceStatusRepository.findById(id).orElseThrow();
        InvoiceStatus partialUpdate = invoiceStatusMapper.partialUpdate(invoiceStatusDto, invoiceStatusEntity);
        var updatedInvoiceStatus = invoiceStatusRepository.save(partialUpdate);
        return invoiceStatusMapper.toDto(updatedInvoiceStatus);
    }

    public void deleteInvoiceStatus(Integer id) {
        invoiceStatusRepository.deleteById(id);
    }

    public String importInvoiceStatusesFromExcel(MultipartFile file) throws IOException {
        List<InvoiceStatusDto> invoiceStatusDtos = ExcelDataImporter.importData(file, InvoiceStatusDto.class);
        List<InvoiceStatus> invoiceStatuses = invoiceStatusDtos.stream().map(invoiceStatusMapper::toEntity).collect(Collectors.toList());
        invoiceStatusRepository.saveAll(invoiceStatuses);
        return invoiceStatuses.size() + " invoice statuses have been imported successfully.";
    }

    public byte[] exportInvoiceStatusesToExcel() throws IOException {
        List<InvoiceStatusDto> invoiceStatusDtos = invoiceStatusRepository.findAll().stream().map(invoiceStatusMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(invoiceStatusDtos, InvoiceStatusDto.class);
    }
}
