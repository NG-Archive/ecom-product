package site.ng_archive.ecom_product.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_product.domain.dto.ProductCommand;
import site.ng_archive.ecom_product.domain.dto.ProductResponse;
import site.ng_archive.ecom_product.global.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Flux<ProductResponse> readAllProducts(long offset, int size) {
        return productRepository.findAllBy(offset, size)
            .map(ProductResponse::from);
    }

    public Mono<ProductResponse> readProduct(Long id) {
        return productRepository.findById(id)
            .map(ProductResponse::from)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))));
    }

    public Mono<ProductResponse> createProduct(ProductCommand command) {
        return productRepository.save(command.toEntity())
            .map(ProductResponse::from);
    }

}
