package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.controllers.ProductController;
import com.armaninvestment.parsparandreporterapplication.dtos.ProductDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ProductSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.Product;
import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.armaninvestment.parsparandreporterapplication.enums.ProductTypeConverter;
import com.armaninvestment.parsparandreporterapplication.mappers.ProductMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.ProductRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ProductSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.ProductSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.getCellIntValue;
import static com.armaninvestment.parsparandreporterapplication.utils.ExcelUtils.getCellStringValue;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(ProductService.class);

    public Page<ProductDto> findProductByCriteria(ProductSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<Product> specification = ProductSpecification.bySearchCriteria(search);
        return productRepository.findAll(specification, pageRequest)
                .map(productMapper::toDto);
    }
    public List<ProductSelectDto> findAllProductSelect(String searchParam) {
        Specification<Product> specification = ProductSpecification.getSelectSpecification(searchParam);
        return productRepository
                .findAll(specification)
                .stream()
                .map(productMapper::toSelectDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        var productEntity = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("محصول با این شناسه یافت نشد."));
        return productMapper.toDto(productEntity);
    }

    public ProductDto createProduct(ProductDto productDto) {
        validateProductUniqueness(productDto.getProductName(), productDto.getProductCode(), null);

        var productEntity = productMapper.toEntity(productDto);
        var savedProduct = productRepository.save(productEntity);
        return productMapper.toDto(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        var existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("محصول با این شناسه یافت نشد."));

        validateProductUniqueness(productDto.getProductName(), productDto.getProductCode(), id);

        productMapper.partialUpdate(productDto, existingProduct);
        var updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("محصول با این شناسه یافت نشد.");
        }
        productRepository.deleteById(id);
    }
    public byte[] exportProductsToExcel() throws IOException {
        List<ProductDto> productDtos = productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(productDtos, ProductDto.class);
    }

    public String importProductsFromExcel(MultipartFile file) throws IOException {
        Map<String, ProductDto> productMap = new HashMap<>();

        // Fetch all existing products once
        Map<String, Product> existingProductsMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getProductCode, product -> product, (existing, replacement) -> existing));

        Set<String> existingProductNames = existingProductsMap.values().stream()
                .map(Product::getProductName)
                .collect(Collectors.toSet());

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
                    String measurementIndex = getCellStringValue(currentRow, 0, rowNum);
                    String productCode = getCellStringValue(currentRow, 1, rowNum);
                    String productName = getCellStringValue(currentRow, 2, rowNum);
                    ProductType productType = ProductType.fromValue(getCellIntValue(currentRow, 3, rowNum));

                    // Check for uniqueness
                    if (existingProductsMap.containsKey(productCode)) {
                        throw new IllegalStateException("محصول با کد " + productCode + " وجود دارد.");
                    }
                    if (existingProductNames.contains(productName)) {
                        throw new IllegalStateException("محصول با نام " + productName + " وجود دارد.");
                    }

                    ProductDto productDto = new ProductDto(null, measurementIndex, productCode, productName, productType);
                    productMap.put(productCode, productDto);

                } catch (Exception e) {
                    throw new RuntimeException("خطا در ردیف " + rowNum + ": " + e.getMessage(), e);
                }
            }
        }

        List<Product> products = productMap.values().stream()
                .map(productMapper::toEntity)
                .collect(Collectors.toList());

        productRepository.saveAll(products);
        return products.size() + " محصول با موفقیت وارد شدند.";
    }

    private void validateProductUniqueness(String productName, String productCode, Long id) {
        if (id == null) {
            if (productRepository.existsByProductName(productName)) {
                logger.error("محصول با این نام {} وجود دارد.", productName);
                throw new IllegalStateException("محصول با این نام وجود دارد.");
            }
            if (productRepository.existsByProductCode(productCode)) {
                logger.error("محصول با کد {} وجود دارد.", productCode);
                throw new IllegalStateException("محصول با این کد وجود دارد.");
            }
        } else {
            if (productRepository.existsByProductNameAndAndIdNot(productName, id)) {
                logger.error("محصول با نام {} وجود دارد.", productName);
                throw new IllegalStateException("محصول با این نام وجود دارد.");
            }
            if (productRepository.existsByProductCodeAndAndIdNot(productCode, id)) {
                logger.error("محصول با کد {} وجود دارد.", productCode);
                throw new IllegalStateException("محصول با این کد وجود دارد.");
            }
        }
    }
}
