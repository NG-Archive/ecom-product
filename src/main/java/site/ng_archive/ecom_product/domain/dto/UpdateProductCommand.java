package site.ng_archive.ecom_product.domain.dto;

import site.ng_archive.ecom_product.domain.ProductStatus;

public record UpdateProductCommand(
    Long id,
    String name,
    Long price,
    ProductStatus status,
    Long memberId
) {
}
