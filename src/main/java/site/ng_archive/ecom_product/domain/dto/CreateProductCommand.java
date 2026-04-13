package site.ng_archive.ecom_product.domain.dto;

public record CreateProductCommand(
    String name,
    Long price,
    Long quantity,
    Long memberId
) {
}
