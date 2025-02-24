package run.bemin.api.order.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PagesResponse<T> {
  private List<T> data; // 데이터 리스트 (주문 목록)
  private int pageNumber; // 현재 페이지 번호
  private int pageSize; // 페이지 크기
  private int totalPages; // 전체 페이지 수
  private Long totalElements; // 전체 데이터 수
}
