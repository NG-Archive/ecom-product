package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.Product;

public record ProductListResponse(
    Long id,
    String name,
    Long price
) {
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(product.id(), product.name(), product.price());
    }
}
