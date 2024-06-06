package com.armaninvestment.parsparandreporterapplication.services;


import com.armaninvestment.parsparandreporterapplication.dtos.ContractDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ContractSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporterapplication.mappers.ContractItemMapper;
import com.armaninvestment.parsparandreporterapplication.mappers.ContractMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.ContractSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ContractSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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
    private final ContractItemMapper contractItemMapper;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<ContractDto> findAll(int page, int size, String sortBy, String sortDir, ContractSearch contractSearch) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Contract> root = cq.from(Contract.class);
        Join<Contract, ContractItem> contractItemJoin = root.join("contractItems", JoinType.LEFT);

        // Aggregation
        Expression<Long> totalQuantity = cb.sum(cb.toLong(contractItemJoin.get("quantity")));
        Expression<Double> totalPrice = cb.sum(cb.prod(cb.toDouble(contractItemJoin.get("unitPrice")), cb.toDouble(contractItemJoin.get("quantity"))));

        // Select
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("contractDescription").alias("contractDescription"),
                root.get("contractNumber").alias("contractNumber"),
                root.get("endDate").alias("endDate"),
                root.get("startDate").alias("startDate"),
                root.get("customer").get("id").alias("customerId"),
                root.get("customer").get("name").alias("customerName"),
                root.get("year").get("id").alias("yearId"),
                root.get("advancePayment").alias("advancePayment"),
                root.get("insuranceDeposit").alias("insuranceDeposit"),
                root.get("performanceBond").alias("performanceBond"),
                root.get("year").get("name").alias("yearName"),
                totalPrice.alias("totalPrice"),
                totalQuantity.alias("totalQuantity")
        );

        // Specification
        Specification<Contract> specification = ContractSpecification.bySearchCriteria(contractSearch);
        cq.groupBy(root.get("id"), root.get("contractDescription"), root.get("contractNumber"), root.get("endDate"),
                root.get("startDate"), root.get("customer").get("id"), root.get("customer").get("name"),
                root.get("year").get("id"), root.get("advancePayment"), root.get("insuranceDeposit"),
                root.get("performanceBond"), root.get("year").get("name"));
        cq.where(specification.toPredicate(root, cq, cb));

        // Sorting
        switch (Objects.requireNonNull(sortBy)) {
            case "totalPrice" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalPrice) : cb.desc(totalPrice));
            case "totalQuantity" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalQuantity) : cb.desc(totalQuantity));
            case "customerName" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get("customer").get("name")) : cb.desc(root.get("customer").get("name")));
            default -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy)));
        }

        // Pagination
        List<Tuple> tuples = entityManager.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList().stream().toList();

        // Convert to DTO
        List<ContractDto> contractDtoList = tuples.stream().map(tuple -> new ContractDto(
                tuple.get("id", Long.class),
                tuple.get("contractDescription", String.class),
                tuple.get("contractNumber", String.class),
                tuple.get("endDate", LocalDate.class),
                tuple.get("startDate", LocalDate.class),
                tuple.get("customerId", Long.class),
                tuple.get("customerName", String.class),
                tuple.get("yearId", Long.class),
                tuple.get("advancePayment", Double.class),
                tuple.get("insuranceDeposit", Double.class),
                tuple.get("performanceBond", Double.class),
                tuple.get("totalQuantity", Long.class),
                tuple.get("totalPrice", Double.class),
                new LinkedHashSet<>() // Assuming you will fill this set later
        )).collect(Collectors.toList());

        // Fetch and set Contract Items
        contractDtoList.forEach(contractDto -> {
            Optional<Contract> optionalContract = contractRepository.findById(contractDto.getId());
            optionalContract.ifPresent(contract -> contractDto
                    .setContractItems(contract.getContractItems().stream().map(contractItemMapper::toDto).collect(Collectors.toSet()))
            );
        });

        // Calculate total pages
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(Objects.requireNonNull(sortBy)) : Sort.Order.desc(Objects.requireNonNull(sortBy)))
        );

        return new PageImpl<>(contractDtoList, pageRequest, getCount(contractSearch));
    }

    private Long getCount(ContractSearch contractSearch){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Contract> root = cq.from(Contract.class);

        cq.select(cb.count(root));

        Specification<Contract> specification = ContractSpecification.bySearchCriteria(contractSearch);
        cq.where(specification.toPredicate(root, cq, cb));

        return entityManager.createQuery(cq).getSingleResult();
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



    public byte[] exportContractsToExcel(ContractSearch search, boolean exportAll) throws IllegalAccessException {
        List<ContractDto> contracts;

        if (exportAll) {
            contracts = findAll(search);
        } else {
            Page<ContractDto> paginatedContracts = findPage(search.getPage(), search.getSize(), search.getSortBy(), search.getOrder(), search);
            contracts = paginatedContracts.getContent();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contracts");
            // set direction to rtl
            sheet.setRightToLeft(true);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Initialize totals
            long totalQuantity = 0;
            double totalPrice = 0;
            double totalAdvancePayment = 0;
            double totalInsuranceDeposit = 0;
            double totalPerformanceBond = 0;

            // Create data rows
            int rowNum = 1;
            for (ContractDto contract : contracts) {
                Row row = sheet.createRow(rowNum++);
                populateContractRow(contract, row);

                // Sum totals
                totalQuantity += contract.getContractItems().stream().mapToLong(ContractItemDto::getQuantity).sum();
                totalPrice += contract.getContractItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();
                totalAdvancePayment += contract.getAdvancePayment() != null ? contract.getAdvancePayment() : 0;
                totalInsuranceDeposit += contract.getInsuranceDeposit() != null ? contract.getInsuranceDeposit() : 0;
                totalPerformanceBond += contract.getPerformanceBond() != null ? contract.getPerformanceBond() : 0;
            }

            // Create subtotal row
            Row subtotalRow = sheet.createRow(rowNum);
            createSubtotalRow(subtotalRow, totalQuantity, totalPrice, totalAdvancePayment, totalInsuranceDeposit, totalPerformanceBond);

            // Adjust column widths
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to a byte array output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export contracts to Excel", e);
        }
    }

    private void createSubtotalRow(Row subtotalRow, long totalQuantity, double totalPrice, double totalAdvancePayment, double totalInsuranceDeposit, double totalPerformanceBond) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle footerCellStyle = cellStyleHelper.getFooterCellStyle(subtotalRow.getSheet().getWorkbook());

        int cellNum = 0;
        subtotalRow.createCell(cellNum++).setCellValue("جمع کل"); // Subtotal label
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue(totalAdvancePayment);
        subtotalRow.createCell(cellNum++).setCellValue(totalInsuranceDeposit);
        subtotalRow.createCell(cellNum++).setCellValue(totalPerformanceBond);
        subtotalRow.createCell(cellNum++).setCellValue(totalQuantity);
        subtotalRow.createCell(cellNum++).setCellValue(totalPrice);

        // Merge cells [0, 6]
        subtotalRow.getSheet().addMergedRegion(new CellRangeAddress(subtotalRow.getRowNum(), subtotalRow.getRowNum(), 0, 6));

        for (int i = 0; i < subtotalRow.getLastCellNum(); i++) {
            Cell cell = subtotalRow.getCell(i);
            if (cell != null) {
                cell.setCellStyle(footerCellStyle);
            }
        }
        subtotalRow.getCell(7).setCellStyle(footerCellStyle);
        subtotalRow.getCell(8).setCellStyle(footerCellStyle);
        subtotalRow.getCell(9).setCellStyle(footerCellStyle);
        subtotalRow.getCell(10).setCellStyle(footerCellStyle);
        subtotalRow.getCell(11).setCellStyle(footerCellStyle);
    }

    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "شناسه قرارداد", "عنوان قرارداد", "شماره قرارداد", "تاریخ شروع", "تاریخ پایان",
                "شناسه مشتری", "نام مشتری", "ضریب پیش پرداخت", "ضریب سپرده بیمه",
                "ضریب حسن انجام کار", "تعداد", "مبلغ"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void populateContractRow(ContractDto contract, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(contract.getId() != null ? contract.getId() : 0);
        row.createCell(cellNum++).setCellValue(contract.getContractDescription() != null ? contract.getContractDescription() : "");
        row.createCell(cellNum++).setCellValue(contract.getContractNumber() != null ? contract.getContractNumber() : "");
        row.createCell(cellNum++).setCellValue(contract.getStartDate() != null ? DateConvertor.convertGregorianToJalali(contract.getStartDate()) : "");
        row.createCell(cellNum++).setCellValue(contract.getEndDate() != null ? DateConvertor.convertGregorianToJalali(contract.getEndDate()) : "");
        row.createCell(cellNum++).setCellValue(contract.getCustomerId() != null ? contract.getCustomerId() : 0);
        row.createCell(cellNum++).setCellValue(contract.getCustomerName() != null ? contract.getCustomerName() : "");
        row.createCell(cellNum++).setCellValue(contract.getAdvancePayment() != null ? contract.getAdvancePayment() : 0.0);
        row.createCell(cellNum++).setCellValue(contract.getInsuranceDeposit() != null ? contract.getInsuranceDeposit() : 0.0);
        row.createCell(cellNum++).setCellValue(contract.getPerformanceBond() != null ? contract.getPerformanceBond() : 0.0);

        long totalQuantity = contract.getContractItems().stream().mapToLong(ContractItemDto::getQuantity).sum();
        double totalPrice = contract.getContractItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();

        row.createCell(cellNum++).setCellValue(totalQuantity);
        row.createCell(cellNum++).setCellValue(totalPrice);

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());

        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            cell.setCellStyle(defaultCellStyle);
        }
        row.getCell(10).setCellStyle(monatoryCellStyle);
        row.getCell(11).setCellStyle(monatoryCellStyle);
    }

    private List<ContractDto> findAll(ContractSearch search) {
        Specification<Contract> contractSpecification = ContractSpecification.bySearchCriteria(search);
        List<Contract> contracts = contractRepository.findAll(contractSpecification);
        return contracts.stream().map(contractMapper::toDto).collect(Collectors.toList());
    }

    private Page<ContractDto> findPage(int page, int size, String sortBy, String order, ContractSearch search) {
        var direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, direction, sortBy);
        Specification<Contract> spec = ContractSpecification.bySearchCriteria(search);
        Page<Contract> paginatedContracts = contractRepository.findAll(spec, pageable);
        return paginatedContracts.map(contractMapper::toDto);
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
