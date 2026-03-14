package site.ng_archive.ecom_product.global.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String code) {
        super(code);
    }

}
