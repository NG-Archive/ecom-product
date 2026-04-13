package site.ng_archive.ecom_product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    PENDING("처리대기"),
    FAILED("처리실패"),
    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private final String desc;
}
