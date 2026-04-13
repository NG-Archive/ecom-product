package site.ng_archive.ecom_product.domain.dto;

public record CreateStockResponse(
    Long id,
    Long productId,
    Long quantity
) {
}
