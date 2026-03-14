package site.ng_archive.ecom_product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("product")
public record Product(
        @Id
        Long id,
        String name,
        Long price
) {
}
