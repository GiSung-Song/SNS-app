package com.outsta.sns.domain.member.auth;

import com.outsta.sns.common.response.SuccessResponse;
import com.outsta.sns.domain.member.auth.dto.LoginRequest;
import com.outsta.sns.domain.member.auth.dto.TokenDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * <p>로그인, 로그아웃, 토큰 재발급 기능 제공</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 처리
     *
     * @param request  로그인 요청 DTO (이메일, 비밀번호)
     * @param response HTTP 응답 객체 (쿠키 설정)
     * @return 성공 시 Access Token과 Refresh Token을 포함한 응답 반환
     */
    @Operation(summary = "로그인", description = "로그인을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(필수 입력 값 누락)"),
            @ApiResponse(responseCode = "401", description = "이메일 혹은 비밀번호 불일치"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<TokenDto>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        TokenDto tokenDto = authService.login(request);

        String cookieValue = "refreshToken=" + tokenDto.refreshToken()
                + "; Max-Age=604800"
                + "; Path=/api/auth/reissue"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=None";

        response.addHeader("Set-Cookie", cookieValue);

        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, tokenDto));
    }

    /**
     * 로그아웃 처리
     * @param authorizationHeader Authroization 헤더 (Bearer 토큰 포함)
     * @param response HTTP 응답 객체 (쿠키 설정)
     * @return 로그아웃 성공 응답
     */
    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "이미 로그아웃된 상태"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        String accessToken = authorizationHeader.substring(7);

        authService.logout(accessToken);

        String cookieValue = "refreshToken=deleted"
                + "; Max-Age=0"
                + "; Path=/api/auth/reissue"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=None";

        response.addHeader("Set-Cookie", cookieValue);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 액세스 토큰 재발급
     *
     * @param refreshToken 리프레시 토큰 (쿠키에서 가져옴)
     * @return 새로운 Access Token을 포함한 응답 반환
     */
    @Operation(summary = "Access Token 재발급", description = "Access Token을 재발급 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "필수 쿠키 누락"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/reissue")
    public ResponseEntity<SuccessResponse<TokenDto>> reIssueToken(
            @CookieValue("refreshToken") String refreshToken
    ) {
        TokenDto tokenDto = authService.reIssueToken(refreshToken);

        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, tokenDto));
    }
}