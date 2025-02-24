package run.bemin.api.general.auditing;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import run.bemin.api.auth.exception.AuthAccessDeniedException;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.security.UserDetailsImpl;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      // 인증 정보가 없는 경우, 기본값 또는 null을 반환
      // RabbitMQ 로 Store 갱신하려고 할 때, 인증 정보가 없을 경우 에러남
      // 그래서 기본 SYSTEM 으로 설정해야 합니다
      return Optional.of("SYSTEM");
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return Optional.of(((UserDetailsImpl) principal).getUsername());
    } else if (principal instanceof String) {
      return Optional.of((String) principal);
    }

    return Optional.empty();
  }

  private void validateAuthentication(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AuthAccessDeniedException(ErrorCode.AUTH_ACCESS_DENIED.getMessage());
    }
  }
}
