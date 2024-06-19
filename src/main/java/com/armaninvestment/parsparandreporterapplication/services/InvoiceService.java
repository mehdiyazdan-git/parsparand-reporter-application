package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.armaninvestment.parsparandreporterapplication.exceptions.RowColumnException;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceItemMapper;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.InvoiceSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils;
import jakarta.persistence.EntityManager;
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
    private final WarehouseInvoiceRepository warehouseInvoiceRepository;

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
            case "totalQuantity" ->
                    cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalQuantity) : cb.desc(totalQuantity));
            case "customerName" ->
                    cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get("customer").get("name")) : cb.desc(root.get("customer").get("name")));
            default ->
                    cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy)));
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


    public List<InvoiceSelectDto> searchInvoiceByDescriptionKeywords(String description, Integer yearId) {
        try {
            List<Object[]> objects = invoiceRepository.searchInvoiceByDescriptionKeywords(description, yearId);

            List<InvoiceSelectDto> invoiceSelectDtos = new ArrayList<>();
            for (Object[] object : objects) {
                InvoiceSelectDto invoiceSelectDto = new InvoiceSelectDto();
                invoiceSelectDto.setId((Long) object[0]);
                invoiceSelectDto.setName((String) object[1]);
                invoiceSelectDtos.add(invoiceSelectDto);
            }
            invoiceSelectDtos.forEach(invoiceSelectDto -> System.out.println(invoiceSelectDto.getName()));

            return invoiceSelectDtos;

        } catch (Exception e) {
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
        WarehouseInvoice warehouseInvoice = new WarehouseInvoice();
        warehouseInvoice.setInvoice(savedInvoice);
        warehouseInvoiceRepository.save(warehouseInvoice);
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
        if (invoiceRepository.existsByInvoiceNumberAndYearIdAndIdNot(invoiceDto.getInvoiceNumber(), invoiceDto.getYearId(), id)) {
            throw new IllegalStateException("یک صورت‌حساب دیگر با این شماره صورت‌حساب برای سال مالی مورد نظر وجود دارد.");
        }
        validateReceiptIdUniquenessOnUpdateEntity(invoiceDto);
    }

    private void validateReceiptIdUniqueness(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                if (invoiceItemRepository.existsByWarehouseReceiptId(invoiceItemDto.getWarehouseReceiptId())) {
                    throw new IllegalStateException("برای این شماره حواله قبلا فاکتور صادر شده است.");
                }
            });
        }
    }

    private void validateReceiptIdUniquenessOnUpdateEntity(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                if (invoiceItemRepository.existsByWarehouseReceiptIdAndIdNot(invoiceItemDto.getWarehouseReceiptId(), invoiceItemDto.getId())) {
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
        Optional<WarehouseInvoice> optionalWarehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByInvoiceId(id);
        if (optionalWarehouseInvoice.isPresent()) {
            WarehouseInvoice warehouseInvoice = optionalWarehouseInvoice.get();
            warehouseInvoice.setInvoice(null);
        }
    }

    @Transactional
    public String importInvoicesFromExcel(MultipartFile file) throws IOException {
        Map<String, InvoiceDto> invoicesMap = new LinkedHashMap<>();

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

        WarehouseReceipt warehouseReceipt = null;
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
                    String index = ExcelUtils.getCellStringValue(currentRow, 0, rowNum);
                    Long invoiceNumber = ExcelUtils.getCellLongValue(currentRow, 1, rowNum);
                    LocalDate issuedDate = DateConvertor.convertJalaliToGregorian(Objects.requireNonNull(getCellStringValue(currentRow, 2, rowNum)));
                    LocalDate dueDate = DateConvertor.convertJalaliToGregorian(Objects.requireNonNull(getCellStringValue(currentRow, 3, rowNum)));
                    String salesType = ExcelUtils.getCellStringValue(currentRow, 4, rowNum);
                    String contractNumber = ExcelUtils.getCellStringValue(currentRow, 5, rowNum);
                    String customerCode = ExcelUtils.getCellStringValue(currentRow, 6, rowNum);
                    Long advancedPayment = ExcelUtils.getCellLongValue(currentRow, 7, rowNum);
                    Long insuranceDeposit = ExcelUtils.getCellLongValue(currentRow, 8, rowNum);
                    Long performanceBound = ExcelUtils.getCellLongValue(currentRow, 9, rowNum);
                    Long yearName = ExcelUtils.getCellLongValue(currentRow, 10, rowNum);
                    Integer quantity = ExcelUtils.getCellIntValue(currentRow, 11, rowNum);
                    Long unitPrice = ExcelUtils.getCellLongValue(currentRow, 12, rowNum);
                    String productCode = ExcelUtils.getCellStringValue(currentRow, 13, rowNum);
                    Long warehouseReceiptNumber = ExcelUtils.getCellLongValue(currentRow, 14, rowNum);
                    LocalDate warehouseReceiptDate = ExcelUtils.convertToDate(ExcelUtils.getCellStringValue(currentRow, 15, rowNum));
                    Integer invoiceStatusId = ExcelUtils.getCellIntValue(currentRow, 16, rowNum);

                    int finalRowNum = rowNum;
                    Year year = Optional.ofNullable(yearsMap.get(yearName))
                            .orElseThrow(() -> new RowColumnException(finalRowNum, 10, "سال با نام " + yearName + " یافت نشد.", null));
                    int finalRowNum1 = rowNum;
                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new RowColumnException(finalRowNum1, 6, "مشتری با کد " + customerCode + " یافت نشد.", null));
                    int finalRowNum2 = rowNum;
                    Product product = Optional.ofNullable(productsMap.get(productCode))
                            .orElseThrow(() -> new RowColumnException(finalRowNum2, 13, "محصول با کد " + productCode + " یافت نشد.", null));
                    int finalRowNum3 = rowNum;
                    InvoiceStatus invoiceStatus = Optional.ofNullable(invoiceStatusesMap.get(invoiceStatusId))
                            .orElseThrow(() -> new RowColumnException(finalRowNum3, 16, "وضعیت فاکتور با شماره " + invoiceNumber + " یافت نشد.", null));
                    String receiptKey = warehouseReceiptNumber + "-" + warehouseReceiptDate;
                    int finalRowNum4 = rowNum;
                    warehouseReceipt = Optional.ofNullable(warehouseReceiptsMap.get(receiptKey))
                            .orElseThrow(() -> new RowColumnException(finalRowNum4, 14, "رسید انبار با شماره " + warehouseReceiptNumber + " و تاریخ " + warehouseReceiptDate + " یافت نشد.", null));

                    // ترکیب شماره فاکتور + تاریخ فاکتور به عنوان کلید Map
                    assert issuedDate != null;
                    InvoiceDto invoiceDto = invoicesMap.computeIfAbsent(index, k -> {
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

                } catch (RowColumnException e) {
                    throw new RuntimeException(e.getMessage());
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
                    if (invoiceDto.getInvoiceStatusId() != null) {
                        entity.setInvoiceStatus(entityManager.find(InvoiceStatus.class, invoiceDto.getInvoiceStatusId()));
                    }
                    entity.getInvoiceItems().forEach(item -> {
                        if (item.getProductId() != null) {
                            Product product = entityManager.find(Product.class, item.getProductId());
                            product.addInvoiceItem(item);
                            item.setProduct(product);
                        }
                        if (item.getWarehouseReceiptId() != null) {
                            WarehouseReceipt receipt = entityManager.find(WarehouseReceipt.class, item.getWarehouseReceiptId());
                            receipt.addInvoiceItem(item);
                            item.setWarehouseReceipt(receipt);
                        }
                        item.setInvoice(entity);
                    });
                    return entity;
                })
                .collect(Collectors.toList());

        List<Invoice> invoiceList = invoiceRepository.saveAll(invoices);


        List<WarehouseInvoice> warehouseInvoiceList = invoiceList.stream()
                .flatMap(invoice -> invoice.getInvoiceItems().stream())
                .map(invoiceItem -> new AbstractMap.SimpleEntry<>(invoiceItem.getWarehouseReceipt(), invoiceItem))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))
                .entrySet().stream().map(entry -> {
                    WarehouseInvoice warehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByReceiptId(entry.getKey().getId());
                    warehouseInvoice.setInvoice(entry.getValue().getInvoice());
                    warehouseInvoice.setWarehouseReceipt(entry.getKey());
                    return warehouseInvoice;
                })
                .map(warehouseInvoiceRepository::save)
                .toList();

        return invoiceList.size() + " invoices created";
    }

    public byte[] exportInvoicesToExcel(InvoiceSearch search, boolean exportAll) {
        List<Invoice> invoices;

        if (exportAll) {
            // Fetch all filtered data
            invoices = getInvoicesBySearchCriteria(search);
        } else {
            // Fetch only the paginated result set
            Page<InvoiceDto> paginatedInvoices = findAll(search.getPage(), search.getSize(), search.getSortBy(), search.getOrder(), search);
            invoices = paginatedInvoices.getContent().stream()
                    .map(invoiceMapper::toEntity)
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoices");
            sheet.setRightToLeft(true);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Initialize totals
            long totalQuantity = 0;
            double totalPrice = 0;
            double totalAdvancedPayment = 0;
            double totalInsuranceDeposit = 0;
            double totalPerformanceBound = 0;

            // Create data rows
            int rowNum = 1;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowNum++);
                populateInvoiceRow(invoice, row);

                // Sum totals
                totalQuantity += invoice.getInvoiceItems().stream().mapToLong(InvoiceItem::getQuantity).sum();
                totalPrice += invoice.getInvoiceItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();
                totalAdvancedPayment += invoice.getAdvancedPayment() != null ? invoice.getAdvancedPayment() : 0;
                totalInsuranceDeposit += invoice.getInsuranceDeposit() != null ? invoice.getInsuranceDeposit() : 0;
                totalPerformanceBound += invoice.getPerformanceBound() != null ? invoice.getPerformanceBound() : 0;
            }

            // Create subtotal row
            Row subtotalRow = sheet.createRow(rowNum);
            createSubtotalRow(subtotalRow, totalQuantity, totalPrice, totalAdvancedPayment, totalInsuranceDeposit, totalPerformanceBound);

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

    private void createSubtotalRow(Row subtotalRow, long totalQuantity, double totalPrice, double totalAdvancedPayment, double totalInsuranceDeposit, double totalPerformanceBound) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle footerCellStyle = cellStyleHelper.getFooterCellStyle(subtotalRow.getSheet().getWorkbook());

        int cellNum = 0;
        subtotalRow.createCell(cellNum++).setCellValue("جمع کل"); // Subtotal label
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue(totalAdvancedPayment);
        subtotalRow.createCell(cellNum++).setCellValue(totalInsuranceDeposit);
        subtotalRow.createCell(cellNum++).setCellValue(totalPerformanceBound);
        subtotalRow.createCell(cellNum++).setCellValue(totalQuantity);
        subtotalRow.createCell(cellNum++).setCellValue(totalPrice);

        // Merge cells [0, 5]
        subtotalRow.getSheet().addMergedRegion(new CellRangeAddress(subtotalRow.getRowNum(), subtotalRow.getRowNum(), 0, 5));

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
    }

// Other existing methods...


    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "شماره فاکتور", "تاریخ صدور", "تاریخ پیگیری", "نوع فروش", "شماره قرارداد",
                "کد مشتری", "پیش پرداخت", "سپرده بیمه", "حسن انجام کار",
                "تعداد", "مبلغ"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private String convertToPersianCaption(String salesType) {
        return switch (salesType) {
            case "CASH_SALES" -> "فروش نقدی";
            case "CONTRACTUAL_SALES" -> "فروش قراردادی";
            default -> "نامشخص";
        };
    }

    private void populateInvoiceRow(Invoice invoice, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getIssuedDate() != null ? DateConvertor.convertGregorianToJalali(invoice.getIssuedDate()) : "");
        row.createCell(cellNum++).setCellValue(invoice.getDueDate() != null ? DateConvertor.convertGregorianToJalali(invoice.getDueDate()) : "");
        row.createCell(cellNum++).setCellValue(invoice.getSalesType() != null ? convertToPersianCaption(invoice.getSalesType().name()) : "");
        row.createCell(cellNum++).setCellValue(invoice.getContract() != null ? invoice.getContract().getContractNumber() : "");
        row.createCell(cellNum++).setCellValue(invoice.getCustomer() != null ? invoice.getCustomer().getCustomerCode() : "");
        row.createCell(cellNum++).setCellValue(invoice.getAdvancedPayment() != null ? invoice.getAdvancedPayment() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getInsuranceDeposit() != null ? invoice.getInsuranceDeposit() : 0);
        row.createCell(cellNum++).setCellValue(invoice.getPerformanceBound() != null ? invoice.getPerformanceBound() : 0);

        // Calculate total quantity and total price
        long totalQuantity = invoice.getInvoiceItems().stream().mapToLong(InvoiceItem::getQuantity).sum();
        double totalPrice = invoice.getInvoiceItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();

        row.createCell(cellNum++).setCellValue(totalQuantity);
        row.createCell(cellNum++).setCellValue(totalPrice);

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());


        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                cell.setCellStyle(defaultCellStyle);
            }
        }
        row.getCell(7).setCellStyle(monatoryCellStyle);
        row.getCell(8).setCellStyle(monatoryCellStyle);
        row.getCell(9).setCellStyle(monatoryCellStyle);
        row.getCell(10).setCellStyle(monatoryCellStyle);
    }

    private List<Invoice> getInvoicesBySearchCriteria(InvoiceSearch search) {
        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(search);
        return invoiceRepository.findAll(specification);
    }


}
