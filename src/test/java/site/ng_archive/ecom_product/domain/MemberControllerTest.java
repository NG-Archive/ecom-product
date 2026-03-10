package site.ng_archive.ecom_product.domain;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import site.ng_archive.ecom_product.config.AcceptedTest;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class ProductControllerTest extends AcceptedTest {

    @Test
    void readProduct() {
        given()
            .contentType(ContentType.JSON)
            .consumeWith(document(
                    resource(
                            builder()
                                    .tag("Product")             // Swagger에서 그룹화될 이름
                                    .summary("회원 상세 조회")    // API 제목
                                    .description("회원 ID를 사용하여 상세 정보를 조회합니다.") // 상세 설명
                                    .pathParameters(
                                            parameterWithName("id").description("회원 아이디")
                                    )
                                    .build()
                    )

            ))
            .get("product/{id}", "1")
        .then()
            .statusCode(200)
        ;
    }

}
