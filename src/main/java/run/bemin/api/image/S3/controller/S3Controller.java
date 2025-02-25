package run.bemin.api.image.S3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.image.S3.service.S3Service;
import run.bemin.api.image.util.UrlUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
@Slf4j
@Tag(name = "S3", description = "S3TestController")
public class S3Controller {

  private final S3Service s3Service;
  private final UrlUtil util;

  /**
   * 최적화 테스트 메소드
   *
   * @param url -> 이미지 url
   */
  @PostMapping
  @Operation(summary = "S3에 이미지 업로드하기", description = "url 활용해 최적화하고 S3에 이미지를 업로드 하는 Test 컨트롤러 입니다.")
  public ResponseEntity<String> uploadFile(@RequestParam("url") String url) {
    String uploadImg = util.getFoodImgAndUploadImg(url);
    return ResponseEntity.ok(uploadImg);
  }
}
