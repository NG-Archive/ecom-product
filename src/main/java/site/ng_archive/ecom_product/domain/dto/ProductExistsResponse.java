package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.Product;

public record ProductExistsResponse(
    Long id,
    String name,
    Long memberId
) {
    public static ProductExistsResponse of(Product product) {
        return new ProductExistsResponse(product.id(), product.name(), product.memberId());
    }
}
