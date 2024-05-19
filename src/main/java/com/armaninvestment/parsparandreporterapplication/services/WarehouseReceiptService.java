package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporterapplication.mappers.WarehouseReceiptMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.WarehouseReceiptRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.WarehouseReceiptSpecification;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseReceiptService {
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final WarehouseReceiptMapper warehouseReceiptMapper;
    private final YearRepository yearRepository;
    private final CustomerRepository customerRepository;

    public Page<WarehouseReceiptDto> findWarehouseReceiptByCriteria(WarehouseReceiptSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<WarehouseReceipt> specification = WarehouseReceiptSpecification.bySearchCriteria(search);
        return warehouseReceiptRepository.findAll(specification, pageRequest)
                .map(warehouseReceiptMapper::toDto);
    }
    public WarehouseReceiptDto getWarehouseReceiptById(Long id) {
        var warehouseReceiptEntity = warehouseReceiptRepository.findById(id).orElseThrow();
        return warehouseReceiptMapper.toDto(warehouseReceiptEntity);
    }
    @Transactional
    public WarehouseReceiptDto createWarehouseReceipt(WarehouseReceiptDto warehouseReceiptDto) {

        if (warehouseReceiptRepository.existsByWarehouseReceiptNumberAndYearId(warehouseReceiptDto.getWarehouseReceiptNumber(), warehouseReceiptDto.getYearId())) {
            throw new IllegalStateException("یک رسید انبار با این شماره برای سال مورد نظر قبلاً ثبت شده است.");
        }
        var warehouseReceiptEntity = warehouseReceiptMapper.toEntity(warehouseReceiptDto);
        var savedWarehouseReceipt = warehouseReceiptRepository.save(warehouseReceiptEntity);
        return warehouseReceiptMapper.toDto(savedWarehouseReceipt);
    }


    public WarehouseReceiptDto updateWarehouseReceipt(Long id, WarehouseReceiptDto warehouseReceiptDto) {
        var existingWarehouseReceipt = warehouseReceiptRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("رسید انبار پیدا نشد."));

        if (warehouseReceiptRepository.existsByWarehouseReceiptNumberAndYearIdAndIdNot(
                warehouseReceiptDto.getWarehouseReceiptNumber(), warehouseReceiptDto.getYearId(), id)) {
            throw new IllegalStateException("یک رسید انبار دیگر با این شماره برای سال مورد نظر وجود دارد.");
        }

        WarehouseReceipt partialUpdate = warehouseReceiptMapper.partialUpdate(warehouseReceiptDto, existingWarehouseReceipt);

        if (!partialUpdate.getYear().getId().equals(warehouseReceiptDto.getYearId())) {
            partialUpdate.setYear(yearRepository.findById(warehouseReceiptDto.getYearId())
                    .orElseThrow(() -> new IllegalStateException("سال یافت نشد.")));
        }

        if (!partialUpdate.getCustomer().getId().equals(warehouseReceiptDto.getCustomerId())) {
            partialUpdate.setCustomer(customerRepository.findById(warehouseReceiptDto.getCustomerId())
                    .orElseThrow(() -> new IllegalStateException("مشتری یافت نشد.")));
        }

        var updatedWarehouseReceipt = warehouseReceiptRepository.save(partialUpdate);
        return warehouseReceiptMapper.toDto(updatedWarehouseReceipt);
    }



    public void deleteWarehouseReceipt(Long id) {
        warehouseReceiptRepository.deleteById(id);
    }

    public String importWarehouseReceiptsFromExcel(MultipartFile file) throws IOException {
        List<WarehouseReceiptDto> warehouseReceiptDtos = ExcelDataImporter.importData(file, WarehouseReceiptDto.class);
        List<WarehouseReceipt> warehouseReceipts = warehouseReceiptDtos.stream().map(warehouseReceiptMapper::toEntity).collect(Collectors.toList());

        warehouseReceipts.forEach(warehouseReceipt -> {
            warehouseReceipt.setCustomer(customerRepository.findById(warehouseReceipt.getCustomer().getId()).orElseThrow(() -> new IllegalStateException("مشتری یافت نشد.")));
            warehouseReceipt.setYear(yearRepository.findById(warehouseReceipt.getYear().getId()).orElseThrow(() -> new IllegalStateException("سال یافت نشد.")));

        });
        warehouseReceiptRepository.saveAll(warehouseReceipts);
        return warehouseReceipts.size() + " رسید انبار با موفقیت وارد شد.";
    }

    public byte[] exportWarehouseReceiptsToExcel() throws IOException {
        List<WarehouseReceiptDto> warehouseReceiptDtos = warehouseReceiptRepository.findAll().stream().map(warehouseReceiptMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(warehouseReceiptDtos, WarehouseReceiptDto.class);
    }
}
