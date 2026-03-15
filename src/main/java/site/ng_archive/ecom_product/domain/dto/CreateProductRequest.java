package site.ng_archive.ecom_product.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
    @NotBlank(message = "product.name.blank")
    String name,

    @NotNull(message = "product.price.null")
    @Min(value = 0, message = "product.price.min")
    Long price
) {

    public CreateProductCommand toCommand() {
        return new CreateProductCommand(name, price);
    }

}
