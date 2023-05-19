package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.ListProductDto;
import br.edu.unifip.ecommerceapi.dtos.ProductDto;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.service.ProductService;
import br.edu.unifip.ecommerceapi.utils.FileDownloadUtil;
import br.edu.unifip.ecommerceapi.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ListProductDto>> getAllProducts() {

        return ResponseEntity.ok().body(productService.getAllProduct());

    }

    @GetMapping("/active")
    public ResponseEntity<List<ListProductDto>> getProductIsActive() {

        return ResponseEntity.ok().body(productService.findByActiveTrue());

    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable UUID id) {

        return ResponseEntity.ok().body(productService.getById(id));

    }

    @PostMapping
    public ResponseEntity<Object> saveProduct(@Valid ProductDto productDto, UriComponentsBuilder uriBuilder, HttpServletRequest request) throws IOException {

        var product = new Product(productDto);
        UUID categoryId = null;

        if (productDto.getIdCategory() != null) {
            categoryId = productDto.getIdCategory();
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");

        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "product-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                product.setImage("/api/products/product-images/" + filecode);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted.");
            }
        }

        var uri = uriBuilder.path("/api/products/{id}").buildAndExpand(product.getId()).toUri();

        return ResponseEntity.created(uri).body(productService.save(product, categoryId));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable UUID id) {
        if (productService.delete(id)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Product was delected");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");

    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, HttpServletRequest request) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        Map<Object, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue()[0]);
        }

        // Salvar a url da imagem em uma variável separada
        String imageUrl = null;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");
        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "product-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                imageUrl = "/api/products/product-images/" + filecode;
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted.");
            }
        }

        // Adicionar a url da imagem ao objeto mapeado, se ela foi enviada
        if (imageUrl != null) {
            objectMap.put("image", imageUrl);
        }

        productService.partialUpdate(productOptional.get(), objectMap);
        return ResponseEntity.status(HttpStatus.OK).body(productOptional.get());
    }

    @GetMapping("/product-images/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {
        FileDownloadUtil downloadUtil = new FileDownloadUtil();

        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(fileCode, "product-images");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        MediaType contentType;

        if (Objects.equals(FilenameUtils.getExtension(resource.getFilename()), "jpg")) {
            contentType = MediaType.IMAGE_JPEG;
        } else {
            contentType = MediaType.IMAGE_PNG;
        }

        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}
