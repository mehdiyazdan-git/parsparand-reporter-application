package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.*;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.armaninvestment.parsparandreporterapplication.exceptions.RowColumnException;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceItemMapper;
import com.armaninvestment.parsparandreporterapplication.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.InvoiceSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.*;
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
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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

        // Create a CriteriaBuilder object to build the query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        // Create a CriteriaQuery object to query the database
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        // Specify the root of the query (the Invoice entity)
        Root<Invoice> root = cq.from(Invoice.class);
        // Join the Invoice entity with the InvoiceItem entity
        Join<Invoice, InvoiceItem> invoiceItemJoin = root.join("invoiceItems", JoinType.LEFT);

        Join<Invoice,Contract> contractJoin = root.join("contract", JoinType.LEFT);
        Join<Invoice,Customer> customerJoin = root.join("customer", JoinType.LEFT);

        // define contractNumber using contractJoin
        Expression<String> contractNumber = contractJoin.get("contractNumber");
        // define contract id using contractJoin
        Expression<Long> contractId = contractJoin.get("id");

        // define customerName using customerJoin
        Expression<String> customerName = customerJoin.get("name");
        // define customer id using customerJoin
        Expression<Long> customerId = customerJoin.get("id");

        // define Aggregation columns by expressions like totalQuantity and totalPrice
        Expression<Long> totalQuantity = cb.sum(cb.toLong(invoiceItemJoin.get("quantity")));
        Expression<Double> totalPrice = cb.sum(cb.prod(cb.toDouble(invoiceItemJoin.get("unitPrice")), cb.toDouble(invoiceItemJoin.get("quantity"))));

        // define multiselect columns
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("dueDate").alias("dueDate"),
                root.get("invoiceNumber").alias("invoiceNumber"),
                root.get("issuedDate").alias("issuedDate"),
                root.get("salesType").alias("salesType"),
                // columns from joined tables
                contractId.alias("contractId"),
                contractNumber.alias("contractNumber"),
                customerId.alias("customerId"),
                customerName.alias("customerName"),
                root.get("invoiceStatus").get("id").alias("invoiceStatusId"),
                root.get("advancedPayment").alias("advancedPayment"),
                root.get("insuranceDeposit").alias("insuranceDeposit"),
                root.get("performanceBound").alias("performanceBound"),
                root.get("year").get("id").alias("yearId"),
                root.get("year").get("name").alias("yearName"),
                root.get("jalaliYear").alias("jalaliYear"),
                // aggregation columns
                totalPrice.alias("totalPrice"),
                totalQuantity.alias("totalQuantity")
        );

        // prepare specifications
        Specification<Invoice> specification = InvoiceSpecification.bySearchCriteria(invoiceSearch);
        Predicate specificationPredicate = specification.toPredicate(root, cq, cb);

        if (specificationPredicate != null) {
            cq.where(specificationPredicate);
        }
        // Group by
        cq.groupBy(
                root.get("id"),
                root.get("dueDate"),
                root.get("invoiceNumber"),
                root.get("issuedDate"),
                root.get("salesType"),
                contractId,
                contractNumber,
                customerId,
                customerName,
                root.get("invoiceStatus").get("id"),
                root.get("year").get("id"),
                root.get("year").get("name"),
                root.get("jalaliYear")
        );
        // This code dynamically sorts a JPA query, handling cases where the sort column (`searchForm.sortBy`) might not be
        // a direct attribute of the main entity. It uses a `switch` statement to apply specific sorting logic for
        // calculated values or attributes of related entities, ensuring flexibility and type safety.
        switch (Objects.requireNonNull(sortBy)) {
            case "totalPrice" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalPrice) : cb.desc(totalPrice));
            case "totalQuantity" ->
                    cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalQuantity) : cb.desc(totalQuantity));
            case "customerName" ->
                cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(customerName) : cb.desc(customerName));
            case "contractNumber" ->
                cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(contractNumber) : cb.desc(contractNumber));
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
//        List<InvoiceDto> invoiceDtoList = convertToDtoList(tuples);

        // Convert tuple to DTO list explicitly
        List<InvoiceDto> invoiceDtoList = tuples.stream().map(tuple -> {
            InvoiceDto invoiceDto = new InvoiceDto();
            invoiceDto.setId((Long) tuple.get("id"));
            invoiceDto.setDueDate((LocalDate) tuple.get("dueDate"));
            invoiceDto.setInvoiceNumber((Long) tuple.get("invoiceNumber"));
            invoiceDto.setIssuedDate((LocalDate) tuple.get("issuedDate"));

            // Handle SalesType conversion if necessary
            Object salesTypeValue = tuple.get("salesType");
            if (salesTypeValue instanceof SalesType) {
                invoiceDto.setSalesType((SalesType) salesTypeValue);
            } else if (salesTypeValue instanceof String) {
                invoiceDto.setSalesType(SalesType.valueOf((String) salesTypeValue));
            }
            invoiceDto.setContractId((Long) tuple.get("contractId"));
            invoiceDto.setContractNumber((String) tuple.get("contractNumber"));
            invoiceDto.setCustomerId((Long) tuple.get("customerId"));
            invoiceDto.setCustomerName((String) tuple.get("customerName"));
            invoiceDto.setInvoiceStatusId((Integer) tuple.get("invoiceStatusId"));
            invoiceDto.setAdvancedPayment((Long) tuple.get("advancedPayment"));
            invoiceDto.setInsuranceDeposit((Long) tuple.get("insuranceDeposit"));
            invoiceDto.setPerformanceBound((Long) tuple.get("performanceBound"));
            invoiceDto.setYearId((Long) tuple.get("yearId"));

            // Set totalQuantity and totalPrice directly from the query result
            invoiceDto.setTotalQuantity((Long) tuple.get("totalQuantity"));
            invoiceDto.setTotalPrice((Double) tuple.get("totalPrice"));

            // Initialize invoiceItems list (or you might fetch them separately if needed)
            invoiceDto.setInvoiceItems(new ArrayList<>());

            return invoiceDto;
        }).collect(Collectors.toList());

        invoiceDtoList.forEach(invoiceDto -> {
            Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceDto.getId());
            optionalInvoice.ifPresent(invoice -> invoiceDto
                    .setInvoiceItems(invoice.getInvoiceItems().stream().map(invoiceItemMapper::toDto).collect(Collectors.toList()))
            );
        });


        // Calculate total pages
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDir.equalsIgnoreCase("asc")
                        ? Sort.Order.asc(Objects.requireNonNull(sortBy))
                        : Sort.Order.desc(Objects.requireNonNull(sortBy)))
        );

        // entire result set query:
        List<Tuple> overall = entityManager.createQuery(cq)
                .setFirstResult(0)
                .setMaxResults(Integer.MAX_VALUE)
                .getResultList();

        List<InvoiceDto> overallDtoList = convertToDtoList(overall);

        Double overallTotalQuantity = calculateTotalQuantity(overallDtoList);
        Double overallTotalPrice = calculateTotalPrice(overallDtoList);
        // Create a new CustomPageImpl with the overall totals
        CustomPageImpl<InvoiceDto> pageImpel = new CustomPageImpl<>(invoiceDtoList, pageRequest, getCount(invoiceSearch));
        pageImpel.setOverallTotalPrice(overallTotalPrice);
        pageImpel.setOverallTotalQuantity(overallTotalQuantity);
        return pageImpel;
    }

    private Double calculateTotalPrice(List<InvoiceDto> list) {

        return list.stream()
                .mapToDouble(dto -> dto.getInvoiceItems()
                        .stream()
                        .mapToDouble(InvoiceItemDto::getTotalPrice)
                        .sum())
                .sum();
    }

    private Double calculateTotalQuantity(List<InvoiceDto> list) {
        return list.stream()
                .mapToDouble(dto -> dto.getInvoiceItems().stream().mapToDouble(InvoiceItemDto::getQuantity).sum()).sum();
    }

    private List<InvoiceDto> convertToDtoList(List<Tuple> tuples) {
        TupleQueryHelper<InvoiceDto, Tuple> helper = new TupleQueryHelper<>(InvoiceDto.class);
        List<InvoiceDto> invoiceDtoList = helper.convertToDtoList(tuples);

        invoiceDtoList.forEach(invoiceDto -> {
            Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceDto.getId());
            optionalInvoice.ifPresent(invoice -> invoiceDto
                    .setInvoiceItems(invoice.getInvoiceItems().stream().map(invoiceItemMapper::toDto).collect(Collectors.toList()))
            );
        });
        return invoiceDtoList;
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
            throw new RuntimeException(e);
        }
    }

    public InvoiceDto getInvoiceById(Long id) {
        var invoiceEntity = invoiceRepository.findById(id).orElseThrow();
        return invoiceMapper.toDto(invoiceEntity);
    }


    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {


        var invoiceEntity = invoiceMapper.toEntity(invoiceDto);
        if (invoiceDto.getContractId() != null)
            invoiceEntity.setContract(contractRepository.findById(invoiceDto.getContractId()).orElseThrow());
        if (invoiceDto.getCustomerId() != null)
            invoiceEntity.setCustomer(customerRepository.findById(invoiceDto.getCustomerId()).orElseThrow());
        invoiceEntity.setYear(yearRepository.findByName(Long.valueOf(invoiceEntity.getJalaliYear())).orElseThrow());
        if (invoiceDto.getInvoiceStatusId() != null)
            invoiceEntity.setInvoiceStatus(invoiceStatusRepository.findById(invoiceDto.getInvoiceStatusId()).orElseThrow());
        validateInvoiceUniqueness(invoiceDto);
        invoiceEntity.getInvoiceItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new IllegalStateException("کالای مورد نظر پیدا نشد."));
            item.setProduct(product);
            WarehouseReceipt warehouseReceipt = warehouseReceiptRepository.findById(item.getWarehouseReceiptId()).orElseThrow(() -> new IllegalStateException("رسید انبار مورد نظر پیدا نشد."));
            item.setWarehouseReceipt(warehouseReceipt);
        });
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
                Long warehouseReceiptNumber = findWarehouseReceiptById(invoiceItemDto.getWarehouseReceiptId()).getWarehouseReceiptNumber();
                if (invoiceItemRepository.existsByWarehouseReceiptId(invoiceItemDto.getWarehouseReceiptId())) {
                    throw new IllegalStateException(String.format("برای این شماره حواله قبلا فاکتور صادر شده است. شماره حواله: %s", warehouseReceiptNumber));
                }
            });
        }
    }

    private void validateReceiptIdUniquenessOnUpdateEntity(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceItems() != null) {
            invoiceDto.getInvoiceItems().forEach(invoiceItemDto -> {
                Long warehouseReceiptNumber = findWarehouseReceiptById(invoiceItemDto.getWarehouseReceiptId()).getWarehouseReceiptNumber();
                if (invoiceItemRepository.existsByWarehouseReceiptIdAndIdNot(invoiceItemDto.getWarehouseReceiptId(), invoiceItemDto.getId())) {
                    throw new IllegalStateException(String.format("برای این شماره حواله قبلا فاکتور صادر شده است. شماره حواله: %s", warehouseReceiptNumber));
                }
            });
        }
    }
    private WarehouseReceipt findWarehouseReceiptById(Long id) {
        return warehouseReceiptRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("حواله پیدا نشد."));
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
                        wr ->  String.format("%s-%s",wr.getWarehouseReceiptNumber(),wr.getWarehouseReceiptDate()),
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
                            .orElseThrow(() -> new RowColumnException(finalRowNum, 10, String.format("سال با نام %s یافت نشد.",yearName), null));
                    int finalRowNum1 = rowNum;
                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new RowColumnException(finalRowNum1, 6, String.format("مشتری با کد %s یافت نشد.",customerCode), null));
                    int finalRowNum2 = rowNum;
                    Product product = Optional.ofNullable(productsMap.get(productCode))
                            .orElseThrow(() -> new RowColumnException(finalRowNum2, 13, String.format("محصول با کد %s یافت نشد.", productCode), null));

                    int finalRowNum3 = rowNum;
                    InvoiceStatus invoiceStatus = Optional.ofNullable(invoiceStatusesMap.get(invoiceStatusId))
                            .orElseThrow(() -> new RowColumnException(finalRowNum3, 16, String.format("وضعیت فاکتور با شماره %s یافت نشد.", invoiceNumber), null));

                    String receiptKey = String.format("%s-%s", warehouseReceiptNumber, warehouseReceiptDate);
                    int finalRowNum4 = rowNum;
                    warehouseReceipt = Optional.ofNullable(warehouseReceiptsMap.get(receiptKey))
                            .orElseThrow(() -> new RowColumnException(finalRowNum4, 14, String.format("رسید انبار با شماره %s و تاریخ %s یافت نشد.", warehouseReceiptNumber, warehouseReceiptDate), null));

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
                        dto.setInvoiceItems(new ArrayList<>());
                        return dto;
                    });

                    InvoiceItemDto itemDto = new InvoiceItemDto();
                    itemDto.setQuantity(Long.valueOf(Objects.requireNonNull(quantity,"تعداد نمی تواند null باشد.")));
                    itemDto.setUnitPrice(Double.valueOf(Objects.requireNonNull(unitPrice,"قیمت واحد نمی تواند null باشد.")));
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
