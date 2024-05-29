package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Adjustment;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.mappers.AdjustmentMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.AdjustmentRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.AdjustmentSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.AdjustmentSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdjustmentService {
    private final AdjustmentRepository adjustmentRepository;
    private final AdjustmentMapper adjustmentMapper;
    private final YearRepository yearRepository;

    public Page<AdjustmentDto> findAdjustmentByCriteria(AdjustmentSearch search, int page, int size, String sortBy, String order) {
        Sort sort;
        if ("totalPrice".equals(sortBy)) {
            sort = Sort.unsorted(); // We'll handle this manually
        } else {
            sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Adjustment> specification = AdjustmentSpecification.bySearchCriteria(search);

        return adjustmentRepository.findAll((root, query, criteriaBuilder) -> {
            query.where(specification.toPredicate(root, query, criteriaBuilder));
            if ("totalPrice".equals(sortBy)) {
                // Add custom sorting logic for totalPrice
                if ("asc".equalsIgnoreCase(order)) {
                    query.orderBy(criteriaBuilder.asc(criteriaBuilder.prod(root.get("unitPrice"), root.get("quantity"))));
                } else {
                    query.orderBy(criteriaBuilder.desc(criteriaBuilder.prod(root.get("unitPrice"), root.get("quantity"))));
                }
            }
            return query.getRestriction();
        }, pageRequest).map(adjustmentMapper::toDto);
    }
    private String calculateJalaliDate(Adjustment adjustment) {
        return DateConvertor.convertGregorianToJalali(adjustment.getAdjustmentDate());
    }
    private String calculateJalaliYear(Adjustment adjustment) {
        return calculateJalaliDate(adjustment).substring(0, 4);
    }
    private Year calculateYear(Adjustment adjustment) {
        Optional<Year> optionalYear = yearRepository.findByName(Long.valueOf(calculateJalaliYear(adjustment)));
        return optionalYear.orElseThrow();
    }


    public AdjustmentDto createAdjustment(AdjustmentDto adjustmentDto) {
        var adjustmentEntity = adjustmentMapper.toEntity(adjustmentDto);
        Year year = DateConvertor.findYearFromLocalDate(adjustmentEntity.getAdjustmentDate());
        adjustmentEntity.setYear(year);
        var savedAdjustment = adjustmentRepository.save(adjustmentEntity);
        return adjustmentMapper.toDto(savedAdjustment);
    }

    private Adjustment findAdjustmentById(Long adjustmentId){
        return adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new IllegalStateException("سند تعدیل با این شناسه یافت نشد."));
    }

    public AdjustmentDto getAdjustmentById(Long id) {
        var adjustmentEntity = findAdjustmentById(id);
        return adjustmentMapper.toDto(adjustmentEntity);
    }

    public AdjustmentDto updateAdjustment(Long id, AdjustmentDto adjustmentDto) {

        var existingAdjustment = findAdjustmentById(id);
        Adjustment partialedUpdate = adjustmentMapper.partialUpdate(adjustmentDto, existingAdjustment);
        Year year = DateConvertor.findYearFromLocalDate(partialedUpdate.getAdjustmentDate());
        partialedUpdate.setYear(year);
        var updatedAdjustment = adjustmentRepository.save(partialedUpdate);
        return adjustmentMapper.toDto(updatedAdjustment);
    }

    public void deleteAdjustment(Long id) {
        if (!adjustmentRepository.existsById(id)) {
            throw new IllegalStateException("سند تعدیل با این شناسه یافت نشد.");
        }
        adjustmentRepository.deleteById(id);
    }

    public String importAdjustmentsFromExcel(MultipartFile file) throws IOException {
        List<AdjustmentDto> adjustmentDtos = ExcelDataImporter.importData(file, AdjustmentDto.class);
        List<Adjustment> adjustments = adjustmentDtos.stream()
                .map(adjustmentMapper::toEntity)
                .collect(Collectors.toList());

        adjustmentRepository.saveAll(adjustments);
        return adjustments.size() + " سند تعدیل با موفقیت وارد شدند.";
    }

    public byte[] exportAdjustmentsToExcel() throws IOException {
        List<AdjustmentDto> adjustmentDtos = adjustmentRepository.findAll().stream()
                .map(adjustmentMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(adjustmentDtos, AdjustmentDto.class);
    }
}
