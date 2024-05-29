package com.armaninvestment.parsparandreporterapplication.services;


import com.armaninvestment.parsparandreporterapplication.dtos.ContractDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ContractSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.Contract;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Product;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporterapplication.mappers.ContractMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.ContractSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ContractSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.*;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final ProductRepository productRepository;

    public Page<ContractDto> findContractByCriteria(ContractSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Contract> specification = ContractSpecification.bySearchCriteria(search);
        return contractRepository.findAll(specification, pageRequest)
                .map(contractMapper::toDto);
    }
    public List<ContractSelectDto> findAllContractSelect(String searchParam) {
        Specification<Contract> specification = ContractSpecification.getSelectSpecification(searchParam);
        return contractRepository
                .findAll(specification)
                .stream()
                .map(contractMapper::toSelectDto)
                .collect(Collectors.toList());
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

    public byte[] exportContractsToExcel() throws IOException {
        List<ContractDto> contractDtos = contractRepository.findAll().stream()
                .map(contractMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(contractDtos, ContractDto.class);
    }

    public String importContractsFromExcel(MultipartFile file) throws IOException {
        Map<String, ContractDto> contractsMap = new HashMap<>();

        // Fetch all necessary data once
        Map<String, Customer> customersMap = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getCustomerCode, customer -> customer, (existing, replacement) -> existing));
        Map<Long, Year> yearsMap = yearRepository.findAll().stream()
                .collect(Collectors.toMap(Year::getName, year -> year, (existing, replacement) -> existing));
        Map<String, Product> productsMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getProductCode, product -> product, (existing, replacement) -> existing));

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();

            // Skip the header row
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNum = 1;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNum++;
                try {
                    String contractNumber = getCellStringValue(currentRow, 0, rowNum);
                    String contractDescription = getCellStringValue(currentRow, 1, rowNum);
                    LocalDate startDate = convertToDate(getCellStringValue(currentRow, 2, rowNum));
                    LocalDate endDate = convertToDate(getCellStringValue(currentRow, 3, rowNum));
                    Long yearName = getCellLongValue(currentRow, 4, rowNum);
                    String customerCode = getCellStringValue(currentRow, 5, rowNum);
                    Double advancePayment = getCellDoubleValue(currentRow, 6, rowNum);
                    Double insuranceDeposit = getCellDoubleValue(currentRow, 7, rowNum);
                    Double performanceBond = getCellDoubleValue(currentRow, 8, rowNum);
                    Long quantity = getCellLongValue(currentRow, 9, rowNum);
                    Long unitPrice = getCellLongValue(currentRow, 10, rowNum);
                    String productCode = getCellStringValue(currentRow, 11, rowNum);

                    Year year = Optional.ofNullable(yearsMap.get(yearName))
                            .orElseThrow(() -> new IllegalStateException("سال با نام " + yearName + " یافت نشد."));
                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new IllegalStateException("مشتری با کد " + customerCode + " یافت نشد."));
                    Product product = Optional.ofNullable(productsMap.get(productCode))
                            .orElseThrow(() -> new IllegalStateException("محصول با کد " + productCode + " یافت نشد."));

                    if (contractRepository.existsByContractNumber(contractNumber)) {
                        throw new IllegalStateException("قرارداد با شماره " + contractNumber + " قبلاً ثبت شده است.");
                    }

                    ContractDto contractDto = contractsMap.computeIfAbsent(contractNumber, k -> {
                        ContractDto dto = new ContractDto();
                        dto.setContractNumber(contractNumber);
                        dto.setContractDescription(contractDescription);
                        dto.setStartDate(startDate);
                        dto.setEndDate(endDate);
                        dto.setYearId(year.getId());
                        dto.setCustomerId(customer.getId());
                        dto.setAdvancePayment(advancePayment);
                        dto.setInsuranceDeposit(insuranceDeposit);
                        dto.setPerformanceBond(performanceBond);
                        dto.setContractItems(new LinkedHashSet<>());
                        return dto;
                    });

                    ContractItemDto itemDto = new ContractItemDto();
                    itemDto.setQuantity(quantity);
                    itemDto.setUnitPrice(unitPrice);
                    itemDto.setProductId(product.getId());
                    contractDto.getContractItems().add(itemDto);

                } catch (Exception e) {
                    throw new RuntimeException("خطا در ردیف " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<Contract> contracts = contractsMap.values().stream()
                .map(contractMapper::toEntity)
                .collect(Collectors.toList());

        contractRepository.saveAll(contracts);
        return contracts.size() + " قرارداد با موفقیت وارد شدند.";
    }

}
