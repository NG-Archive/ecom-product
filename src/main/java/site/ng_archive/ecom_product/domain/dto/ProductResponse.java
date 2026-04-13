package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.Product;

public record ProductResponse(
    Long id,
    String name,
    Long price,
    Long quantity,
    Long memberId
) {
    public static ProductResponse of(Product product, Long quantity) {
        return new ProductResponse(product.id(), product.name(), product.price(), quantity, product.memberId());
    }
}
