package site.ng_archive.ecom_product.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.auth.Role;
import site.ng_archive.ecom_common.auth.UserContext;
import site.ng_archive.ecom_common.auth.aspect.LoginUser;
import site.ng_archive.ecom_common.auth.aspect.RequireRoles;
import site.ng_archive.ecom_product.domain.dto.*;

@RestController
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public Flux<ProductListResponse> readAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) long offset,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return productService.readAllProducts(offset, size);
    }

    @GetMapping("/product/{id}")
    public Mono<ProductResponse> readProduct(@PathVariable Long id) {
        return productService.readProduct(id);
    }

    @RequireRoles(roles = {Role.ROLES.SELLER})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/product")
    public Mono<ProductResponse> createProduct(
        @LoginUser UserContext user,
        @Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request.toCommand(user.id()));
    }

    @RequireRoles(roles = {Role.ROLES.SELLER})
    @PutMapping("/product/{id}")
    public Mono<UpdateProductResponse> updateProduct(
        @LoginUser UserContext user,
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(request.toCommand(id, user.id()));
    }

}
