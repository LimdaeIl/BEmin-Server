package run.bemin.api.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import run.bemin.api.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {

  List<Store> findByOwner_UserEmail(String userEmail);

  Page<Store> findAllByIsDeletedAndNameContainingIgnoreCase(Boolean isDeleted, String name, Pageable pageable);

  Page<Store> findAllByIsDeleted(Boolean isDeleted, Pageable pageable);

  // 삭제 여부 상관 없이 검색
  Page<Store> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

  @Query("select s from p_store s left join fetch s.reviews where s.id = :storeId")
  Optional<Store> findByIdWithReviews(@Param("storeId") UUID storeId);

}
