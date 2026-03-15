package site.ng_archive.ecom_product.domain.dto;

public record UpdateProductCommand(
    Long id,
    String name,
    Long price
) {
}
