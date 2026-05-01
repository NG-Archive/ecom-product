package site.ng_archive.ecom_product.domain;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import site.ng_archive.ecom_common.auth.UserContext;
import site.ng_archive.ecom_common.auth.token.TokenUtil;
import site.ng_archive.ecom_product.config.AcceptedTest;
import site.ng_archive.ecom_product.domain.dto.UpdateProductCommand;
import site.ng_archive.ecom_product.domain.dto.UpdateProductResponse;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@EmbeddedKafka(
    partitions = 1,
    topics = {"product-price-changed"}
)
class ProductKafkaTest extends AcceptedTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Consumer<String, Product> testConsumer;

    private static final String TEST_PRODUCT_NAME = "테스트 상품";
    private static final Long TEST_PRODUCT_PRICE = 1000L;
    private static final Long TEST_MEMBER_ID = 1L;

    private static final String TOPIC = "product-price-changed";

    @BeforeEach
    void setUp() {
        // Consumer 설정
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            "test-group-" + UUID.randomUUID(),
            "true",
            embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // Consumer 인스턴스 생성
        DefaultKafkaConsumerFactory<String, Product> consumerFactory = new DefaultKafkaConsumerFactory<>(
            consumerProps,
            new StringDeserializer(),
            new JsonDeserializer<>(Product.class)
        );
        testConsumer = consumerFactory.createConsumer();
        // 구독 및 파티션 할당
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(testConsumer, TOPIC);
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.wakeup();
            testConsumer.close(Duration.ofSeconds(1));
        }
    }

    @Test
    void 상품수정_가격변경_이벤트발행() {
        Product createdProduct = createProduct(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, ProductStatus.ACTIVE, TEST_MEMBER_ID);
        Long updatePrice = 20000L;
        UpdateProductCommand command = new UpdateProductCommand(createdProduct.id(), "테스트 상품 수정", updatePrice, ProductStatus.INACTIVE, TEST_MEMBER_ID);

        UpdateProductResponse response = productService.updateProduct(command).block();

        ConsumerRecord<String, Product> record = KafkaTestUtils.getSingleRecord(testConsumer, TOPIC, Duration.ofSeconds(2));
        Assertions.assertThat(record.key()).isEqualTo(response.id().toString());
        Assertions.assertThat(record.value().price()).isEqualTo(updatePrice);
    }

    @Test
    void 상품수정_가격동일_이벤트미발행() {
        Product createdProduct = createProduct(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, ProductStatus.ACTIVE, TEST_MEMBER_ID);
        UpdateProductCommand command = new UpdateProductCommand(createdProduct.id(), "테스트 상품 수정", TEST_PRODUCT_PRICE, ProductStatus.INACTIVE, TEST_MEMBER_ID);

        productService.updateProduct(command).block();

        ConsumerRecords<String, Product> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(2));
        Assertions.assertThat(records.count()).isZero();
    }

    private String createTestJwtToken(Long memberId, String role) {
        return TokenUtil.getSign(UserContext.of(memberId, role));
    }

    private Product createProduct(String name, Long price, ProductStatus status, Long memberId) {
        Product product = new Product(null, name, price, status, memberId);
        return productRepository.save(product).block();
    }

}
