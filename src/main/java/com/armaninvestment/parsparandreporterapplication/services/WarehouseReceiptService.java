package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptSelect;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Product;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.mappers.WarehouseReceiptMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.ProductRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.WarehouseReceiptRepository;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.WarehouseReceiptSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.*;

@Service
@RequiredArgsConstructor
public class WarehouseReceiptService {
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final WarehouseReceiptMapper warehouseReceiptMapper;
    private final YearRepository yearRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public Page<WarehouseReceiptDto> findWarehouseReceiptByCriteria(WarehouseReceiptSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<WarehouseReceipt> specification = WarehouseReceiptSpecification.bySearchCriteria(search);
        return warehouseReceiptRepository.findAll(specification, pageRequest)
                .map(warehouseReceiptMapper::toDto);
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
    }

    public byte[] exportWarehouseReceiptsToExcel() throws IOException {
        List<WarehouseReceiptDto> warehouseReceiptDtos = warehouseReceiptRepository.findAll().stream().map(warehouseReceiptMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(warehouseReceiptDtos, WarehouseReceiptDto.class);
    }

    public String importWarehouseReceiptsFromExcel(MultipartFile file) throws IOException {
        Map<Long, WarehouseReceiptDto> warehouseReceiptsMap = new HashMap<>();

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

                    WarehouseReceiptDto receiptDto = warehouseReceiptsMap.computeIfAbsent(receiptNumber, k -> {
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

        warehouseReceiptRepository.saveAll(warehouseReceipts);
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
}
