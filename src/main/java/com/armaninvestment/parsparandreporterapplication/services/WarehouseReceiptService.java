package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptSelect;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.mappers.WarehouseReceiptItemMapper;
import com.armaninvestment.parsparandreporterapplication.mappers.WarehouseReceiptMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.WarehouseReceiptSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.*;

@Service
@RequiredArgsConstructor
public class WarehouseReceiptService {
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final WarehouseReceiptMapper warehouseReceiptMapper;
    private final WarehouseReceiptItemMapper warehouseReceiptItemMapper;
    private final YearRepository yearRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseInvoiceRepository warehouseInvoiceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<WarehouseReceiptDto> findAll(int page, int size, String sortBy, String sortDir, WarehouseReceiptSearch warehouseReceiptSearch) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<WarehouseReceipt> root = cq.from(WarehouseReceipt.class);
        Join<WarehouseReceipt, WarehouseReceiptItem> warehouseReceiptItemJoin = root.join("warehouseReceiptItems", JoinType.LEFT);

        // Aggregation
        Expression<Long> totalQuantity = cb.sum(warehouseReceiptItemJoin.get("quantity").as(Long.class));
        Expression<Double> totalPrice = cb.sum(cb.prod(cb.toDouble(warehouseReceiptItemJoin.get("unitPrice")), cb.toDouble(warehouseReceiptItemJoin.get("quantity"))));

        // Select
        CriteriaQuery<Tuple> multiselect = cq.multiselect(
                root.get("id").alias("id"),
                root.get("warehouseReceiptDate").alias("warehouseReceiptDate"),
                root.get("warehouseReceiptDescription").alias("warehouseReceiptDescription"),
                root.get("warehouseReceiptNumber").alias("warehouseReceiptNumber"),
                root.get("customer").get("id").alias("customerId"),
                root.get("customer").get("name").alias("customerName"),
                root.get("year").get("id").alias("yearId"),
                root.get("year").get("name").alias("yearName"),
                totalQuantity.alias("totalQuantity"),
                totalPrice.alias("totalPrice")
        );

        // Specification
        Specification<WarehouseReceipt> specification = WarehouseReceiptSpecification.bySearchCriteria(warehouseReceiptSearch);
        cq.groupBy(root.get("id"), root.get("warehouseReceiptDate"), root.get("warehouseReceiptDescription"), root.get("warehouseReceiptNumber"),
                root.get("customer").get("id"), root.get("customer").get("name"),
                root.get("year").get("id"), root.get("year").get("name"), root.get("year").get("name"));
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
        List<WarehouseReceiptDto> warehouseReceiptDtoList = tuples.stream().map(tuple -> new WarehouseReceiptDto(
                tuple.get("id", Long.class),
                tuple.get("warehouseReceiptDate", LocalDate.class),
                tuple.get("warehouseReceiptDescription", String.class),
                tuple.get("warehouseReceiptNumber", Long.class),
                tuple.get("customerId", Long.class),
                tuple.get("customerName", String.class),
                tuple.get("yearId", Long.class),
                tuple.get("yearName", Long.class),
                tuple.get("totalQuantity", Long.class),
                tuple.get("totalPrice", Double.class),
                new LinkedHashSet<>() // Assuming you will fill this set later
        )).collect(Collectors.toList());

        warehouseReceiptDtoList.forEach(warehouseReceiptDto -> {
            Optional<WarehouseReceipt> optionalWarehouseReceipt = warehouseReceiptRepository.findById(warehouseReceiptDto.getId());
            optionalWarehouseReceipt.ifPresent(warehouseReceipt -> warehouseReceiptDto
                    .setWarehouseReceiptItems(warehouseReceipt.getWarehouseReceiptItems().stream().map(warehouseReceiptItemMapper::toDto).collect(Collectors.toSet()))
            );
        });


        // Calculate total pages
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(Objects.requireNonNull(sortBy)) : Sort.Order.desc(Objects.requireNonNull(sortBy)))
        );

        return new PageImpl<>(warehouseReceiptDtoList, pageRequest, getCount(warehouseReceiptSearch));
    }

    private Long getCount(WarehouseReceiptSearch warehouseReceiptSearch){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<WarehouseReceipt> root = cq.from(WarehouseReceipt.class);

        cq.select(cb.count(root));

        Specification<WarehouseReceipt> specification = WarehouseReceiptSpecification.bySearchCriteria(warehouseReceiptSearch);
        cq.where(specification.toPredicate(root, cq, cb));

        return entityManager.createQuery(cq).getSingleResult();
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
        WarehouseInvoice warehouseInvoice = new WarehouseInvoice();
        warehouseInvoice.setWarehouseReceipt(savedWarehouseReceipt);
        warehouseInvoiceRepository.save(warehouseInvoice);
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
        warehouseInvoiceRepository.deleteByReceiptId(id);
    }

    public String importWarehouseReceiptsFromExcel(MultipartFile file) throws IOException {
        Map<String, WarehouseReceiptDto> warehouseReceiptsMap = new HashMap<>();

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
                    //receiptNumber , receiptDate , receiptDescription , customerCode , yearName , quantity , unitPrice , productCode
                    Long receiptNumber = getCellLongValue(currentRow, 0, rowNum);
                    LocalDate receiptDate = convertToDate(getCellStringValue(currentRow, 1, rowNum));
                    String receiptDescription = getCellStringValue(currentRow, 2, rowNum);
                    String customerCode = getCellStringValue(currentRow, 3, rowNum);
                    Long yearName = getCellLongValue(currentRow, 4, rowNum);
                    Integer quantity = getCellIntValue(currentRow, 5, rowNum);
                    Long unitPrice = getCellLongValue(currentRow, 6, rowNum);
                    String productCode = getCellStringValue(currentRow, 7, rowNum);

                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new IllegalStateException("مشتری با کد " + customerCode + " یافت نشد."));
                    Year year = Optional.ofNullable(yearsMap.get(yearName))
                            .orElseThrow(() -> new IllegalStateException("سال با نام " + yearName + " یافت نشد."));
                    Product product = Optional.ofNullable(productsMap.get(productCode))
                            .orElseThrow(() -> new IllegalStateException("محصول با کد " + productCode + " یافت نشد."));

                    assert receiptDate != null;
                    WarehouseReceiptDto receiptDto = warehouseReceiptsMap.computeIfAbsent(
                            String.valueOf(receiptNumber).concat(receiptDate.format(DateTimeFormatter.BASIC_ISO_DATE)), k -> {
                        WarehouseReceiptDto dto = new WarehouseReceiptDto();
                        dto.setWarehouseReceiptNumber(receiptNumber);
                        dto.setWarehouseReceiptDate(receiptDate);
                        dto.setWarehouseReceiptDescription(receiptDescription);
                        dto.setCustomerId(customer.getId());
                        dto.setCustomerName(customer.getName());
                        dto.setYearId(year.getId());
                        dto.setYearName(year.getName());
                        dto.setWarehouseReceiptItems(new LinkedHashSet<>());
                        return dto;
                    });

                    WarehouseReceiptItemDto itemDto = new WarehouseReceiptItemDto();
                    itemDto.setQuantity(quantity);
                    itemDto.setUnitPrice(unitPrice);
                    itemDto.setProductId(product.getId());
                    receiptDto.getWarehouseReceiptItems().add(itemDto);
                } catch (Exception e) {
                    throw new RuntimeException("خطا در ردیف " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<WarehouseReceipt> warehouseReceipts = warehouseReceiptsMap.values().stream()
                .map(warehouseReceiptMapper::toEntity)
                .collect(Collectors.toList());

        List<WarehouseReceipt> warehouseReceiptList = warehouseReceiptRepository.saveAll(warehouseReceipts);
        Set<WarehouseInvoice> warehouseInvoices = warehouseReceiptList
                .stream()
                .map(warehouseReceipt -> {
                        WarehouseInvoice warehouseInvoice = new WarehouseInvoice();
                        warehouseInvoice.setWarehouseReceipt(warehouseReceipt);
                        return warehouseInvoice;
                })
                .collect(Collectors.toSet());


        warehouseInvoiceRepository.saveAll(warehouseInvoices);

        return warehouseReceipts.size() + " رسید انبار با موفقیت وارد شد.";
    }

    public List<WarehouseReceiptSelect> findAllWarehouseReceiptSelect(String searchQuery,Long yearId)  {
        List<Object[]> results = warehouseReceiptRepository.searchWarehouseReceiptByDescriptionKeywords(searchQuery,yearId);
        return results.stream().map(result -> {

            WarehouseReceiptSelect warehouseReceiptSelect = new WarehouseReceiptSelect();

            warehouseReceiptSelect.setId((Long) result[0]);
            warehouseReceiptSelect.setName((String) result[1]);

            return warehouseReceiptSelect;
        }).collect(Collectors.toList());
    }

    public Integer getCurrentYear() {
        LocalDate date = LocalDate.now();
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return jalaliDate.getYear();
    }



    public byte[] exportWarehouseReceiptsToExcel(WarehouseReceiptSearch search, boolean exportAll) throws IllegalAccessException {
        List<WarehouseReceiptDto> warehouseReceipts;

        if (exportAll) {
            warehouseReceipts = findAll(search);
        } else {
            Page<WarehouseReceiptDto> paginatedWarehouseReceipts = findPage(search.getPage(), search.getSize(), search.getSortBy(), search.getOrder(), search);
            warehouseReceipts = paginatedWarehouseReceipts.getContent();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Warehouse Receipts");
            // set direction to rtl
            sheet.setRightToLeft(true);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Initialize totals
            long totalQuantity = 0;
            double totalPrice = 0;

            // Create data rows
            int rowNum = 1;
            for (WarehouseReceiptDto warehouseReceipt : warehouseReceipts) {
                Row row = sheet.createRow(rowNum++);
                populateWarehouseReceiptRow(warehouseReceipt, row);

                // Sum totals
                totalQuantity += warehouseReceipt.getWarehouseReceiptItems().stream().mapToLong(WarehouseReceiptItemDto::getQuantity).sum();
                totalPrice += warehouseReceipt.getWarehouseReceiptItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();
            }

            // Create subtotal row
            Row subtotalRow = sheet.createRow(rowNum);
            createSubtotalRow(subtotalRow, totalQuantity, totalPrice);

            // Adjust column widths
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to a byte array output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export warehouse receipts to Excel", e);
        }
    }

    private void createSubtotalRow(Row subtotalRow, long totalQuantity, double totalPrice) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle footerCellStyle = cellStyleHelper.getFooterCellStyle(subtotalRow.getSheet().getWorkbook());

        int cellNum = 0;
        subtotalRow.createCell(cellNum++).setCellValue("جمع کل"); // Subtotal label
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
        subtotalRow.createCell(cellNum++).setCellValue("");
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
        subtotalRow.getCell(6).setCellStyle(footerCellStyle);
        subtotalRow.getCell(7).setCellStyle(footerCellStyle);
    }

    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "شناسه حواله", "تاریخ حواله", "شرح حواله", "شماره حواله", "شناسه مشتری",
                "نام مشتری", "تعداد", "مبلغ (ریال)"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void populateWarehouseReceiptRow(WarehouseReceiptDto warehouseReceipt, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(warehouseReceipt.getId() != null ? warehouseReceipt.getId() : 0);
        row.createCell(cellNum++).setCellValue(warehouseReceipt.getWarehouseReceiptDate() != null ? DateConvertor.convertGregorianToJalali(warehouseReceipt.getWarehouseReceiptDate()) : "");
        row.createCell(cellNum++).setCellValue(warehouseReceipt.getWarehouseReceiptDescription() != null ? warehouseReceipt.getWarehouseReceiptDescription() : "");
        row.createCell(cellNum++).setCellValue(warehouseReceipt.getWarehouseReceiptNumber() != null ? warehouseReceipt.getWarehouseReceiptNumber() : 0);
        row.createCell(cellNum++).setCellValue(warehouseReceipt.getCustomerId() != null ? warehouseReceipt.getCustomerId() : 0);
        row.createCell(cellNum++).setCellValue(warehouseReceipt.getCustomerName() != null ? warehouseReceipt.getCustomerName() : "");

        // Calculate total quantity and total price
        long totalQuantity = warehouseReceipt.getWarehouseReceiptItems().stream().mapToLong(WarehouseReceiptItemDto::getQuantity).sum();
        double totalPrice = warehouseReceipt.getWarehouseReceiptItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();

        row.createCell(cellNum++).setCellValue(totalQuantity);
        row.createCell(cellNum++).setCellValue(totalPrice);

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());

        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            cell.setCellStyle(defaultCellStyle);
        }
        row.getCell(6).setCellStyle(monatoryCellStyle);
        row.getCell(7).setCellStyle(monatoryCellStyle);
    }

    private List<WarehouseReceiptDto> findAll(WarehouseReceiptSearch search) {
        Specification<WarehouseReceipt> warehouseReceiptSpecification = WarehouseReceiptSpecification.bySearchCriteria(search);
        List<WarehouseReceipt> warehouseReceipts = warehouseReceiptRepository.findAll(warehouseReceiptSpecification);
        return warehouseReceipts.stream().map(warehouseReceiptMapper::toDto).collect(Collectors.toList());
    }

    private Page<WarehouseReceiptDto> findPage(int page, int size, String sortBy, String order, WarehouseReceiptSearch search) {
        var direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, direction, sortBy);
        Specification<WarehouseReceipt> spec = WarehouseReceiptSpecification.bySearchCriteria(search);
        Page<WarehouseReceipt> paginatedWarehouseReceipts = warehouseReceiptRepository.findAll(spec, pageable);
        return paginatedWarehouseReceipts.map(warehouseReceiptMapper::toDto);
    }

}
