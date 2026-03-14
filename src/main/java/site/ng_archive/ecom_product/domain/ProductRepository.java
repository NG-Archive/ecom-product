package site.ng_archive.ecom_product.domain;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    @Query("""
            SELECT p.*
            FROM product p 
            JOIN (
                SELECT id FROM product ORDER BY id DESC LIMIT :size OFFSET :offset) temp
            ON p.id = temp.id
            """)
    Flux<Product> findAllBy(long offset, int size);

}
