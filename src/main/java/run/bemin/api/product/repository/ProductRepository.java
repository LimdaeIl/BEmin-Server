package run.bemin.api.product.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import run.bemin.api.product.dto.ProductSearchDto;
import run.bemin.api.product.entity.Product;
import run.bemin.api.store.entity.Store;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  @Query("""
    SELECT new run.bemin.api.product.dto.ProductSearchDto(
       p.price, p.title, p.comment, p.imageUrl, p.isHidden
    )
    FROM Product p
    WHERE p.store.id = :storeId AND p.activated = true
    """)
  Page<ProductSearchDto> findByStoreId(UUID store_id, Pageable pageable);
}
