package site.ng_archive.ecom_product.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
    @NotBlank(message = "product.name.blank")
    String name,

    @NotNull(message = "product.price.null")
    @Min(value = 0, message = "product.price.min")
    Long price,

    @NotNull(message = "product.quantity.null")
    @Min(value = 0, message = "product.quantity.min")
    Long quantity
) {

    public CreateProductCommand toCommand(Long memberId) {
        return new CreateProductCommand(name, price, quantity, memberId);
    }

}
