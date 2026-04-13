package site.ng_archive.ecom_product.domain.requester;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.webclient.WebClientErrorHandler;
import site.ng_archive.ecom_product.domain.dto.CreateStockRequest;
import site.ng_archive.ecom_product.domain.dto.CreateStockResponse;
import site.ng_archive.ecom_product.domain.dto.StockResponse;

@Component
public class StockRequester {

    private final WebClient webClient;

    public StockRequester(@Qualifier("stockClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<CreateStockResponse> createStock(Long productId, Long quantity) {
        CreateStockRequest request = new CreateStockRequest(productId, quantity);

        return webClient.post()
            .uri("/{productId}/stock", productId)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, WebClientErrorHandler::handle)
            .bodyToMono(CreateStockResponse.class);
    }

    public Mono<StockResponse> getStock(Long productId) {
        return webClient.get()
            .uri("/{productId}/stock", productId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, WebClientErrorHandler::handle)
            .bodyToMono(StockResponse.class);
    }

}
