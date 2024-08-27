package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.*;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.exceptions.ConflictException;
import com.armaninvestment.parsparandreporterapplication.mappers.ReportItemMapper;
import com.armaninvestment.parsparandreporterapplication.mappers.ReportMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ReportSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.CellStyleHelper;
import com.armaninvestment.parsparandreporterapplication.utils.CustomPageImpl;
import com.armaninvestment.parsparandreporterapplication.utils.DateConvertor;
import com.armaninvestment.parsparandreporterapplication.utils.TupleQueryHelper;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
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
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final ReportItemRepository reportItemRepository;


    @PersistenceContext
        private EntityManager entityManager;
    private final ReportItemMapper reportItemMapper;


    public Page<ReportDto> findAll(int page, int size, String sortBy, String sortDir, ReportSearch reportSearch) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Report> root = cq.from(Report.class);
        Join<Report, ReportItem> reportItemJoin = root.join("reportItems", JoinType.LEFT);


        // Aggregation
        Expression<Long> totalQuantity = cb.sum(reportItemJoin.get("quantity"));
        Expression<Double> totalPrice = cb.sum(cb.prod(reportItemJoin.get("unitPrice"), reportItemJoin.get("quantity")));

        // Select
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("reportDate").alias("reportDate"),
                root.get("reportExplanation").alias("reportExplanation"),
                root.get("year").get("id").alias("yearId"),
                totalPrice.alias("totalPrice"),
                totalQuantity.alias("totalQuantity")
        );


        // Specification
        Specification<Report> specification = new ReportSpecification(reportSearch);
        cq.groupBy(root.get("id"), root.get("reportDate"), root.get("reportExplanation"), root.get("year").get("id"));
        cq.where(specification.toPredicate(root, cq, cb));

        // Sorting
        switch (Objects.requireNonNull(sortBy)) {
            case "totalPrice" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalPrice) : cb.desc(totalPrice));
            case "totalQuantity" ->   cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalQuantity) : cb.desc(totalQuantity));
            default -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy)));
        }
        // Pagination [ PAGE]
        List<Tuple> tuples = entityManager.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList().stream().toList();

        List<ReportDto> reportDtoList = convertToDtoList(tuples);

        // Calculate total pages
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDir.equalsIgnoreCase("asc")
                        ? Sort.Order.asc(Objects.requireNonNull(sortBy))
                        : Sort.Order.desc(Objects.requireNonNull(sortBy)))
        );

        // Pagination [OVERALL]
        List<Tuple> overall = entityManager.createQuery(cq)
                .setFirstResult(0)
                .setMaxResults(Integer.MAX_VALUE)
                .getResultList().stream().toList();

        List<ReportDto> overallDtoList = convertToDtoList(overall);


        Double overallTotalQuantity = calculateTotalQuantity(overallDtoList);
        Double overallTotalPrice = calculateTotalPrice(overallDtoList);

        // Create a new CustomPageImpl
        CustomPageImpl<ReportDto> pageImpel = new CustomPageImpl<>(reportDtoList, pageRequest, getCount(reportSearch));
        pageImpel.setOverallTotalPrice(overallTotalPrice);
        pageImpel.setOverallTotalQuantity(overallTotalQuantity);
        return pageImpel;
    }
    private List<ReportDto> convertToDtoList(List<Tuple> tuples) {
        TupleQueryHelper<ReportDto, Tuple> helper = new TupleQueryHelper<>(ReportDto.class);
        List<ReportDto> reportDtoList = helper.convertToDtoList(tuples);

        reportDtoList.forEach(reportDto -> {
            Optional<Report> optionalReport = reportRepository.findById(reportDto.getId());
            optionalReport.ifPresent(report -> reportDto
                    .setReportItems(report.getReportItems().stream().map(reportItemMapper::toDto).collect(Collectors.toSet()))
            );
        });
        return reportDtoList;
    }

    private Double calculateTotalQuantity(List<ReportDto> list) {
    return list.stream()
                .map(dto -> dto.getReportItems().stream().mapToDouble(ReportItemDto::getQuantity).sum())
                .mapToDouble(Double::doubleValue)
                .sum();
    }
    private Double calculateTotalPrice(List<ReportDto> list) {
        return list.stream()
                .map(dto -> dto.getReportItems()
                        .stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum())
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private Long getCount(ReportSearch reportSearch){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Report> root = cq.from(Report.class);

        cq.select(cb.count(root));

        Specification<Report> specification = new ReportSpecification(reportSearch);
        cq.where(specification.toPredicate(root, cq, cb));

        return entityManager.createQuery(cq).getSingleResult();
    }

    public ReportDto createReport(ReportDto reportDto) {
        LocalDate reportDate = reportDto.getReportDate();
        if (reportRepository.existsByReportDate(reportDate)) {
            throw new ConflictException("یک گزارش با همین تاریخ قبلاً ثبت شده است.");
        }

        if (reportDto.getReportItems() != null) {
            validateReportItemsForCreation(reportDto.getReportItems());
        }

        Report reportEntity = reportMapper.toEntity(reportDto);
        Report savedReport = reportRepository.save(reportEntity);
        return reportMapper.toDto(savedReport);
    }

    private void validateReportItemsForCreation(Set<ReportItemDto> reportItemDtos) {
        for (ReportItemDto reportItemDto : reportItemDtos) {
            if (reportItemRepository.existsByWarehouseReceiptId(reportItemDto.getWarehouseReceiptId())) {
                WarehouseReceipt warehouseReceipt = warehouseReceiptRepository.findById(reportItemDto.getWarehouseReceiptId())
                        .orElseThrow(() -> new EntityNotFoundException("رسید انبار با شناسه " + reportItemDto.getWarehouseReceiptId() + " یافت نشد."));
                throw new ConflictException("برای شماره حواله " + warehouseReceipt.getWarehouseReceiptNumber() + " قبلا گزارش ایجاد شده است.");
            }
        }
    }

    public ReportDto getReportById(Long id) {
        var reportEntity = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد."));
        return reportMapper.toDto(reportEntity);
    }

    public ReportDto updateReport(Long id, ReportDto reportDto) {
        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("گزارش با شناسه " + id + " یافت نشد.")); // Translated

        LocalDate reportDate = reportDto.getReportDate();
        if (reportRepository.existsByReportDateAndIdNot(reportDate, id)) {
            throw new ConflictException("گزارش دیگری با همین تاریخ وجود دارد."); // Translated
        }

        if (reportDto.getReportItems() != null) {
            validateReportItems(reportDto.getReportItems());
        }

        reportMapper.partialUpdate(reportDto, existingReport);
        Report updatedReport = reportRepository.save(existingReport);
        return reportMapper.toDto(updatedReport);
    }

    private void validateReportItems(Set<ReportItemDto> reportItemDtos) throws ConflictException {
        for (ReportItemDto reportItemDto : reportItemDtos) {
            if (reportItemRepository.existsByWarehouseReceiptIdAndIdNot(reportItemDto.getWarehouseReceiptId(), reportItemDto.getId())) {
                WarehouseReceipt warehouseReceipt = warehouseReceiptRepository.findById(reportItemDto.getWarehouseReceiptId())
                        .orElseThrow(() -> new EntityNotFoundException("رسید انبار با شناسه " + reportItemDto.getWarehouseReceiptId() + " یافت نشد.")); // Translated
                throw new ConflictException("برای شماره حواله " + warehouseReceipt.getWarehouseReceiptNumber() + " قبلا گزارش ایجاد شده است."); // Translated
            }
        }
    }


    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد.");
        }
        reportRepository.deleteById(id);
    }

    public byte[] exportReportsToExcel(ReportSearch search, boolean exportAll) {
        List<Report> reports;

        if (exportAll) {
            // Fetch all filtered data
            reports = getReportsBySearchCriteria(search);
        } else {
            // Fetch only the paginated result set
            Page<ReportDto> paginatedReports = findAll(search.getPage(), search.getSize(), search.getSortBy(), search.getOrder(), search);
            reports = paginatedReports.getContent().stream()
                    .map(reportMapper::toEntity)
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reports");
            sheet.setRightToLeft(true);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow);

            // Create data rows
            int rowNum = 1;
            for (Report report : reports) {
                Row row = sheet.createRow(rowNum++);
                populateReportRow(report, row);
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
            throw new RuntimeException("Failed to export reports to Excel", e);
        }
    }

    private void createHeaderCells(Row headerRow) {
        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle headerCellStyle = cellStyleHelper.getHeaderCellStyle(headerRow.getSheet().getWorkbook());

        String[] headers = {
                "تاریخ گزارش", "شرح گزارش", "مبلغ ( ریال)", "تعداد"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void populateReportRow(Report report, Row row) {
        int cellNum = 0;

        row.createCell(cellNum++).setCellValue(report.getReportDate() != null ? DateConvertor.convertGregorianToJalali(report.getReportDate()) : "");
        row.createCell(cellNum++).setCellValue(report.getReportExplanation() != null ? report.getReportExplanation() : "");

        // Calculate total quantity and total price
        long totalQuantity = report.getReportItems().stream().mapToLong(ReportItem::getQuantity).sum();
        double totalPrice = report.getReportItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();

        row.createCell(cellNum++).setCellValue(totalPrice);
        row.createCell(cellNum++).setCellValue(totalQuantity);

        CellStyleHelper cellStyleHelper = new CellStyleHelper();
        CellStyle defaultCellStyle = cellStyleHelper.getCellStyle(row.getSheet().getWorkbook());
        CellStyle monatoryCellStyle = cellStyleHelper.getMonatoryCellStyle(row.getSheet().getWorkbook());

        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            cell.setCellStyle(defaultCellStyle);
        }
        row.getCell(2).setCellStyle(monatoryCellStyle);
        row.getCell(3).setCellStyle(monatoryCellStyle);
    }

    private List<Report> getReportsBySearchCriteria(ReportSearch search) {
        Specification<Report> specification = new ReportSpecification(search);
        return reportRepository.findAll(specification);
    }


    public String importReportsFromExcel(MultipartFile file) throws IOException {
        Map<LocalDate, ReportDto> reportsMap = new HashMap<>();

        // Fetch all necessary data once
        Map<String, Customer> customersMap = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getCustomerCode, customer -> customer, (existing, replacement) -> existing));
        Map<Long, Year> yearsMap = yearRepository.findAll().stream()
                .collect(Collectors.toMap(Year::getName, year -> year, (existing, replacement) -> existing));
        Map<String, WarehouseReceipt> warehouseReceiptsMap = warehouseReceiptRepository.findAll().stream()
                .collect(Collectors.toMap(
                        wr -> wr.getWarehouseReceiptNumber() + "-" + wr.getWarehouseReceiptDate(),
                        wr -> wr,
                        (existing, replacement) -> existing
                ));
        Map<Long, Boolean> reportItemsMap = reportItemRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ri -> ri.getWarehouseReceipt().getId(),
                        ri -> true,
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
                    LocalDate reportDate = convertToDate(getCellStringValue(currentRow, 0, rowNum));
                    String reportExplanation = getCellStringValue(currentRow, 1, rowNum);
                    Long warehouseReceiptNumber = getCellLongValue(currentRow, 2, rowNum);
                    Long yearName = getCellLongValue(currentRow, 3, rowNum);
                    Long quantity = getCellLongValue(currentRow, 4, rowNum);
                    Double unitPrice = getCellDoubleValue(currentRow, 5, rowNum);
                    String customerCode = getCellStringValue(currentRow, 6, rowNum);
                    LocalDate warehouseReceiptDate = convertToDate(getCellStringValue(currentRow, 7, rowNum));

                    Year year = Optional.ofNullable(yearsMap.get(yearName))
                            .orElseThrow(() -> new IllegalStateException("سال با نام " + yearName + " یافت نشد."));
                    Customer customer = Optional.ofNullable(customersMap.get(customerCode))
                            .orElseThrow(() -> new IllegalStateException("مشتری با کد " + customerCode + " یافت نشد."));

                    String receiptKey = warehouseReceiptNumber + "-" + warehouseReceiptDate;
                    WarehouseReceipt warehouseReceipt = Optional.ofNullable(warehouseReceiptsMap.get(receiptKey))
                            .orElseThrow(() -> new IllegalStateException("رسید انبار با شماره " + warehouseReceiptNumber + " و تاریخ " + warehouseReceiptDate + " یافت نشد."));

                    // Check if the warehouse receipt is already referenced in any report item
                    if (Boolean.TRUE.equals(reportItemsMap.get(warehouseReceipt.getId()))) {
                        throw new IllegalStateException("رسید انبار با شماره " + warehouseReceiptNumber + " و تاریخ " + warehouseReceiptDate + " قبلاً در یک گزارش استفاده شده است.");
                    }

                    ReportDto reportDto = reportsMap.computeIfAbsent(reportDate, k -> {
                        ReportDto dto = new ReportDto();
                        dto.setReportDate(reportDate);
                        dto.setReportExplanation(reportExplanation);
                        dto.setYearId(year.getId());
                        dto.setReportItems(new LinkedHashSet<>());
                        return dto;
                    });

                    ReportItemDto itemDto = new ReportItemDto();
                    itemDto.setQuantity(quantity);
                    itemDto.setUnitPrice(unitPrice);
                    itemDto.setCustomerId(customer.getId());
                    itemDto.setWarehouseReceiptId(warehouseReceipt.getId());
                    reportDto.getReportItems().add(itemDto);
                } catch (Exception e) {
                    throw new RuntimeException("خطا در ردیف " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<Report> reports = reportsMap.values().stream()
                .map(reportMapper::toEntity)
                .collect(Collectors.toList());

        reportRepository.saveAll(reports);
        return reports.size() + " گزارش با موفقیت وارد شدند.";
    }
    private Year getMaxYear() {
        return yearRepository.findFirstByOrderByNameDesc();
    }

    public List<SalesByYearGroupByMonth> findSalesByYearGroupByMonth(Integer yearName, Integer productType) {
        Integer yearNameParam = (Integer) Objects.requireNonNullElseGet(yearName, () -> getMaxYear().getName());

        List<Object[]> results = reportRepository.getSalesByYearGroupByMonth(yearNameParam, productType);
        List<SalesByYearGroupByMonth> list = new ArrayList<>();

        // Create a map to store the results by month number
        Map<Short, SalesByYearGroupByMonth> resultMap = new HashMap<>();

        for (Object[] result : results) {
            resultMap.put((Short) result[0], new SalesByYearGroupByMonth(
                    (Short) result[0],     // month number
                    (String) result[1],      // month name
                    (Double) result[2],  // total amount
                    (Long) result[3]         // total quantity
            ));
        }

        // Iterate through all 12 months and add them to the list, using the resultMap
        for (short monthNumber = 1; monthNumber <= 12; monthNumber++) {
            SalesByYearGroupByMonth monthData = resultMap.get(monthNumber);

            if (monthData == null) {
                // Month has no result, create an item with zero values
                list.add(new SalesByYearGroupByMonth(
                        monthNumber,
                        getMonthName(monthNumber), // You can implement this method to get the month name
                        0D,
                        0L
                ));
            } else {
                list.add(monthData);
            }
        }

        return list;
    }
    private String getMonthName(int monthNumber) {
        // Define an array of Persian month names (replace with actual month names)
        String[] persianMonthNames = {
                "فروردین",
                "اردیبهشت",
                "خرداد",
                "تیر",
                "مرداد",
                "شهریور",
                "مهر",
                "آبان",
                "آذر",
                "دی",
                "بهمن",
                "اسفند"
        };

        // Check if the provided monthNumber is within a valid range (1 to 12)
        if (monthNumber >= 1 && monthNumber <= 12) {
            // Subtract 1 from the monthNumber to match the array index
            return persianMonthNames[monthNumber - 1];
        } else {
            return "Invalid Month";
        }
    }
}
