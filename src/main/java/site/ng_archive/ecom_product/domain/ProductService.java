package site.ng_archive.ecom_product.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import site.ng_archive.ecom_product.domain.dto.ProductResponse;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Flux<ProductResponse> readAllProducts(long offset, int size) {
        return productRepository.findAllBy(offset, size)
            .map(ProductResponse::fromEntity);
    }

}
