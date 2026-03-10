package site.ng_archive.ecom_product.domain;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @RequestMapping("/product/{id}")
    public void readProduct(@PathVariable Long id) {

    }
}
