package site.ng_archive.ecom_product.config;

import io.restassured.RestAssured;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;
import com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper;



@Import(RestDocsConfig.class)
@ExtendWith({RestDocumentationExtension.class})
@AutoConfigureRestDocs
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AcceptedTest {

    @Autowired
    protected Consumer<EntityExchangeResult<byte[]>> restDocs;

    @Autowired
    protected RestDocsConfig restDocsConfig;

    @Autowired
    protected WebTestClient webTestClient;

    @LocalServerPort
    protected Integer port = 0;

    @BeforeEach
    void setUp(ApplicationContext applicationContext, RestDocumentationContextProvider provider) {

        this.webTestClient = this.webTestClient.mutate()
                .filter(WebTestClientRestDocumentation.documentationConfiguration(provider))
                .build();

        RestAssured.port = port;
        RestAssuredWebTestClient.webTestClient(webTestClient);
    }

    protected Consumer<EntityExchangeResult<byte[]>> document(Snippet... snippets) {
        return WebTestClientRestDocumentationWrapper.document(
                "{class-name}/{method-name}",
                restDocsConfig.getRequestPreprocessor(),
                restDocsConfig.getResponsePreprocessor(),
                snippets
        );
    }
}
