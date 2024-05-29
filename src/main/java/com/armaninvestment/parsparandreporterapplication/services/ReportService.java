package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.ReportDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.mappers.ReportMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ReportSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
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
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final ReportItemRepository reportItemRepository;


        @PersistenceContext
        private EntityManager entityManager;

    private Page<Report> findReportsFromCriteria(Pageable pageable,ReportSearch reportSearch) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Specification<Report> specification = new ReportSpecification(reportSearch);

        CriteriaQuery<Report> query = cb.createQuery(Report.class);
        Root<Report> root = query.from(Report.class);
        query
                .select(root)
                .where(specification.toPredicate(root, query, cb));

        List<Report> pagedData = paginateQuery(entityManager.createQuery(query), pageable).getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Report> countRoot = countQuery.from(Report.class);
        countQuery
                .select(cb.count(countRoot))
                .where(specification.toPredicate(countRoot, countQuery, cb));
        var totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(pagedData, pageable, totalCount);
    }
    public static <T> TypedQuery<T> paginateQuery(TypedQuery<T> query, Pageable pageable) {
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query;
    }


        public Page<ReportDto> findReportByCriteria(ReportSearch searchCriteria, int page, int size, String sortBy, String order) {
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<Report> specification = new ReportSpecification(searchCriteria);

            // Calculate total elements
            long totalElements = countReports(specification);

            // Main query for results
            List<Report> reports = getReports(specification, pageable, sortBy, order);
            List<ReportDto> reportDtos = reports.stream().map(reportMapper::toDto).toList();

            // Create Page object
            return new PageImpl<>(reportDtos, pageable, totalElements);
        }

        private long countReports(Specification<Report> specification) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
            Root<Report> countRoot = countQuery.from(Report.class);
            countQuery.select(criteriaBuilder.countDistinct(countRoot));
            Predicate predicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
            if (predicate != null) {
                countQuery.where(predicate);
            }
            return entityManager.createQuery(countQuery).getSingleResult();
        }

        private List<Report> getReports(Specification<Report> specification, Pageable pageable, String sortBy, String order) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Report> query = criteriaBuilder.createQuery(Report.class);
            Root<Report> root = query.from(Report.class);
            query.where(specification.toPredicate(root, query, criteriaBuilder));

            // Custom sorting logic for totalPrice and totalQuantity
            if ("totalPrice".equals(sortBy) || "totalQuantity".equals(sortBy)) {
                Join<Report, ReportItem> reportItemJoin = root.join("reportItems", JoinType.LEFT);
                if ("totalPrice".equals(sortBy)) {
                    Expression<Double> totalPriceExpression = criteriaBuilder.sum(
                            criteriaBuilder.toDouble(
                                    criteriaBuilder.prod(
                                            criteriaBuilder.toDouble(reportItemJoin.get("unitPrice")),
                                            criteriaBuilder.toDouble(reportItemJoin.get("quantity"))
                                    )
                            )

                    );
                    if ("asc".equalsIgnoreCase(order)) {
                        query.orderBy(criteriaBuilder.asc(totalPriceExpression));
                    } else {
                        query.orderBy(criteriaBuilder.desc(totalPriceExpression));
                    }
                } else if ("totalQuantity".equals(sortBy)) {
                    Expression<Long> totalQuantityExpression = criteriaBuilder.sum(
                            criteriaBuilder.toLong(reportItemJoin.get("quantity"))
                    );
                    if ("asc".equalsIgnoreCase(order)) {
                        query.orderBy(criteriaBuilder.asc(totalQuantityExpression));
                    } else {
                        query.orderBy(criteriaBuilder.desc(totalQuantityExpression));
                    }
                }
                query.groupBy(root.get("id")); // Group by report ID to aggregate totalPrice and totalQuantity correctly
            } else {
                query.orderBy(criteriaBuilder.asc(root.get(sortBy)));
            }

            TypedQuery<Report> typedQuery = entityManager.createQuery(query);
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());

            return typedQuery.getResultList();
        }


    public ReportDto createReport(ReportDto reportDto) {
        LocalDate reportDate = reportDto.getReportDate();
        if (reportRepository.existsByReportDate(reportDate)) {
            throw new IllegalStateException("یک گزارش با همین تاریخ قبلاً ثبت شده است.");
        }
        var reportEntity = reportMapper.toEntity(reportDto);
        var savedReport = reportRepository.save(reportEntity);
        return reportMapper.toDto(savedReport);
    }

    public ReportDto getReportById(Long id) {
        var reportEntity = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد."));
        return reportMapper.toDto(reportEntity);
    }

    public ReportDto updateReport(Long id, ReportDto reportDto) {
        var existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد."));

        LocalDate reportDate = reportDto.getReportDate();
        if (reportRepository.existsByReportDateAndIdNot(reportDate, id)) {
            throw new IllegalStateException("یک گزارش دیگر با همین تاریخ وجود دارد.");
        }

        reportMapper.partialUpdate(reportDto, existingReport);
        var updatedReport = reportRepository.save(existingReport);
        return reportMapper.toDto(updatedReport);
    }


    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new IllegalStateException("گزارش با شناسه " + id + " پیدا نشد.");
        }
        reportRepository.deleteById(id);
    }

    public byte[] exportReportsToExcel() throws IOException {
        List<ReportDto> reportDtos = reportRepository.findAll().stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(reportDtos, ReportDto.class);
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
}
