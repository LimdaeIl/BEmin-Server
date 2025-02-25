package run.bemin.api.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.comment.dto.CommentDto;
import run.bemin.api.comment.dto.CommentListDto;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.product.dto.MessageResponseDto;
import run.bemin.api.product.dto.ProductRequestDto;
import run.bemin.api.product.dto.ProductSearchDto;
import run.bemin.api.product.dto.UpdateProductDetailDto;
import run.bemin.api.product.exception.UnauthorizedStoreAccessException;
import run.bemin.api.product.service.ProductService;
import run.bemin.api.product.validator.ProductValidator;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.exception.StoreNotFoundException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Tag(name = "상품", description = "HomeController")
public class ProductController {
  private final ProductService productService;
  private final ProductValidator validator;

  /**
   * store id 기준으로 상품을 추가합니다.
   *
   * @param storeId           -> 가게 id
   * @param productRequestDto -> price,title,imageUrl,comment
   * @return MessageResponseDto -> 성공 메세지를 담고 있습니다.
   * @throws UnauthorizedStoreAccessException () -> store에 대한 접근권한을 가지지 않았을떄 발생합니다.
   * @throws StoreNotFoundException           () -> store Id가 유효하지 않을때 발생합니다.
   */
  @PostMapping("/{storeId}/products")
  @Operation(summary = "상품 추가하기", description = "가게 주인인지 확인한 후, store id 기준으로 가게에 상품을 추가합니다.")
  public ApiResponse<MessageResponseDto> addProduct(@PathVariable String storeId,
                                                    @RequestBody ProductRequestDto productRequestDto) {
    UUID storeUUID = UUID.fromString(storeId);
    Store store = validator.isStoreOwner(storeUUID);

    productService.createProduct(
        store,
        productRequestDto.price(),
        productRequestDto.title(),
        productRequestDto.comment(),
        productRequestDto.imageUrl()
    );
    return ApiResponse.from(HttpStatus.CREATED, "성공", new MessageResponseDto("상품 추가가 완료되었습니다."));
  }

  /**
   * store id 기준으로 상품을 조회합니다.
   *
   * @param storeId   -> 가게 id
   * @param page,size -> pagination
   * @return Page<ProductSearchDto> -> id, price, title, comment, imageUrl, is_hidden
   */
  @GetMapping("/{storeId}/products")
  @Operation(summary = "상품 조회하기", description = "가게 주인인지 확인한 후, store id 기준으로 가게에 상품을 조회합니다.")
  public ApiResponse<Page<ProductSearchDto>> getProductsByStoreId(@PathVariable String storeId,
                                                                  @RequestParam("page") int page,
                                                                  @RequestParam("size") int size) {
    Page<ProductSearchDto> products = productService.getProducts(storeId, page, size);
    return ApiResponse.from(HttpStatus.OK, "성공", products);
  }

  /**
   * store id와 product id 기준으로 상품 상세를 수정합니다.
   *
   * @param storeId    -> 가게 id
   * @param productId  -> 상품 id
   * @param requestDto -> price(Nullable), title(Nullable), imageUrl(Nullable), isHidden(Nullable)
   * @return MessageResponseDto -> 성공 메세지를 담고 있습니다.
   * @throws UnauthorizedStoreAccessException () -> store에 대한 접근권한을 가지지 않았을떄 발생합니다.
   * @throws StoreNotFoundException           () -> store Id가 유효하지 않을때 발생합니다.
   */
  @PostMapping("/{storeId}/{productId}")
  @Operation(summary = "상품 상세 수정하기", description = "상품의 가격, 설명, 이미지, 숨기처리 등을 설정합니다.")
  public ApiResponse<MessageResponseDto> updateProductDetails(@PathVariable String storeId,
                                                              @PathVariable String productId,
                                                              @RequestBody UpdateProductDetailDto requestDto) {
    UUID storeUUID = UUID.fromString(storeId);
    validator.isStoreOwner(storeUUID);

    productService.updateProductDetails(productId, requestDto);
    return ApiResponse.from(HttpStatus.OK, "성공", new MessageResponseDto("상품 상세 설정 변경이 완료되었습니다."));
  }

  /**
   * store id와 product id 기준으로 상품을 삭제합니다.
   *
   * @param storeId   -> 가게 id
   * @param productId -> 상품 id
   * @return MessageResponseDto -> 성공 메세지를 담고 있습니다.
   * @throws UnauthorizedStoreAccessException () -> store에 대한 접근권한을 가지지 않았을떄 발생합니다.
   * @throws StoreNotFoundException           () -> store Id가 유효하지 않을때 발생합니다.
   */
  @DeleteMapping("/{storeId}/{productId}")
  @Operation(summary = "상품 삭제하기", description = "가게 주인인지 확인한 후, 상품을 삭제합니다.")
  public ApiResponse<MessageResponseDto> deleteProduct(@PathVariable String storeId,
                                                       @PathVariable String productId) {
    UUID storeUUID = UUID.fromString(storeId);
    validator.isStoreOwner(storeUUID);
    LocalDateTime time = LocalDateTime.now();

    productService.deleteProduct(productId, time);
    return ApiResponse.from(HttpStatus.OK, "성공", new MessageResponseDto("상품 삭제가 완료되었습니다."));
  }


  /**
   * store id와 product id 기준으로 상품 설명을 수정합니다.
   *
   * @param storeId    -> 가게 id
   * @param productId  -> 상품 id
   * @param commentDto -> content
   * @return MessageResponseDto -> 성공 메세지를 담고 있습니다.
   * @throws UnauthorizedStoreAccessException () -> store에 대한 접근권한을 가지지 않았을떄 발생합니다.
   * @throws StoreNotFoundException           () -> store Id가 유효하지 않을때 발생합니다.
   */
  @PutMapping("/{storeId}/{productId}/comment")
  @Operation(summary = "상품 설명 수정하기", description = "가게 주인인지 확인한 후, 상품 설명을 수정합니다.")
  public ApiResponse<MessageResponseDto> updateProductComment(@PathVariable String storeId,
                                                              @PathVariable String productId,
                                                              @RequestBody CommentDto commentDto) {
    UUID storeUUID = UUID.fromString(storeId);
    validator.isStoreOwner(storeUUID);

    productService.updateComment(productId, commentDto.content());
    return ApiResponse.from(HttpStatus.OK, "성공", new MessageResponseDto("상품 Comment 설정이 완료되었습니다."));
  }

  /**
   * store id와 product id 기준으로 생성했던 상품 설명 최신순 5개를 조회합니다.
   *
   * @param storeId   -> 가게 id
   * @param productId -> 상품 id
   * @return CommentListDto -> comments(List<String>)
   * @throws UnauthorizedStoreAccessException () -> store에 대한 접근권한을 가지지 않았을떄 발생합니다.
   * @throws StoreNotFoundException           () -> store Id가 유효하지 않을때 발생합니다.
   */
  @GetMapping("/{storeId}/{productId}/comment")
  @Operation(summary = "상품 설명 조회하기", description = "store id와 product id 기준으로 생성했던 상품 설명 최신순 5개를 조회합니다.")
  public ApiResponse<CommentListDto> getProductComment(@PathVariable String storeId,
                                                       @PathVariable String productId) {
    UUID storeUUID = UUID.fromString(storeId);
    validator.isStoreOwner(storeUUID);

    List<String> comments = productService.getRecentProductComment(productId);
    return ApiResponse.from(HttpStatus.OK, "성공", new CommentListDto(comments));
  }

}
