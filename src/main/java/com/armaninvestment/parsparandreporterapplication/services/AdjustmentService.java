package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Adjustment;
import com.armaninvestment.parsparandreporterapplication.mappers.AdjustmentMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.AdjustmentRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.AdjustmentSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.AdjustmentSpecification;
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
public class AdjustmentService {
    private final AdjustmentRepository adjustmentRepository;
    private final AdjustmentMapper adjustmentMapper;

    public Page<AdjustmentDto> findAdjustmentByCriteria(AdjustmentSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Adjustment> specification = AdjustmentSpecification.bySearchCriteria(search);
        return adjustmentRepository.findAll(specification, pageRequest)
                .map(adjustmentMapper::toDto);
    }

    public AdjustmentDto createAdjustment(AdjustmentDto adjustmentDto) {
        var adjustmentEntity = adjustmentMapper.toEntity(adjustmentDto);
        var savedAdjustment = adjustmentRepository.save(adjustmentEntity);
        return adjustmentMapper.toDto(savedAdjustment);
    }

    public AdjustmentDto getAdjustmentById(Long id) {
        var adjustmentEntity = adjustmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("الحاقیه با این شناسه یافت نشد."));
        return adjustmentMapper.toDto(adjustmentEntity);
    }

    public AdjustmentDto updateAdjustment(Long id, AdjustmentDto adjustmentDto) {
        var existingAdjustment = adjustmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("الحاقیه با این شناسه یافت نشد."));

        adjustmentMapper.partialUpdate(adjustmentDto, existingAdjustment);
        var updatedAdjustment = adjustmentRepository.save(existingAdjustment);
        return adjustmentMapper.toDto(updatedAdjustment);
    }

    public void deleteAdjustment(Long id) {
        if (!adjustmentRepository.existsById(id)) {
            throw new IllegalStateException("الحاقیه با این شناسه یافت نشد.");
        }
        adjustmentRepository.deleteById(id);
    }

    public String importAdjustmentsFromExcel(MultipartFile file) throws IOException {
        List<AdjustmentDto> adjustmentDtos = ExcelDataImporter.importData(file, AdjustmentDto.class);
        List<Adjustment> adjustments = adjustmentDtos.stream()
                .map(adjustmentMapper::toEntity)
                .collect(Collectors.toList());

        adjustmentRepository.saveAll(adjustments);
        return adjustments.size() + " الحاقیه با موفقیت وارد شدند.";
    }

    public byte[] exportAdjustmentsToExcel() throws IOException {
        List<AdjustmentDto> adjustmentDtos = adjustmentRepository.findAll().stream()
                .map(adjustmentMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(adjustmentDtos, AdjustmentDto.class);
    }
}
