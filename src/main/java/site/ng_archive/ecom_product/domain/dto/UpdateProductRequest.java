package site.ng_archive.ecom_product.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProductRequest(
    @NotBlank(message = "product.name.blank")
    String name,

    @NotNull(message = "product.price.null")
    @Min(value = 0, message = "product.price.min")
    Long price
) {

    public UpdateProductCommand toCommand(Long id) {
        return new UpdateProductCommand(id, name, price);
    }

}
