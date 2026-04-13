package site.ng_archive.ecom_product.domain.dto;

public record CreateStockRequest(
    Long productId,
    Long quantity
) {
}
