package site.ng_archive.ecom_product.domain;

import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import site.ng_archive.ecom_product.config.AcceptedTest;
import site.ng_archive.ecom_product.domain.dto.ProductResponse;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

class ProductControllerTest extends AcceptedTest {

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
            .log().all();
    }

}
