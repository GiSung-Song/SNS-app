package com.outsta.sns.domain.follow.controller;

import com.outsta.sns.common.config.security.CustomUserDetails;
import com.outsta.sns.common.response.SuccessResponse;
import com.outsta.sns.domain.follow.dto.FollowerListResponse;
import com.outsta.sns.domain.follow.dto.FollowingListResponse;
import com.outsta.sns.domain.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 팔로우 관련 API 컨트롤러
 *
 * <p>팔로우 추가, 팔로우 취소, 팔로워 목록 조회, 팔로잉 목록 조회 기능 제공</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "팔로우", description = "팔로우 관련 API")
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우 추가 처리
     *
     * @param memberId    팔로우하려고 하는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "팔로우 추가", description = "팔로우를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "팔로우 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(자기 자신 팔로우)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(차단)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "409", description = "이미 팔로우한 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{memberId}/follow")
    public ResponseEntity<SuccessResponse<Void>> addFollow(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        followService.follow(userDetails.id(), memberId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(HttpStatus.CREATED));
    }

    /**
     * 팔로우 취소 처리
     *
     * @param memberId    팔로우 취소하려고 하는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "팔로우 취소", description = "팔로우를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로우 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(자기 자신 팔로우 취소, 팔로우 하지 않은 경우)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{memberId}/follow")
    public ResponseEntity<SuccessResponse<Void>> cancelFollow(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        followService.cancelFollow(userDetails.id(), memberId);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 팔로워 목록 조회
     * - 자신의 팔로워 목록
     * - 회원의 팔로워 목록
     *
     * @param memberId    팔로워 목록을 조회하려고 하는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 자신 혹은 다른 사람의 팔로워 목록
     */
    @Operation(summary = "팔로워 목록 조회", description = "팔로워 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 경로 변수)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(차단, 팔로워 전용, 비공개 등)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{memberId}/follower")
    public ResponseEntity<SuccessResponse<FollowerListResponse>> getFollowerList(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginId = (userDetails != null) ? userDetails.id() : null;
        boolean isMe = loginId != null && loginId.equals(memberId);

        FollowerListResponse followerListResponse = isMe
                ? followService.getMyFollowerList(loginId)
                : followService.getFollowerList(loginId, memberId);

        return ResponseEntity.ok(SuccessResponse.of(followerListResponse));
    }

    /**
     * 팔로잉 목록 조회
     * - 자신의 팔로잉 목록
     * - 회원의 팔로잉 목록
     *
     * @param memberId    팔로잉 목록을 조회하려고 하는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 자신 혹은 다른 사람의 팔로워 목록
     */
    @Operation(summary = "팔로잉 목록 조회", description = "팔로잉 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 경로 변수)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(차단, 팔로워 전용, 비공개 등)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{memberId}/following")
    public ResponseEntity<SuccessResponse<FollowingListResponse>> getFollowingList(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginId = (userDetails != null) ? userDetails.id() : null;
        boolean isMe = loginId != null && loginId.equals(memberId);

        FollowingListResponse followingListResponse = isMe
                ? followService.getMyFollowingList(loginId)
                : followService.getFollowingList(loginId, memberId);

        return ResponseEntity.ok(SuccessResponse.of(followingListResponse));
    }
}