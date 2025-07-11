package com.outsta.sns.domain.profile.controller;

import com.outsta.sns.common.config.security.CustomUserDetails;
import com.outsta.sns.common.response.SuccessResponse;
import com.outsta.sns.domain.profile.dto.request.ProfileImageRequest;
import com.outsta.sns.domain.profile.dto.response.ProfileImageResponse;
import com.outsta.sns.domain.profile.service.ProfileImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 프로필 이미지 등록 API 컨트롤러
 *
 * <p>프로필 이미지 등록, 삭제, 조회, 대표 이미지 변경</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "프로필 이미지", description = "프로필 이미지 관련 API")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    /**
     * 프로필 이미지 등록 처리
     *
     * @param request     (이미지 URL, 원본 파일명, 저장 파일명, 대표 이미지 여부)
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "프로필 이미지 등록", description = "프로필 이미지를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로필 이미지 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(필수값 누락, 유효하지 않은 형식)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/me/profile-images")
    public ResponseEntity<SuccessResponse<Void>> addProfileImage(
            @Valid @RequestBody ProfileImageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        profileImageService.saveProfileImage(userDetails.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(HttpStatus.CREATED));
    }

    /**
     * 프로필 이미지 목록 조회
     * - 자신의 프로필 이미지 목록
     * - 회원의 프로필 이미지 목록
     *
     * @param memberId    조회 하려는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 자신 혹은 회원의 프로필 이미지 목록
     */
    @Operation(summary = "프로필 이미지 목록 조회", description = "프로필 이미지 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 경로 변수)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(차단, 팔로우, 비공개)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{memberId}/profile-images")
    public ResponseEntity<SuccessResponse<ProfileImageResponse>> getProfileImages(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginId = (userDetails != null) ? userDetails.id() : null;
        boolean isMe = loginId != null && loginId.equals(memberId);

        ProfileImageResponse profileImageResponse = isMe
                ? profileImageService.getMyProfileImages(loginId)
                : profileImageService.getProfileImages(loginId, memberId);

        return ResponseEntity.ok(SuccessResponse.of(profileImageResponse));
    }

    /**
     * 프로필 이미지 삭제
     *
     * @param imageId     삭제하려는 프로필 이미지 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 경로 변수)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로필 이미지"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/me/profile-images/{imageId}")
    public ResponseEntity<SuccessResponse<Void>> deleteProfileImage(
            @Parameter(description = "프로필 이미지 식별자 ID", example = "1")
            @PathVariable("imageId") Long imageId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        profileImageService.deleteProfileImage(userDetails.id(), imageId);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 프로필 대표 이미지 설정
     *
     * @param imageId     삭제하려는 프로필 이미지 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "프로필 대표 이미지 설정", description = "프로필 대표 이미지를 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 대표 이미지 설정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 경로 변수)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로필 이미지"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me/profile-images/{imageId}")
    public ResponseEntity<SuccessResponse<Void>> updateRepresentProfileImage(
            @Parameter(description = "프로필 이미지 식별자 ID", example = "1")
            @PathVariable("imageId") Long imageId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        profileImageService.updateRepresentImage(userDetails.id(), imageId);

        return ResponseEntity.ok(SuccessResponse.of());
    }
}
