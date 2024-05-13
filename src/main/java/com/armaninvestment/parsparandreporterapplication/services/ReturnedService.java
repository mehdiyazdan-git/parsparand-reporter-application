package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import com.armaninvestment.parsparandreporterapplication.mappers.ReturnedMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.ReturnedRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReturnedSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ReturnedSpecification;
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
public class ReturnedService {
    private final ReturnedRepository returnedRepository;
    private final ReturnedMapper returnedMapper;

    public Page<ReturnedDto> findReturnedByCriteria(ReturnedSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Returned> specification = ReturnedSpecification.bySearchCriteria(search);
        return returnedRepository.findAll(specification, pageRequest)
                .map(returnedMapper::toDto);
    }

    public ReturnedDto createReturned(ReturnedDto returnedDto) {
        var returnedEntity = returnedMapper.toEntity(returnedDto);
        var savedReturned = returnedRepository.save(returnedEntity);
        return returnedMapper.toDto(savedReturned);
    }

    public ReturnedDto getReturnedById(Long id) {
        var returnedEntity = returnedRepository.findById(id).orElseThrow();
        return returnedMapper.toDto(returnedEntity);
    }

    public ReturnedDto updateReturned(Long id, ReturnedDto returnedDto) {
        var returnedEntity = returnedRepository.findById(id).orElseThrow();
        Returned partialUpdate = returnedMapper.partialUpdate(returnedDto, returnedEntity);
        var updatedReturned = returnedRepository.save(partialUpdate);
        return returnedMapper.toDto(updatedReturned);
    }

    public void deleteReturned(Long id) {
        returnedRepository.deleteById(id);
    }

    public String importReturnedsFromExcel(MultipartFile file) throws IOException {
        List<ReturnedDto> returnedDtos = ExcelDataImporter.importData(file, ReturnedDto.class);
        List<Returned> returneds = returnedDtos.stream().map(returnedMapper::toEntity).collect(Collectors.toList());
        returnedRepository.saveAll(returneds);
        return returneds.size() + " returneds have been imported successfully.";
    }

    public byte[] exportReturnedsToExcel() throws IOException {
        List<ReturnedDto> returnedDtos = returnedRepository.findAll().stream().map(returnedMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(returnedDtos, ReturnedDto.class);
    }
}
