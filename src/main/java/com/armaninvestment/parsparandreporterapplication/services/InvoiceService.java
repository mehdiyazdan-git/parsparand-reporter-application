package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceItemMapper;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.InvoiceSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.*;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceItemMapper invoiceItemMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final ContractRepository contractRepository;
    private final ProductRepository productRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final InvoiceStatusRepository invoiceStatusRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public Page<InvoiceDto> findAll(int page, int size, String sortBy, String sortDir, InvoiceSearch invoiceSearch) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Invoice> root = cq.from(Invoice.class);
        Join<Invoice, InvoiceItem> invoiceItemJoin = root.join("invoiceItems", JoinType.LEFT);

        // Aggregation
        Expression<Long> totalQuantity = cb.sum(cb.toLong(invoiceItemJoin.get("quantity")));
        Expression<Double> totalPrice = cb.sum(cb.prod(cb.toDouble(invoiceItemJoin.get("unitPrice")), cb.toDouble(invoiceItemJoin.get("quantity"))));

        // Select with coalesce to handle null values
        cq.multiselect(
                root.get("id").alias("id"),
                cb.coalesce(root.get("dueDate"), LocalDate.of(1970, 1, 1)).alias("dueDate"),
                cb.coalesce(root.get("invoiceNumber"), 0L).alias("invoiceNumber"),
                cb.coalesce(root.get("issuedDate"), LocalDate.of(1970, 1, 1)).alias("issuedDate"),
                cb.coalesce(root.get("salesType"), SalesType.CASH_SALES).alias("salesType"),
                cb.coalesce(root.get("contract").get("id"), 0L).alias("contractId"),
                cb.coalesce(root.get("contract").get("contractNumber"), "").alias("contractNumber"),
                cb.coalesce(root.get("customer").get("id"), 0L).alias("customerId"),
                cb.coalesce(root.get("customer").get("name"), "").alias("customerName"),
                cb.coalesce(root.get("invoiceStatus").get("id"), 0).alias("invoiceStatusId"),
                cb.coalesce(root.get("advancedPayment"), 0L).alias("advancedPayment"),
                cb.coalesce(root.get("insuranceDeposit"), 0L).alias("insuranceDeposit"),
                cb.coalesce(root.get("performanceBound"), 0L).alias("performanceBound"),
                cb.coalesce(root.get("year").get("id"), 0L).alias("yearId"),
                cb.coalesce(root.get("year").get("name"), 1402L).alias("yearName"),
                cb.coalesce(root.get("jalaliYear"), 0).alias("jalaliYear"),
                cb.coalesce(totalPrice, 0.0).alias("totalPrice"),
                cb.coalesce(totalQuantity, 0L).alias("totalQuantity")
        );

        // Specification
        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(invoiceSearch);
        Predicate specificationPredicate = specification.toPredicate(root, cq, cb);

        if (specificationPredicate != null) {
            cq.where(specificationPredicate);
        }

        cq.groupBy(
                root.get("id"),
                root.get("dueDate"),
                root.get("invoiceNumber"),
                root.get("issuedDate"),
                root.get("salesType"),
                root.get("contract").get("id"),
                root.get("contract").get("contractNumber"),
                root.get("customer").get("id"),
                root.get("customer").get("name"),
                root.get("invoiceStatus").get("id"),
                root.get("year").get("id"),
                root.get("year").get("name"),
                root.get("jalaliYear")
        );

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
                .getResultList();

        if (tuples.isEmpty()) {
            // Log the issue if the list is empty
            System.out.println("No results found for the given criteria.");
        }

        // Convert to DTO
        List<InvoiceDto> invoiceDtoList = tuples.stream().map(tuple -> new InvoiceDto(
                tuple.get("id", Long.class),
                tuple.get("dueDate", LocalDate.class),
                tuple.get("invoiceNumber", Long.class),
                tuple.get("issuedDate", LocalDate.class),
                tuple.get("salesType", SalesType.class),
                tuple.get("contractId", Long.class),
                tuple.get("contractNumber", String.class),
                tuple.get("customerId", Long.class),
                tuple.get("customerName", String.class),
                tuple.get("invoiceStatusId", Integer.class),
                tuple.get("advancedPayment", Long.class),
                tuple.get("insuranceDeposit", Long.class),
                tuple.get("performanceBound", Long.class),
                tuple.get("yearId", Long.class),
                tuple.get("totalQuantity", Long.class),
                tuple.get("totalPrice", Double.class),
                new LinkedHashSet<>() // Assuming you will fill this set later
        )).collect(Collectors.toList());

        // Fetch and set Invoice Items
        invoiceDtoList.forEach(invoiceDto -> {
            Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceDto.getId());
            optionalInvoice.ifPresent(invoice -> invoiceDto
                    .setInvoiceItems(invoice.getInvoiceItems().stream().map(invoiceItemMapper::toDto).collect(Collectors.toSet()))
            );
        });

        // Calculate total pages
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(Objects.requireNonNull(sortBy)) : Sort.Order.desc(Objects.requireNonNull(sortBy)))
        );

        return new PageImpl<>(invoiceDtoList, pageRequest, getCount(invoiceSearch));
    }

    private Long getCount(InvoiceSearch invoiceSearch) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Invoice> root = cq.from(Invoice.class);

        cq.select(cb.count(root));

        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(invoiceSearch);
        Predicate specificationPredicate = specification.toPredicate(root, cq, cb);

        if (specificationPredicate != null) {
            cq.where(specificationPredicate);
        }

        return entityManager.createQuery(cq).getSingleResult();
    }


    public List<InvoiceSelectDto> searchInvoiceByDescriptionKeywords(String description,Integer yearId) {
        try {
            List<Object[]> objects = invoiceRepository.searchInvoiceByDescriptionKeywords(description, yearId);

            List<InvoiceSelectDto> invoiceSelectDtos = new ArrayList<>();
            for (Object[] object : objects){
                InvoiceSelectDto invoiceSelectDto = new InvoiceSelectDto();
                invoiceSelectDto.setId((Long) object[0]);
                invoiceSelectDto.setName((String) object[1]);
                invoiceSelectDtos.add(invoiceSelectDto);
            }
            invoiceSelectDtos.forEach(invoiceSelectDto -> System.out.println(invoiceSelectDto.getName()));

            return invoiceSelectDtos;

        }catch (Exception e){
            e.printStackTrace();
           throw new RuntimeException(e);
        }
    }

    public InvoiceDto getInvoiceById(Long id) {
        var invoiceEntity = invoiceRepository.findById(id).orElseThrow();
        return invoiceMapper.toDto(invoiceEntity);
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        validateInvoiceUniqueness(invoiceDto);

        var invoiceEntity = invoiceMapper.toEntity(invoiceDto);
        invoiceEntity.setContract(contractRepository.findById(invoiceDto.getContractId()).orElseThrow());
        invoiceEntity.setCustomer(customerRepository.findById(invoiceDto.getCustomerId()).orElseThrow());
        invoiceEntity.setYear(yearRepository.findById(invoiceDto.getYearId()).orElseThrow());
        var savedInvoice = invoiceRepository.save(invoiceEntity);
        return invoiceMapper.toDto(savedInvoice);
    }

    @Transactional
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {

        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new IllegalStateException("صورت‌حساب پیدا نشد."));
        validateInvoiceUniquenessForUpdate(invoiceDto, id);

        Invoice partialUpdate = invoiceMapper.partialUpdate(invoiceDto, invoice);
        return invoiceMapper.toDto(invoiceRepository.save(partialUpdate));
    }

    private void validateInvoiceUniqueness(InvoiceDto invoiceDto) {
        if (invoiceRepository.existsByInvoiceNumberAndYearId(invoiceDto.getInvoiceNumber(), invoiceDto.getYearId())) {
            throw new IllegalStateException("یک صورت‌حساب با این شماره صورت‌حساب برای سال مالی مورد نظر قبلاً ثبت شده است.");
        }
        validateReceiptIdUniqueness(invoiceDto);
    }

    private void validateInvoiceUniquenessForUpdate(InvoiceDto invoiceDto, Long id) {
        if (invoiceRepository.existsByInvoiceNumberAndYearIdAndIdNot(invoiceDto.getInvoiceNumber(),invoiceDto.getYearId(), id)) {
            throw new IllegalStateException("یک صورت‌حساب دیگر با این شماره صورت‌حساب برای سال مالی مورد نظر وجود دارد.");
        }
        validateReceiptIdUniquenessOnUpdateEntity(invoiceDto);
    }

    private void validateReceiptIdUniqueness(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                if (invoiceItemRepository.existsByWarehouseReceiptId(invoiceItemDto.getWarehouseReceiptId())){
                    throw new IllegalStateException("برای این شماره حواله قبلا فاکتور صادر شده است.");
                }
            });
        }
    }
    private void validateReceiptIdUniquenessOnUpdateEntity(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                if (invoiceItemRepository.existsByWarehouseReceiptIdAndIdNot(invoiceItemDto.getWarehouseReceiptId(),invoiceItemDto.getId())){
                    throw new IllegalStateException("برای این شماره حواله قبلا فاکتور صادر شده است.");
                }
            });
        }
    }
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalStateException("صورت‌حساب پیدا نشد.");
        }
        invoiceRepository.deleteById(id);
    }
    @Transactional
    public String importInvoicesFromExcel(MultipartFile file) throws IOException {
        Map<Long, InvoiceDto> invoicesMap = new LinkedHashMap<>();

        // Fetch all necessary data once and store them in maps for quick access
        Map<String, Customer> customersMap = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getCustomerCode, customer -> customer, (existing, replacement) -> existing));
        Map<Long, Year> yearsMap = yearRepository.findAll().stream()
                .collect(Collectors.toMap(Year::getName, year -> year, (existing, replacement) -> existing));
        Map<String, Contract> contractsMap = contractRepository.findAll().stream()
                .collect(Collectors.toMap(Contract::getContractNumber, contract -> contract, (existing, replacement) -> existing));
        Map<String, Product> productsMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getProductCode, product -> product, (existing, replacement) -> existing));
        Map<Integer, InvoiceStatus> invoiceStatusesMap = invoiceStatusRepository.findAll().stream()
                .collect(Collectors.toMap(InvoiceStatus::getId, invoiceStatus -> invoiceStatus, (existing, replacement) -> existing));
        Map<String, WarehouseReceipt> warehouseReceiptsMap = warehouseReceiptRepository.findAll().stream()
                .collect(Collectors.toMap(
                        wr -> wr.getWarehouseReceiptNumber() + "-" + wr.getWarehouseReceiptDate(),
                        wr -> wr,
                        (existing, replacement) -> existing
                ));

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
                    Long invoiceNumber = getCellLongValue(currentRow, 0, rowNum);
                    LocalDate issuedDate = convertToDate(getCellStringValue(currentRow, 1, rowNum));
                    LocalDate dueDate = convertToDate(getCellStringValue(currentRow, 2, rowNum));
                    String salesType = getCellStringValue(currentRow, 3, rowNum);
                    String contractNumber = getCellStringValue(currentRow, 4, rowNum);
                    String customerCode = getCellStringValue(currentRow, 5, rowNum);
                    Long advancedPayment = getCellLongValue(currentRow, 6, rowNum);
                    Long insuranceDeposit = getCellLongValue(currentRow, 7, rowNum);
                    Long performanceBound = getCellLongValue(currentRow, 8, rowNum);
                    Long yearName = getCellLongValue(currentRow, 9, rowNum);
                    Integer quantity = getCellIntValue(currentRow, 10, rowNum);
                    Long unitPrice = getCellLongValue(currentRow, 11, rowNum);
                    String productCode = getCellStringValue(currentRow, 12, rowNum);
                    Long warehouseReceiptNumber = getCellLongValue(currentRow, 13, rowNum);
                    LocalDate warehouseReceiptDate = convertToDate(getCellStringValue(currentRow, 14, rowNum));
                    Integer invoiceStatusId = getCellIntValue(currentRow, 15, rowNum);

                    Year year = Optional.ofNullable(yearsMap.get(yearName))
                            .orElseThrow(() -> new IllegalStateException("سال با نام " + yearName + " یافت نشد."));
                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new IllegalStateException("مشتری با کد " + customerCode + " یافت نشد."));
                    Product product = Optional.ofNullable(productsMap.get(productCode))
                            .orElseThrow(() -> new IllegalStateException("محصول با کد " + productCode + " یافت نشد."));
                    InvoiceStatus invoiceStatus = Optional.ofNullable(invoiceStatusesMap.get(invoiceStatusId))
                            .orElseThrow(() -> new IllegalStateException("وضعیت فاکتور با شماره " + invoiceNumber + " یافت نشد."));
                    String receiptKey = warehouseReceiptNumber + "-" + warehouseReceiptDate;
                    WarehouseReceipt warehouseReceipt = Optional.ofNullable(warehouseReceiptsMap.get(receiptKey))
                            .orElseThrow(() -> new IllegalStateException("رسید انبار با شماره " + warehouseReceiptNumber + " و تاریخ " + warehouseReceiptDate + " یافت نشد."));

                    InvoiceDto invoiceDto = invoicesMap.computeIfAbsent(invoiceNumber, k -> {
                        InvoiceDto dto = new InvoiceDto();
                        dto.setInvoiceNumber(invoiceNumber);
                        dto.setIssuedDate(issuedDate);
                        dto.setDueDate(dueDate);
                        dto.setSalesType(SalesType.valueOf(salesType));
                        dto.setCustomerId(customer.getId());
                        if ("CONTRACTUAL_SALES".equals(salesType) && contractNumber != null && !contractNumber.isEmpty()) {
                            dto.setContractId(contractsMap.get(contractNumber).getId());
                            dto.setAdvancedPayment(advancedPayment);
                            dto.setInsuranceDeposit(insuranceDeposit);
                            dto.setPerformanceBound(performanceBound);
                        } else {
                            dto.setContractId(null);
                            dto.setAdvancedPayment(0L);
                            dto.setInsuranceDeposit(0L);
                            dto.setPerformanceBound(0L);
                        }
                        dto.setYearId(year.getId());
                        dto.setInvoiceStatusId(invoiceStatus.getId());
                        dto.setInvoiceItems(new LinkedHashSet<>());
                        return dto;
                    });

                    InvoiceItemDto itemDto = new InvoiceItemDto();
                    itemDto.setQuantity(quantity);
                    itemDto.setUnitPrice(unitPrice);
                    itemDto.setProductId(product.getId());
                    itemDto.setWarehouseReceiptId(warehouseReceipt.getId());
                    invoiceDto.getInvoiceItems().add(itemDto);

                } catch (Exception e) {
                    throw new RuntimeException("خطا در ردیف " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<Invoice> invoices = invoicesMap.values().stream()
                .map(invoiceDto -> {
                    Invoice entity = invoiceMapper.toEntity(invoiceDto);
                    if (invoiceDto.getCustomerId() != null) {
                        entity.setCustomer(entityManager.find(Customer.class, invoiceDto.getCustomerId()));
                    }
                    if ("CONTRACTUAL_SALES".equals(invoiceDto.getSalesType().name()) && invoiceDto.getContractId() != null) {
                        entity.setContract(entityManager.find(Contract.class, invoiceDto.getContractId()));
                    } else {
                        entity.setContract(null);
                    }
                    if (invoiceDto.getYearId() != null) {
                        entity.setYear(entityManager.find(Year.class, invoiceDto.getYearId()));
                    }
                    if (invoiceDto.getInvoiceStatusId() != null){
                        entity.setInvoiceStatus(entityManager.find(InvoiceStatus.class, invoiceDto.getInvoiceStatusId()));
                    }
                    entity.getInvoiceItems().forEach(item -> {
                        if (item.getProductId() != null) {
                            Product product = entityManager.find(Product.class, item.getProductId());
                            product.addInvoiceItem(item);
                            item.setProduct(product);
                        }
                        if (item.getWarehouseReceiptId() != null) {
                            WarehouseReceipt warehouseReceipt = entityManager.find(WarehouseReceipt.class, item.getWarehouseReceiptId());
                            warehouseReceipt.addInvoiceItem(item);
                            item.setWarehouseReceipt(warehouseReceipt);
                        }
                        item.setInvoice(entity); // Ensure the bidirectional relationship is set
                    });
                    return entity;
                })
                .collect(Collectors.toList());

        invoiceRepository.saveAll(invoices); // Batch save to minimize database connection
        return invoices.size() + " فاکتور با موفقیت وارد شدند.";
    }

    public byte[] exportInvoicesToExcel(InvoiceSearch search, boolean exportAll) {
        List<Invoice> invoices;

        if (exportAll) {
            // Fetch all filtered data
            invoices = getInvoicesBySearchCriteria(search);
        } else {
            // Fetch only the paginated result set
            Page<InvoiceDto> paginatedInvoices = findAll(search.getPage(), search.getSize(), search.getSortBy(), search.getSortDirection(), search);
            invoices = paginatedInvoices.getContent().stream()
                    .map(invoiceMapper::toEntity)
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoices");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Create data rows
            int rowNum = 1;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowNum++);
                populateInvoiceRow(invoice, row);
            }

            // Adjust column widths
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to a byte array output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export invoices to Excel", e);
        }
    }

    private void createHeaderCells(Row headerRow) {
        CellStyle headerCellStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        Font font = headerRow.getSheet().getWorkbook().createFont();
        font.setBold(true);
        headerCellStyle.setFont(font);

        String[] headers = {
                "Invoice Number", "Issued Date", "Due Date", "Sales Type", "Contract Number",
                "Customer Code", "Advanced Payment", "Insurance Deposit", "Performance Bound",
                "Year", "Status", "Total Quantity", "Total Price"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void populateInvoiceRow(Invoice invoice, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getIssuedDate() != null ? invoice.getIssuedDate().toString() : "");
        row.createCell(cellNum++).setCellValue(invoice.getDueDate() != null ? invoice.getDueDate().toString() : "");
        row.createCell(cellNum++).setCellValue(invoice.getSalesType() != null ? invoice.getSalesType().name() : "");
        row.createCell(cellNum++).setCellValue(invoice.getContract() != null ? invoice.getContract().getContractNumber() : "");
        row.createCell(cellNum++).setCellValue(invoice.getCustomer() != null ? invoice.getCustomer().getCustomerCode() : "");
        row.createCell(cellNum++).setCellValue(invoice.getAdvancedPayment() != null ? invoice.getAdvancedPayment() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getInsuranceDeposit() != null ? invoice.getInsuranceDeposit() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getPerformanceBound() != null ? invoice.getPerformanceBound() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getYear() != null ? invoice.getYear().getName() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus().getName() : "");

        // Calculate total quantity and total price
        long totalQuantity = invoice.getInvoiceItems().stream().mapToLong(InvoiceItem::getQuantity).sum();
        double totalPrice = invoice.getInvoiceItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();

        row.createCell(cellNum++).setCellValue(totalQuantity);
        row.createCell(cellNum++).setCellValue(totalPrice);
    }

    private List<Invoice> getInvoicesBySearchCriteria(InvoiceSearch search) {
        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(search);
        return invoiceRepository.findAll(specification);
    }



}
