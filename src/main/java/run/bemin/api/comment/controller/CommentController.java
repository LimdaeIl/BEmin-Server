package run.bemin.api.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.comment.dto.CommentDto;
import run.bemin.api.comment.dto.CommentRequestDto;
import run.bemin.api.comment.dto.CommentResponseDto;
import run.bemin.api.comment.service.ChatGPTService;
import run.bemin.api.comment.service.CommentService;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.product.dto.MessageResponseDto;

@RequestMapping("/api/comment")
@RestController
@RequiredArgsConstructor
@Tag(name = "상품 설명", description = "CommentController")
public class CommentController {
  private final ChatGPTService chatGPTService;
  private final CommentService commentService;

  /**
   * 입력받은 User prompt로 상품 설명 문구를 생성합니다.
   *
   * @param commentRequestDto -> userPrompt
   * @return CommentResponseDto -> content
   */
  @GetMapping("")
  @Operation(summary = "상품 설명 생성하기", description = "사용자 Prompt를 활용해 상품 상세설명 문구를 생성합니다.")
  public ApiResponse<CommentResponseDto> getNewComment(@RequestBody CommentRequestDto commentRequestDto) {
    String comment = chatGPTService.getChatGPTResponse(commentRequestDto.userPrompt());
    return ApiResponse.from(HttpStatus.OK, "성공", new CommentResponseDto(comment));
  }

  /**
   * product Id 기준으로 생성한 상품 설명을 저장합니다.
   *
   * @param productId -> 상품 Id
   * @param commentDto -> content
   * @return MessageResponseDto -> 성공 메세지를 담고 있습니다.
   */
  @GetMapping("/{productId}")
  @Operation(summary = "상품 설명 저장하기", description = "product Id 기준으로 생성한 상품 설명을 저장합니다.")
  public ApiResponse<MessageResponseDto> saveComment(@PathVariable String productId,
                                                     @RequestBody CommentDto commentDto)
  {
    commentService.saveComment(UUID.fromString(productId),commentDto.content());
    return ApiResponse.from(HttpStatus.OK, "성공", new MessageResponseDto("Comment가 저장되었습니다."));
  }

}
