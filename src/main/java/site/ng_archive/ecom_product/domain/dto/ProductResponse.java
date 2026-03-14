package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.Product;

public record ProductResponse(
    Long id,
    String name,
    Long price
) {

    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(product.id(), product.name(), product.price());
    }

}
