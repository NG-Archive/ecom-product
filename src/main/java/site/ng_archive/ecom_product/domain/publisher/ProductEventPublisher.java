package site.ng_archive.ecom_product.domain.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_product.domain.Product;

@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "product-price-changed";

    public Mono<Void> publishChangeEvent(Product product) {
        return kafkaTemplate.send(TOPIC, product.id().toString(), product)
            .onErrorResume(e -> Mono.empty())
            .then();
    }

}
