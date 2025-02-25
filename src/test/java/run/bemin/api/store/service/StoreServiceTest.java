package run.bemin.api.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(StoreServiceTest.class);

  @Mock
  private StoreRepository storeRepository;

  @InjectMocks
  private StoreService storeService;

  @Test
  @DisplayName("[성공|사용자] queryDSL 동적 검색 조회하기")
  void testSearchStoresMultipleResultsWithSlf4j() {
    // given: 검색 조건과 페이징 정보 설정
    String categoryName = "한식";
    String storeName = "한식당";
    Pageable pageable = PageRequest.of(0, 10);

    // 여러 개의 Store 객체 생성
    Store store1 = Store.create("맛있는 한식당", "010-1234-5678", 10000, true, null, null);
    Store store2 = Store.create("신선한 한식당", "010-9876-5432", 20000, true, null, null);
    Store store3 = Store.create("정갈한 한식당", "010-1111-2222", 15000, true, null, null);

    List<Store> storeList = List.of(store1, store2, store3);
    Page<Store> storePage = new PageImpl<>(storeList, pageable, storeList.size());

    when(storeRepository.searchStores(eq(categoryName), eq(storeName), any(Pageable.class)))
        .thenReturn(storePage);

    // when: 서비스 메서드 호출
    Page<Store> result = storeService.searchStores(categoryName, storeName, pageable);

    // then: 결과 검증
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(3, result.getTotalElements());

    // SLF4J 로그로 결과 출력
    logger.info("검색 결과:");
    result.getContent().forEach(s ->
        logger.info("가게 이름: {}, 전화번호: {}, 최소 가격: {}", s.getName(), s.getPhone(), s.getMinimumPrice())
    );

    // repository 의 searchStores 메서드가 한 번 호출되었는지 확인
    verify(storeRepository, times(1)).searchStores(eq(categoryName), eq(storeName), any(Pageable.class));
  }
}