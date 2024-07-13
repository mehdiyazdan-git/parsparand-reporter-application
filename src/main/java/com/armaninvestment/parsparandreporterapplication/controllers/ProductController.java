package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ProductDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ProductSelectDto;
import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.armaninvestment.parsparandreporterapplication.searchForms.ProductSearch;
import com.armaninvestment.parsparandreporterapplication.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(ProductController.class);

    @GetMapping(path = {"/", ""})
    public ResponseEntity<?> getAllProductsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) ProductType productType,
            ProductSearch search) {
        search.setProductType(productType);
        try {
            Page<ProductDto> products = productService.findProductByCriteria(search, page, size, sortBy, order);
            return ResponseEntity.ok(products);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping(path = "/select")
    public ResponseEntity<List<ProductSelectDto>> findAllProductSelect(
            @RequestParam(required = false) String searchQuery) {
        List<ProductSelectDto> productSelects = productService.findAllProductSelect(searchQuery);
        return ResponseEntity.ok(productSelects);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createProduct(@RequestBody ProductDto productDto){
       try {
           return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productDto));
       }catch (Exception e){
           logger.error(e.getMessage());
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
       }
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto){
        try {
            return ResponseEntity.ok(productService.updateProduct(id, productDto));
        }catch (Exception e){
               logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-products.xlsx")
    public ResponseEntity<byte[]> downloadAllProductsExcel() throws IOException {
        byte[] excelData = productService.exportProductsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_products.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importProductsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = productService.importProductsFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {

            logger.error("Failed to import products from Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import products from Excel file: " + e.getMessage());
        } catch (Exception e) {

            logger.error("Failed to import products from Excel file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
