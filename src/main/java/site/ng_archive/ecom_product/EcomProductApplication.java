package site.ng_archive.ecom_product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import site.ng_archive.ecom_common.config.EnableEcomCommon;

@EnableEcomCommon
@SpringBootApplication
public class EcomProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomProductApplication.class, args);
	}

}
