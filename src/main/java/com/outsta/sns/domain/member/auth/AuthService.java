package com.outsta.sns.domain.member.auth;

import com.outsta.sns.common.config.security.JwtPayload;
import com.outsta.sns.common.config.security.JwtProvider;
import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.enums.Activation;
import com.outsta.sns.domain.member.auth.dto.LoginRequest;
import com.outsta.sns.domain.member.auth.dto.TokenDto;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 인증 관련 서비스
 *
 * <p>로그인, 로그아웃, 토큰 재발급 기능 제공</p>
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 로그인 처리
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return 액세스 토큰 및 리프레시 토큰 DTO 반환
     * @throws CustomException 활동중인 회원이 아니거나, 탈퇴 대기중이거나, 비밀번호가 틀릴 시 예외 발생
     */
    @Transactional(readOnly = true)
    public TokenDto login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (member.getActivation() == Activation.SUSPENDED) {
            throw new CustomException(ErrorCode.SUSPENDED_MEMBER);
        }

        if (member.getActivation() == Activation.WAITING_DELETED) {
            throw new CustomException(ErrorCode.WAITING_DELETED_MEMBER);
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        JwtPayload jwtPayload = new JwtPayload(member.getId(), member.getEmail(), member.getRole().getCode());

        String accessToken = jwtProvider.generateAccessToken(jwtPayload);
        String refreshToken = jwtProvider.generateRefreshToken(jwtPayload);

        String redisKey = "refresh:" + member.getId();

        redisTemplate.opsForValue().set(redisKey, refreshToken, jwtProvider.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * 로그아웃 처리
     * - 로그아웃 성공 시 Redis에 블랙리스트(logout) 추가
     *
     * @param accessToken JWT Access Token
     */
    public void logout(String accessToken) {
        Date tokenExpiration = jwtProvider.getTokenExpiration(accessToken);

        long expiration = tokenExpiration.getTime() - System.currentTimeMillis();
        String hashToken = jwtProvider.tokenToHash(accessToken);

        redisTemplate.opsForValue().set(hashToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * 토큰 재발급 처리
     *
     * @param refreshToken 쿠키에서 제공된 리프레시 토큰
     * @return 새로 발급된 액세스 토큰
     * @throws CustomException 유효하지 않은 토큰 또는 사용자가 없는 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public TokenDto reIssueToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtProvider.parseRefreshToken(refreshToken);

        Member member = memberRepository.findActiveMemberById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + member.getId());

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        JwtPayload jwtPayload = new JwtPayload(member.getId(), member.getEmail(), member.getRole().getCode());

        String accessToken = jwtProvider.generateAccessToken(jwtPayload);

        return new TokenDto(accessToken, null);
    }
}