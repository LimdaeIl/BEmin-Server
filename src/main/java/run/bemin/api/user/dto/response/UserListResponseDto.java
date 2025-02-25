package run.bemin.api.user.dto.response;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class UserListResponseDto {
  private List<UserResponseDto> content;
  private PageInfo page;

  public UserListResponseDto(Page<UserResponseDto> userPage) {
    this.content = userPage.getContent();
    this.page = new PageInfo(
        userPage.getSize(),
        userPage.getNumber(),
        userPage.getTotalElements(),
        userPage.getTotalPages()
    );
  }

  @Getter
  public static class PageInfo {
    private final int size;
    private final int number;
    private final long totalElements;
    private final int totalPages;

    public PageInfo(int size, int number, long totalElements, int totalPages) {
      this.size = size;
      this.number = number;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
    }
  }
}
