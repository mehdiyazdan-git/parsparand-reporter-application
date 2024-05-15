package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.CustomerSelect;
import com.armaninvestment.parsparandreporterapplication.dtos.ProductDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ProductSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Product;
import com.armaninvestment.parsparandreporterapplication.mappers.ProductMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.ProductRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.ProductSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.CustomerSpecification;
import com.armaninvestment.parsparandreporterapplication.specifications.ProductSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

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

    public String importProductsFromExcel(MultipartFile file) throws IOException {
        List<ProductDto> productDtos = ExcelDataImporter.importData(file, ProductDto.class);
        List<Product> products = productDtos.stream()
                .map(productMapper::toEntity)
                .collect(Collectors.toList());

        for (Product product : products) {
            validateProductUniqueness(product.getProductName(), product.getProductCode(), null);
        }

        productRepository.saveAll(products);
        return products.size() + " محصول با موفقیت وارد شدند.";
    }

    public byte[] exportProductsToExcel() throws IOException {
        List<ProductDto> productDtos = productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(productDtos, ProductDto.class);
    }

    private void validateProductUniqueness(String productName, String productCode, Long id) {
        if (id == null) {
            if (productRepository.existsByProductName(productName)) {
                throw new IllegalStateException("محصول با این نام وجود دارد.");
            }
            if (productRepository.existsByProductCode(productCode)) {
                throw new IllegalStateException("محصول با این کد وجود دارد.");
            }
        } else {
            if (productRepository.existsByProductNameAndAndIdNot(productName, id)) {
                throw new IllegalStateException("محصول با این نام وجود دارد.");
            }
            if (productRepository.existsByProductCodeAndAndIdNot(productCode, id)) {
                throw new IllegalStateException("محصول با این کد وجود دارد.");
            }
        }
    }
}
