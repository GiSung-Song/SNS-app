package com.outsta.sns.common.config.security;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    /** JWT 서명에 사용되는 키 */
    private final SecretKey secretKey;

    /** Access Token 만료 시간 */
    private final Long accessTokenExpiration;

    /** Refresh Token 만료 시간 */
    private final Long refreshTokenExpiration;

    /**
     * 생성자에서 비밀키와 토큰 만료시간 초기화
     *
     * @param secretKey Base64URL 인코딩 된 키 문자열
     * @param accessTokenExpiration Access Token 만료 시간
     * @param refreshTokenExpiration Refresh Token 만료 시간
     */
    public JwtProvider(@Value("${jwt.secretKey}") String secretKey,
                       @Value("${jwt.access.expiration}") Long accessTokenExpiration,
                       @Value("${jwt.refresh.expiration}") Long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     * - 필수 정보 누락 시 CustomException 발생
     *
     * @param jwtPayload 토큰에 담을 사용자 정보
     * @return JWT Access Token
     */
    public String generateAccessToken(JwtPayload jwtPayload) {
        if (jwtPayload == null) {
            log.error("Access 토큰 발행 중 파라미터 누락");

            throw new CustomException(ErrorCode.MISSING_JWT_PAYLOAD);
        }

        if (jwtPayload.getId() == null) {
            log.error("Access 토큰 발행 중 id 누락");

            throw new CustomException(ErrorCode.MISSING_JWT_PAYLOAD);
        }

        if (!StringUtils.hasText(jwtPayload.getEmail())) {
            log.error("Access 토큰 발행 중 email 누락");

            throw new CustomException(ErrorCode.MISSING_JWT_PAYLOAD);
        }

        if (!StringUtils.hasText(jwtPayload.getRole())) {
            log.error("Access 토큰 발행 중 role 누락");

            throw new CustomException(ErrorCode.MISSING_JWT_PAYLOAD);
        }

        return Jwts.builder()
                .setSubject(String.valueOf(jwtPayload.getId()))
                .claim("role", jwtPayload.getRole())
                .claim("email", jwtPayload.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .setIssuedAt(new Date())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     * - 필수 정보인 식별자 ID 누락 시 CustomException 발생
     *
     * @param jwtPayload 사용자 정보 (식별자 ID만 포함)
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(JwtPayload jwtPayload) {
        if (jwtPayload.getId() == null) {
            log.error("Refresh 토큰 발행 중 id 누락");

            throw new CustomException(ErrorCode.MISSING_JWT_PAYLOAD);
        }

        return Jwts.builder()
                .setSubject(String.valueOf(jwtPayload.getId()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .setIssuedAt(new Date())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 만료 시간 조회
     *
     * @param token JWT 토큰
     * @return 만료 날짜
     */
    public Date getTokenExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * 토큰 만료 여부 검사
     *
     * @param token JWT 토큰
     * @return 만료됐으면 true, 아니면 false
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseToken(token).getExpiration();

            return expiration.before(new Date());
        } catch (CustomException e) {
            return true;
        }
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);

            return true;
        } catch (CustomException e) {
            return false;
        }
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Access Token 파싱하여 JwtPayload 객체로 변환
     *
     * @param accessToken JWT Access Token
     * @return JwtPayload 객체 (id, email, role)
     */
    public JwtPayload parseAccessToken(String accessToken) {
        Claims body = parseToken(accessToken);

        try {
            Long memberId = Long.parseLong(body.getSubject());
            String role = (String) body.get("role");
            String email = (String) body.get("email");

            JwtPayload jwtPayload = new JwtPayload();
            jwtPayload.setId(memberId);
            jwtPayload.setRole(role);
            jwtPayload.setEmail(email);

            return jwtPayload;
        } catch (Exception e) {
            log.error("Access 토큰 파싱 중 데이터 변환 실패");

            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Refresh Token 파싱하여 회원 식별자 ID 반환
     *
     * @param refreshToken JWT Refresh Token
     * @return 회원 식별자 ID
     */
    public Long parseRefreshToken(String refreshToken) {
        Claims body = parseToken(refreshToken);

        try {
            return Long.parseLong(body.getSubject());
        } catch (Exception e) {
            log.error("Refresh 토큰 파싱 중 Subject 변환 실패");

            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT 토큰 문자열을 SHA-256 해시로 변환
     *
     * @param accessToken JWT Access Token
     * @return SHA-256 해시값, 실패 시 null
     */
    public String tokenToHash(String accessToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(accessToken.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Access 토큰 해시 처리 중 오류 발생");

            return null;
        }
    }

    /**
     * JWT 토큰 파싱 및 클레임 반환
     * - 유효하지 않거나 만료된 토큰일 경우 CustomException 발생
     *
     * @param token JWT 토큰
     * @return Claims 토큰 클레임
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("토큰 파싱 중 이미 만료된 토큰");
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            log.error("토큰 파싱 중 유효하지 않은 토큰");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
