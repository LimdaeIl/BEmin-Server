package run.bemin.api.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

  private static final String REFRESH_COOKIE_NAME = "refresh";

  public static void addRefreshCookie(HttpServletResponse res, String rawRefreshToken, JwtUtil jwtUtil) {
    if (rawRefreshToken == null) {
      return;
    }
    // Bearer 접두어 제거
    String token = rawRefreshToken.startsWith(JwtUtil.BEARER_PREFIX)
        ? rawRefreshToken.substring(JwtUtil.BEARER_PREFIX.length())
        : rawRefreshToken;

    Cookie refreshCookie = new Cookie(REFRESH_COOKIE_NAME, token);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiration() / 1000));
    refreshCookie.setSecure(true);
    res.addCookie(refreshCookie);
  }

  public static String getRefreshTokenFromCookies(HttpServletRequest req) {
    if (req.getCookies() != null) {
      for (Cookie cookie : req.getCookies()) {
        if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
