package site.ng_archive.ecom_product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("product")
public record Product(
        @Id
        Long id,
        String name,
        Long price,
        ProductStatus status,
        Long memberId
) {
    public static Product createInitial(String name, Long price, Long memberId) {
        return new Product(null, name, price, ProductStatus.PENDING, memberId);
    }

    public Product update(String name, Long price, ProductStatus status, Long memberId) {
        return new Product(this.id, name, price, status, memberId);
    }

    public Product withStatus(ProductStatus newStatus) {
        return new Product(id, name, price, newStatus, memberId);
    }
}
