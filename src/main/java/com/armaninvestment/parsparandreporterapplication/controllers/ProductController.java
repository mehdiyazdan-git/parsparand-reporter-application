package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.ProductDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.ProductSearch;
import com.armaninvestment.parsparandreporterapplication.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping(path = {"/products", ""})
    public ResponseEntity<Page<ProductDto>> getAllProductsByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            ProductSearch search) {
        Page<ProductDto> products = productService.findProductByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(products);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.createProduct(productDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import products from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
