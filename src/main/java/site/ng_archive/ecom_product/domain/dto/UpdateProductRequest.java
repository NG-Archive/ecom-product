package site.ng_archive.ecom_product.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.ng_archive.ecom_product.domain.ProductStatus;

public record UpdateProductRequest(
    @NotBlank(message = "product.name.blank")
    String name,

    @NotNull(message = "product.price.null")
    @Min(value = 0, message = "product.price.min")
    Long price,

    @NotNull(message = "product.status.null")
    ProductStatus status
) {
    public UpdateProductCommand toCommand(Long id, Long memberId) {
        return new UpdateProductCommand(id, name, price, status, memberId);
    }
}
