package run.bemin.api.review.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewProducer {

  private final RabbitTemplate rabbitTemplate;

  @Autowired
  public ReviewProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void sendReviewEvent(ReviewEvent event) {
    rabbitTemplate.convertAndSend("review-exchange", "review-routing-key", event);
  }
}