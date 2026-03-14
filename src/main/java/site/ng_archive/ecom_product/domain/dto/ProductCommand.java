package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.Product;

public record ProductCommand(
    String name,
    Long price
) {
    public Product toEntity() {
        return new Product(null, name, price);
    }
}
