package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.Product;

public record UpdateProductResponse(
    Long id,
    String name,
    Long price,
    String status,
    String statusName
) {
    public static UpdateProductResponse from(Product product) {
        return new UpdateProductResponse(product.id(),
            product.name(),
            product.price(),
            product.status().name(),
            product.status().getDesc()
        );
    }
}
