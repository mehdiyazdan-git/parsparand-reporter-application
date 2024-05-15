package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.ContractDto;
import com.armaninvestment.parsparandreporterapplication.entities.Contract;
import com.armaninvestment.parsparandreporterapplication.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporterapplication.mappers.ContractMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.ContractRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.InvoiceRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ContractSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ContractSpecification;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final InvoiceRepository invoiceRepository;

    public Page<ContractDto> findContractByCriteria(ContractSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Contract> specification = ContractSpecification.bySearchCriteria(search);
        return contractRepository.findAll(specification, pageRequest)
                .map(contractMapper::toDto);
    }

    public ContractDto getContractById(Long id) {
        var contractEntity = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("قرارداد با شناسه " + id + " پیدا نشد."));
        return contractMapper.toDto(contractEntity);
    }

    public ContractDto createContract(ContractDto contractDto) {
        if (contractRepository.existsByContractNumber(contractDto.getContractNumber())) {
            throw new IllegalStateException("یک قرارداد با همین شماره قرارداد قبلاً ثبت شده است.");
        }
        var contractEntity = contractMapper.toEntity(contractDto);
        var savedContract = contractRepository.save(contractEntity);
        return contractMapper.toDto(savedContract);
    }

    public ContractDto updateContract(Long id, ContractDto contractDto) {
        var existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("قرارداد با شناسه " + id + " پیدا نشد."));

        if (contractRepository.existsByContractNumberAndIdNot(contractDto.getContractNumber(), id)) {
            throw new IllegalStateException("یک قرارداد دیگر با همین شماره قرارداد وجود دارد.");
        }

        contractMapper.partialUpdate(contractDto, existingContract);
        var updatedContract = contractRepository.save(existingContract);
        return contractMapper.toDto(updatedContract);
    }

    public void deleteContract(Long id) {
        if (!contractRepository.existsById(id)) {
            throw new EntityNotFoundException("قرارداد با شناسه " + id + " پیدا نشد.");
        }
        if (invoiceRepository.existsByCustomerId(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف قرارداد وجود ندارد چون فاکتورهای مرتبط دارد.");
        }
        contractRepository.deleteById(id);
    }

    public String importContractsFromExcel(MultipartFile file) throws IOException {
        List<ContractDto> contractDtos = ExcelDataImporter.importData(file, ContractDto.class);
        List<Contract> contracts = contractDtos.stream()
                .map(contractMapper::toEntity)
                .collect(Collectors.toList());
        contractRepository.saveAll(contracts);
        return contracts.size() + " قرارداد با موفقیت وارد شد.";
    }

    public byte[] exportContractsToExcel() throws IOException {
        List<ContractDto> contractDtos = contractRepository.findAll().stream()
                .map(contractMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(contractDtos, ContractDto.class);
    }
}
