package run.bemin.api.review.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewConsumer {

  private final StoreRepository storeRepository;

  @Transactional(rollbackFor = Exception.class)   // 예외를 상세하게 보기
  @RabbitListener(queues = "review-queue")
  public void receiveReviewEvent(ReviewEvent event) {
    try {
      // store 찾기
      Store store = storeRepository.findById(event.getStoreId()).orElseThrow();

      // store 평점 갱신하기
      store.updateRating((float) event.getAverageRating());

      // store 데이터베이스에 저장
      storeRepository.save(store);
    } catch (Exception e) {
      // 예외 처리 로직
      log.info("RabbitMQ 에서 일어난 Error : {}", e.getMessage());
    }
  }
}
