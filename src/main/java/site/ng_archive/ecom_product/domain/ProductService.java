package site.ng_archive.ecom_product.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.auth.exception.ForbiddenException;
import site.ng_archive.ecom_common.handler.EntityNotFoundException;
import site.ng_archive.ecom_product.domain.dto.*;
import site.ng_archive.ecom_product.domain.publisher.ProductEventPublisher;
import site.ng_archive.ecom_product.domain.requester.StockRequester;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRequester stockRequester;
    private final ProductEventPublisher productEventPublisher;

    private final TransactionalOperator transactionalOperator;

    public Flux<ProductListResponse> readAllProducts(long offset, int size) {
        return productRepository.findAllBy(offset, size)
            .map(ProductListResponse::from);
    }

    public Mono<ProductResponse> readProduct(Long id) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("product.notfound")))
            .flatMap(product -> stockRequester.getStock(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("stock.notfound")))
                .map(stock -> ProductResponse.of(product, stock.quantity()))
            );
    }

    public Mono<ProductResponse> createProduct(CreateProductCommand command) {
        Product product = Product.createInitial(command.name(), command.price(), command.memberId());

        return productRepository.save(product)
            .as(transactionalOperator::transactional)
            .flatMap(saved ->
                stockRequester.createStock(saved.id(), command.quantity())
                    .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("stock.notfound")))
                    .then(updateProductStatus(saved, ProductStatus.ACTIVE))
                    .onErrorResume(e ->
                        updateProductStatus(saved, ProductStatus.FAILED)
                            .then(Mono.error(e)))
                    .thenReturn(ProductResponse.of(saved, command.quantity()))
            );
    }

    public Mono<UpdateProductResponse> updateProduct(UpdateProductCommand command) {
        return productRepository.findById(command.id())
            .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("product.notfound")))
            .filter(product -> command.memberId().equals(product.memberId()))
            .switchIfEmpty(Mono.error(() -> new ForbiddenException("product.forbidden")))
            .flatMap(product -> {
                Product updatedProduct = product.update(command.name(), command.price(), command.status(), command.memberId());
                return productRepository.save(updatedProduct)
                    .delayUntil(savedProduct -> {
                        if (savedProduct.price().equals(product.price())) {
                            return Mono.empty();
                        }
                        return productEventPublisher.publishChangeEvent(savedProduct);
                    });
            })
            .map(UpdateProductResponse::from);
    }

    private Mono<Void> updateProductStatus(Product product, ProductStatus status) {
        return productRepository.save(product.withStatus(status))
            .then();
    }

    public Mono<ProductExistsResponse> existsProduct(Long id) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("product.notfound")))
            .map(ProductExistsResponse::of);
    }

}
