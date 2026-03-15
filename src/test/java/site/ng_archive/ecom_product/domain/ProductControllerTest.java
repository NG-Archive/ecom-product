package site.ng_archive.ecom_product.domain;

import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import site.ng_archive.ecom_product.config.AcceptedTest;
import site.ng_archive.ecom_product.domain.dto.ProductCommand;
import site.ng_archive.ecom_product.domain.dto.ProductRequest;
import site.ng_archive.ecom_product.domain.dto.ProductResponse;
import site.ng_archive.ecom_product.domain.dto.UpdateProductRequest;
import site.ng_archive.ecom_product.global.error.ErrorResponse;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

class ProductControllerTest extends AcceptedTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void 상품목록조회() {
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
                        field(ProductResponse.class, "[].id", "상품 ID"),
                        field(ProductResponse.class, "[].name", "상품 이름"),
                        field(ProductResponse.class, "[].price", "상품 가격")
                    )
                    .responseSchema(Schema.schema("ProductList"))
            ))
            .get("/products")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
            .body("[0].id", notNullValue())
            .body("[0].name", notNullValue())
            .body("[0].price", greaterThanOrEqualTo(0))
            .log().all();
    }

    @Test
    void 상품단건조회() {
        Long id = createMember("테스트 상품", 1000L);

        ProductResponse response = given()
            .pathParam("id", id)
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
                        field(ProductResponse.class, "price", "상품 가격")
                    )
                    .responseSchema(Schema.schema("ProductDetail"))
            ))
            .get("/product/{id}")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ProductResponse.class);

        Assertions.assertThat(id).isEqualTo(response.id());
    }

    @Test
    void 상품단건조회_미존재상품오류() {
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
        Assertions.assertThat(response.message()).isEqualTo("상품이 존재하지 않습니다.");
    }

    @Test
    void 상품등록() {
        ProductRequest request = new ProductRequest("테스트 상품", 1000L);

        ProductResponse response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Product")
                    .summary("상품 등록")
                    .description("상품 정보를 입력해 상품을 등록합니다.")
                    .requestFields(
                        field(ProductRequest.class, "name", "상품 이름"),
                        field(ProductRequest.class, "price", "상품 가격")
                    )
                    .requestSchema(Schema.schema("ProductCreateRequest"))
                    .responseFields(
                        field(ProductResponse.class, "id", "상품 ID"),
                        field(ProductResponse.class, "name", "상품 이름"),
                        field(ProductResponse.class, "price", "상품 가격")
                    )
                    .responseSchema(Schema.schema("ProductCreatedResponse"))
            ))
            .post("/product")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ProductResponse.class);

        Product product = productRepository.findById(response.id()).block();
        Assertions.assertThat(product.id()).isEqualTo(response.id());
    }

    @Test
    void 상품수정() {
        Long id = createMember("테스트 상품", 1000L);
        UpdateProductRequest request = new UpdateProductRequest("테스트 상품 수정", 2000L);

        ProductResponse response = given()
            .contentType(ContentType.JSON)
            .pathParam("id", id)
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
                        field(UpdateProductRequest.class, "price", "상품 가격")
                    )
                    .requestSchema(Schema.schema("ProductUpdateRequest"))
                    .responseFields(
                        field(ProductResponse.class, "id", "상품 ID"),
                        field(ProductResponse.class, "name", "상품 이름"),
                        field(ProductResponse.class, "price", "상품 가격")
                    )
                    .responseSchema(Schema.schema("ProductUpdatedResponse"))
            ))
            .put("/product/{id}")
            .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .log().all()
            .extract().body().as(ProductResponse.class);

        Assertions.assertThat(response.id()).isEqualTo(id);
        Assertions.assertThat(response.name()).isEqualTo(request.name());
        Assertions.assertThat(response.price()).isEqualTo(request.price());
    }

    @Test
    void 상품수정_미존재상품오류() {
        UpdateProductRequest request = new UpdateProductRequest("테스트 상품 수정", 2000L);

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
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
                        field(UpdateProductRequest.class, "price", "상품 가격")
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
        Assertions.assertThat(response.message()).isEqualTo("상품이 존재하지 않습니다.");
    }

    private Long createMember(String name, Long price) {
        ProductCommand command = new ProductCommand(name, price);
        return productService.createProduct(command).block().id();
    }

}
