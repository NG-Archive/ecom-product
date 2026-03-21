package site.ng_archive.ecom_product.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.handler.EntityNotFoundException;
import site.ng_archive.ecom_product.domain.dto.CreateProductCommand;
import site.ng_archive.ecom_product.domain.dto.ProductResponse;
import site.ng_archive.ecom_product.domain.dto.UpdateProductCommand;

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

    public Mono<ProductResponse> createProduct(CreateProductCommand command) {
        return productRepository.save(command.toEntity())
            .map(ProductResponse::from);
    }

    public Mono<ProductResponse> updateProduct(UpdateProductCommand command) {
        return productRepository.findById(command.id())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))))
            .flatMap(product -> {
                Product updatedProduct = product.update(command.name(), command.price());
                return productRepository.save(updatedProduct);
            })
            .map(ProductResponse::from);
    }

}
