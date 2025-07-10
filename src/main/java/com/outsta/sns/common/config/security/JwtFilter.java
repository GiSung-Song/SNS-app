package com.outsta.sns.common.config.security;

import com.outsta.sns.domain.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class JwtFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;

    /**
     * 인증 필터에서 예외로 처리할 요청 리스트
     * - 로그인, 토큰 재발급, 회원가입, 닉네임 중복 체크, 이메일 중복 체크, swagger API 등 인증이 필요 없는 경로
     */
    private final List<PermitPass> PASS_PATHS = List.of(
            new PermitPass(HttpMethod.POST, "/api/auth/login"),
            new PermitPass(HttpMethod.POST, "/api/auth/reIssue"),
            new PermitPass(HttpMethod.POST, "/api/members"),
            new PermitPass(HttpMethod.GET, "/api/members/{memberId}"),
            new PermitPass(HttpMethod.GET, "/api/members/{memberId}/follower"),
            new PermitPass(HttpMethod.GET, "/api/members/{memberId}/following"),
            new PermitPass(HttpMethod.GET, "/api/members/check-nickname"),
            new PermitPass(HttpMethod.GET, "/api/members/check-email"),
            new PermitPass(null, "/v3/api-docs"),
            new PermitPass(null, "/swagger-ui")
    );

    /**
     * HTTP 요청마다 실행하는 JWT 인증 필터
     * - 인증 제외 경로는 필터 통과
     * - Authorization 헤더에서 JWT Access Token 추출 및 유효성 검사
     * - Redis에서 로그아웃 여부 확인
     * - 인증 성공 시 SecurityContext에 인증 정보 저장
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Authorization 헤더 값
        String requestHeader = request.getHeader("Authorization");

        // 인증 예외 경로 검사
        boolean isPermitPass = PASS_PATHS.stream()
                .anyMatch(p -> p.matches(method, requestURI));

        // 토큰 여부
        boolean hasToken = StringUtils.hasText(requestHeader) && requestHeader.startsWith("Bearer ");

        if (hasToken) {
            // Bearer 토큰에서 JWT AccessToken 문자열 추출
            String accessToken = requestHeader.substring(7);

            // 토큰 유효성 검사 실패 시 OR 로그아웃 시 401 반환
            if (!jwtProvider.validateToken(accessToken) || isLogout(accessToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 토큰 파싱하여 사용자 정보 추출
            JwtPayload jwtPayload = jwtProvider.parseAccessToken(accessToken);

            Long memberId = jwtPayload.getId();
            String email = jwtPayload.getEmail();
            Role role = Role.valueOf(jwtPayload.getRole());

            // 사용자 인증 객체 생성
            CustomUserDetails customUserDetails = new CustomUserDetails(memberId, role, email);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    List.of(new SimpleGrantedAuthority(role.getValue()))
            );

            // 기존 SecurityContext 초기화 후 인증 정보 저장
            SecurityContextHolder.clearContext();
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } else {
            if (!isPermitPass) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 해시처리 된 토큰으로 로그아웃 여부 확인
     * @param accessToken JWT 액세스 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    private boolean isLogout(String accessToken) {
        String hashToken = jwtProvider.tokenToHash(accessToken);

        return redisTemplate.opsForValue().get(hashToken) != null;
    }
}