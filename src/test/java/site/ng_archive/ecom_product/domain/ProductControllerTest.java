package site.ng_archive.ecom_product.domain;

import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.auth.Role;
import site.ng_archive.ecom_common.auth.UserContext;
import site.ng_archive.ecom_common.auth.token.TokenUtil;
import site.ng_archive.ecom_common.error.ErrorResponse;
import site.ng_archive.ecom_product.config.AcceptedTest;
import site.ng_archive.ecom_product.domain.dto.*;
import site.ng_archive.ecom_product.domain.requester.StockRequester;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

class ProductControllerTest extends AcceptedTest {

    @MockitoBean
    private StockRequester stockRequester;

    @Autowired
    private ProductRepository productRepository;

    private static final Long TEST_PRODUCT_ID = 1L;
    private static final String TEST_PRODUCT_NAME = "테스트 상품";
    private static final Long TEST_PRODUCT_PRICE = 1000L;
    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_QUANTITY = 10L;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
    }

    @Test
    void 상품목록조회_성공() {
        Product activeProduct = createProduct("활성상품", TEST_PRODUCT_PRICE, ProductStatus.ACTIVE, TEST_MEMBER_ID);
        Product inactiveProduct = createProduct("비활성상품", TEST_PRODUCT_PRICE, ProductStatus.INACTIVE, TEST_MEMBER_ID);

        given()
            .queryParam("offset", 0)
            .queryParam("size", 10)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 목록 조회")
                    .description("상품 목록을 페이징으로 조회합니다.")
                        .queryParameters(
                            parameterWithName("offset").description("페이지 오프셋").type(SimpleType.INTEGER).defaultValue(0),
                            parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER).defaultValue(10)
                        )
                    .responseFields(
                        field(ProductListResponse.class, "[].id", "상품 ID"),
                        field(ProductListResponse.class, "[].name", "상품 이름"),
                        field(ProductListResponse.class, "[].price", "상품 가격")
                    )
                    .responseSchema(Schema.schema("ProductListResponse"))
            ))
            .get("/products")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .body("size()", is(1))
            .body("id", not(hasItem(inactiveProduct.id())))
            .body("name", hasItem(activeProduct.name()))
            .body("name", not(hasItem(inactiveProduct.name())))
            .body("price", notNullValue())
            .log().all();
    }

    @Test
    void 상품단건조회_성공() {
        Product createProduct = createProduct(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, ProductStatus.ACTIVE, TEST_MEMBER_ID);
        mockGetStock(createProduct.id(), TEST_QUANTITY);

        ProductResponse response = given()
            .pathParam("id", createProduct.id())
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 상세 조회")
                    .description("상품 ID를 사용하여 상세 정보를 조회합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        field(ProductResponse.class, "id", "상품 ID"),
                        field(ProductResponse.class, "name", "상품 이름"),
                        field(ProductResponse.class, "price", "상품 가격"),
                        field(ProductResponse.class, "quantity", "상품 재고"),
                        field(ProductResponse.class, "memberId", "회원 ID")
                    )
                    .responseSchema(Schema.schema("ProductDetail"))
            ))
            .get("/product/{id}")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ProductResponse.class);

        Assertions.assertThat(createProduct.id()).isEqualTo(response.id());
        Assertions.assertThat(createProduct.name()).isEqualTo(response.name());
        Assertions.assertThat(createProduct.price()).isEqualTo(response.price());
        Assertions.assertThat(TEST_QUANTITY).isEqualTo(response.quantity());
    }

    @Test
    void 상품단건조회_실패_미존재상품오류() {
        ErrorResponse response = given()
            .pathParam("id", -1L)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 상세 조회")
                    .description("상품 ID를 사용하여 상세 정보를 조회합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .get("/product/{id}")
            .then()
            .status(HttpStatus.NOT_FOUND)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("product.notfound");
        Assertions.assertThat(response.message()).isEqualTo("상품 데이터가 존재하지 않습니다.");
    }

    @Test
    void 상품단건조회_실패_재고정보없음() {
        Product createProduct = createProduct(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, ProductStatus.FAILED, TEST_MEMBER_ID);
        mockGetStockEmpty();

        ErrorResponse response = given()
            .pathParam("id", createProduct.id())
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 상세 조회")
                    .description("상품 ID를 사용하여 상세 정보를 조회합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .get("/product/{id}")
            .then()
            .status(HttpStatus.NOT_FOUND)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.notfound");
        Assertions.assertThat(response.message()).isEqualTo("재고 데이터가 존재하지 않습니다.");
    }

    @Test
    void 상품등록_성공() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.SELLER);
        CreateProductRequest request = new CreateProductRequest(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, TEST_QUANTITY);

        mockCreateStock(TEST_PRODUCT_ID, TEST_QUANTITY);

        ProductResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 등록")
                    .description("상품 정보를 입력해 상품을 등록합니다.")
                    .requestFields(
                        field(CreateProductRequest.class, "name", "상품 이름"),
                        field(CreateProductRequest.class, "price", "상품 가격"),
                        field(CreateProductRequest.class, "quantity", "상품 재고")
                    )
                    .requestSchema(Schema.schema("ProductCreateRequest"))
                    .responseFields(
                        field(ProductResponse.class, "id", "상품 ID"),
                        field(ProductResponse.class, "name", "상품 이름"),
                        field(ProductResponse.class, "price", "상품 가격"),
                        field(ProductResponse.class, "quantity", "상품 재고"),
                        field(ProductResponse.class, "memberId", "회원 ID")
                    )
                    .responseSchema(Schema.schema("ProductCreatedResponse"))
            ))
            .post("/product")
            .then()
            .status(HttpStatus.CREATED)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ProductResponse.class);

        Product product = productRepository.findById(response.id()).block();
        Assertions.assertThat(product.id()).isEqualTo(response.id());
        Assertions.assertThat(product.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void 상품등록_실패_판매자권한아님() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.USER);
        CreateProductRequest request = new CreateProductRequest(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, TEST_QUANTITY);

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 등록")
                    .description("상품 정보를 입력해 상품을 등록합니다.")
                    .requestFields(
                        field(CreateProductRequest.class, "name", "상품 이름"),
                        field(CreateProductRequest.class, "price", "상품 가격"),
                        field(CreateProductRequest.class, "quantity", "상품 재고")
                    )
                    .requestSchema(Schema.schema("ProductCreateRequest"))
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .post("/product")
            .then()
            .status(HttpStatus.FORBIDDEN)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("auth.forbidden");
        Assertions.assertThat(response.message()).isEqualTo("권한이 필요합니다.");
    }

    @Test
    void 상품등록_실패_재고등록정보없음() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.SELLER);
        CreateProductRequest request = new CreateProductRequest(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, TEST_QUANTITY);

        mockCreateStockEmpty();

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 등록")
                    .description("상품 정보를 입력해 상품을 등록합니다.")
                    .requestFields(
                        field(CreateProductRequest.class, "name", "상품 이름"),
                        field(CreateProductRequest.class, "price", "상품 가격"),
                        field(CreateProductRequest.class, "quantity", "상품 재고")
                    )
                    .requestSchema(Schema.schema("ProductCreateRequest"))
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .post("/product")
            .then()
            .status(HttpStatus.NOT_FOUND)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.notfound");
        Assertions.assertThat(response.message()).isEqualTo("재고 데이터가 존재하지 않습니다.");

        Product product = productRepository.findAll().blockFirst();
        Assertions.assertThat(product.status()).isEqualTo(ProductStatus.FAILED);
    }

    @Test
    void 상품등록_실패_재고등록오류() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.SELLER);
        CreateProductRequest request = new CreateProductRequest(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, TEST_QUANTITY);

        mockCreateStockError();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 등록")
                    .description("상품 정보를 입력해 상품을 등록합니다.")
                    .requestFields(
                        field(CreateProductRequest.class, "name", "상품 이름"),
                        field(CreateProductRequest.class, "price", "상품 가격"),
                        field(CreateProductRequest.class, "quantity", "상품 재고")
                    )
                    .requestSchema(Schema.schema("ProductCreateRequest"))
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .post("/product")
            .then()
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .log().all()
            .extract().body();

        Product product = productRepository.findAll().blockFirst();
        Assertions.assertThat(product.status()).isEqualTo(ProductStatus.FAILED);
    }

    @Test
    void 상품수정_성공() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.SELLER);
        Product createdProduct = createProduct(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, ProductStatus.ACTIVE, TEST_MEMBER_ID);
        UpdateProductRequest request = new UpdateProductRequest("테스트 상품 수정", 2000L, ProductStatus.INACTIVE);

        UpdateProductResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .pathParam("id", createdProduct.id())
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 수정")
                    .description("수정할 상품 정보를 입력해 상품을 수정합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .requestFields(
                        field(UpdateProductRequest.class, "name", "상품 이름"),
                        field(UpdateProductRequest.class, "price", "상품 가격"),
                        field(UpdateProductRequest.class, "status", "상품 상태")
                    )
                    .requestSchema(Schema.schema("ProductUpdateRequest"))
                    .responseFields(
                        field(UpdateProductResponse.class, "id", "상품 ID"),
                        field(UpdateProductResponse.class, "name", "상품 이름"),
                        field(UpdateProductResponse.class, "price", "상품 가격"),
                        field(UpdateProductResponse.class, "status", "상품 상태"),
                        field(UpdateProductResponse.class, "statusName", "상품 상태 설명")
                    )
                    .responseSchema(Schema.schema("ProductUpdatedResponse"))
            ))
            .put("/product/{id}")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(UpdateProductResponse.class);

        Assertions.assertThat(response.id()).isEqualTo(createdProduct.id());
        Assertions.assertThat(response.name()).isEqualTo(request.name());
        Assertions.assertThat(response.price()).isEqualTo(request.price());
        Assertions.assertThat(response.status()).isEqualTo(request.status().name());
    }

    @Test
    void 상품수정_실패_판매자권한아님() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.USER);
        UpdateProductRequest request = new UpdateProductRequest("테스트 상품 수정", 2000L, ProductStatus.INACTIVE);

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .pathParam("id", TEST_PRODUCT_ID)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 수정")
                    .description("수정할 상품 정보를 입력해 상품을 수정합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .requestFields(
                        field(UpdateProductRequest.class, "name", "상품 이름"),
                        field(UpdateProductRequest.class, "price", "상품 가격"),
                        field(UpdateProductRequest.class, "status", "상품 상태")
                    )
                    .requestSchema(Schema.schema("ProductUpdateRequest"))
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .put("/product/{id}")
            .then()
            .status(HttpStatus.FORBIDDEN)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("auth.forbidden");
        Assertions.assertThat(response.message()).isEqualTo("권한이 필요합니다.");
    }

    @Test
    void 상품수정_실패_미존재상품오류() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.SELLER);
        UpdateProductRequest request = new UpdateProductRequest("테스트 상품 수정", 2000L, ProductStatus.INACTIVE);

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .pathParam("id", -1L)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 수정")
                    .description("수정할 상품 정보를 입력해 상품을 수정합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .requestFields(
                        field(UpdateProductRequest.class, "name", "상품 이름"),
                        field(UpdateProductRequest.class, "price", "상품 가격"),
                        field(UpdateProductRequest.class, "status", "상품 상태")
                    )
                    .requestSchema(Schema.schema("ProductUpdateRequest"))
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .put("/product/{id}")
            .then()
            .status(HttpStatus.NOT_FOUND)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("product.notfound");
        Assertions.assertThat(response.message()).isEqualTo("상품 데이터가 존재하지 않습니다.");
    }

    @Test
    void 상품수정_실패_타인상품수정() {
        String token = createTestJwtToken(TEST_MEMBER_ID, Role.ROLES.SELLER);
        Product createdProduct = createProduct(TEST_PRODUCT_NAME, TEST_PRODUCT_PRICE, ProductStatus.ACTIVE, 20L);
        UpdateProductRequest request = new UpdateProductRequest("테스트 상품 수정", 2000L, ProductStatus.INACTIVE);

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .pathParam("id", createdProduct.id())
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 수정")
                    .description("수정할 상품 정보를 입력해 상품을 수정합니다.")
                    .pathParameters(
                        parameterWithName("id").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .requestFields(
                        field(UpdateProductRequest.class, "name", "상품 이름"),
                        field(UpdateProductRequest.class, "price", "상품 가격"),
                        field(UpdateProductRequest.class, "status", "상품 상태")
                    )
                    .requestSchema(Schema.schema("ProductUpdateRequest"))
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
                    .responseSchema(Schema.schema("ErrorResponse"))
            ))
            .put("/product/{id}")
            .then()
            .status(HttpStatus.FORBIDDEN)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("product.forbidden");
        Assertions.assertThat(response.message()).isEqualTo("해당 상품에 대한 접근 권한이 없습니다.");
    }

    private String createTestJwtToken(Long memberId, String role) {
        return TokenUtil.getSign(UserContext.of(memberId, role));
    }

    private Product createProduct(String name, Long price, ProductStatus status, Long memberId) {
        Product product = new Product(null, name, price, status, memberId);
        return productRepository.save(product).block();
    }

    private void mockGetStock(Long productId, Long quantity) {
        BDDMockito.given(stockRequester.getStock(any()))
            .willReturn(Mono.just(new StockResponse(productId, quantity)));
    }

    private void mockGetStockEmpty() {
        BDDMockito.given(stockRequester.getStock(any()))
            .willReturn(Mono.empty());
    }

    private void mockCreateStock(Long productId, Long quantity) {
        BDDMockito.given(stockRequester.createStock(any(), any()))
            .willReturn(Mono.just(new CreateStockResponse(2L, productId, quantity)));
    }

    private void mockCreateStockEmpty() {
        BDDMockito.given(stockRequester.createStock(any(), any()))
            .willReturn(Mono.empty());
    }

    private void mockCreateStockError() {
        BDDMockito.given(stockRequester.createStock(any(), any()))
            .willReturn(Mono.error(new RuntimeException("재고 등록 오류")));
    }

}
