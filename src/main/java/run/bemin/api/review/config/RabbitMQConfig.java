package run.bemin.api.review.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  // Direct 방식으로 목적지에 전달하기 때문에 directExchange 사용
  @Bean
  public Exchange reviewExchange() {
    return ExchangeBuilder.directExchange("review-exchange").build();
  }

  // exchange 를 통해 전달할 메시지 큐 이름이 review-queue 인 큐에 전달합니다.
  @Bean
  public Queue reviewQueue() {
    return QueueBuilder.durable("review-queue").build();
  }

  // exchange 와 queue 를 'review-routing-key'를 통해 연결
  @Bean
  public Binding reviewBinding() {
    return BindingBuilder.bind(reviewQueue()).to(reviewExchange()).with("review-routing-key").noargs();
  }

  // 메시지 보내고 받기
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
    return rabbitTemplate;
  }

  // 기본적으로 SimpleMessageConverter 사용되지만, Jackson2JsonMessageConverter 사용해서 객체로 메시지 보내기
  // 메시지를 JSON 형식으로 변환
  @Bean
  public MessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
