package run.bemin.api.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.bemin.api.user.entity.UserRoleEnum;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
  // Access Token Header KEY 값
  public static final String AUTHORIZATION_HEADER = "Authorization";
  // Refresh Token Header KEY 값
  public static final String REFRESH_HEADER = "refresh";
  // 사용자 권한 값의 KEY
  public static final String AUTHORIZATION_KEY = "auth";
  // Token 식별자
  public static final String BEARER_PREFIX = "Bearer ";
  // AccessToken 만료시간 24시간
  private final long ACCESS_TOKEN_TIME = 24 * 60 * 60 * 1000L;
  // RefreshToken 만료시간 - 14일
  private final long REFRESHTOKEN_TIME = 14 * 24 * 60 * 60 * 1000L;


  @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
  public String secretKey; // JwtUtilTokenTest로 인해 public으로 변경했습니다.
  private Key key;
  private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

  @PostConstruct
  public void init() {
    byte[] bytes = Base64.getDecoder().decode(secretKey);
    key = Keys.hmacShaKeyFor(bytes);
  }

  // 토큰 생성
  public String createAccessToken(String username, UserRoleEnum role) {
    Date date = new Date();

    return BEARER_PREFIX +
        Jwts.builder()
            .setSubject(username) // 사용자 식별자값(ID)
            .claim(AUTHORIZATION_KEY, role) // 사용자 권한
            .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME)) // 만료 시간
            .setIssuedAt(date) // 발급일
            .signWith(key, signatureAlgorithm) // 암호화 알고리즘
            .compact();
  }

  public String createRefreshToken(String userName) {
    Date date = new Date();

    return BEARER_PREFIX +
        Jwts.builder()
            .setSubject(userName) // 사용자 식별자값(ID)
            .setExpiration(new Date(date.getTime() + REFRESHTOKEN_TIME)) // 만료 시간
            .setIssuedAt(date) // 발급일
            .signWith(key, signatureAlgorithm) // 암호화 알고리즘
            .compact();

  }

  public void addJwtToHeader(String tokenHeader, String token, HttpServletResponse res) {
    res.addHeader(tokenHeader, token);
  }

  // header 에서 JWT 가져오기
  public String getTokenFromHeader(String tokenHeader, HttpServletRequest request) {
    // header에서 token을 가져온다.
    String token = request.getHeader(tokenHeader);
    // 공백(null)인지 && BEARER로 시작을 하는지 확인
    if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
      // 둘 다 만족할 경우 BEARER 뒤에 공백 길이만큼 잘라내고 순수한 토큰 값만을 리턴
      return token.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  // 토큰 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException | SignatureException e) {
      log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
    } catch (ExpiredJwtException e) {
      log.error("Expired JWT token, 만료된 JWT token 입니다.");
    } catch (UnsupportedJwtException e) {
      log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
    } catch (IllegalArgumentException e) {
      log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
    }
    return false;
  }

  // 토큰에서 사용자 정보 가져오기
  public Claims getUserInfoFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  // 토큰에서 사용자 이메일(Subject) 가져오기
  public String getUserEmailFromToken(String token) {
    Claims claims = getUserInfoFromToken(token);
    return claims.getSubject();
  }

  public long getAccessTokenExpiration() {
    return ACCESS_TOKEN_TIME; // 예: 24시간
  }

  public long getRefreshTokenExpiration() {
    return REFRESHTOKEN_TIME; // 예: 3분 (또는 원하는 시간)
  }

  public long getRemainingExpirationTime(String token) {
    // 토큰의 만료 시간을 가져오기
    Claims claims = getUserInfoFromToken(token);
    Date expiration = claims.getExpiration();
    // 현재 시간과 비교하여 남은 시간을 계산 (밀리초 단위)
    long remainingTime = expiration.getTime() - System.currentTimeMillis();
    return remainingTime > 0 ? remainingTime : 0;
  }


}