package com.outsta.sns.domain.block.controller;

import com.outsta.sns.common.config.security.CustomUserDetails;
import com.outsta.sns.common.response.SuccessResponse;
import com.outsta.sns.domain.block.dto.BlockListResponse;
import com.outsta.sns.domain.block.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 차단 관련 API 컨트롤러
 *
 * <p>차단, 차단 취소, 차단 목록 조회 기능 제공</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/block")
@Tag(name = "차단", description = "차단 관련 API")
public class BlockController {

    private final BlockService blockService;

    /**
     * 회원 차단 처리
     * - 자기 자신 차단 시도 시 400 반환
     * - 존재하지 않는 회원일 시 404 반환
     * - 이미 차단한 회원일 시 409 반환
     *
     * @param memberId    차단하려고 하는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "회원 차단", description = "회원을 차단합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(자기 자신 차단)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "409", description = "이미 차단한 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{memberId}")
    public ResponseEntity<SuccessResponse<Void>> blockMember(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        blockService.blockMember(userDetails.id(), memberId);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 회원 차단 취소
     * - 자기 자신 차단 취소 시도 시 400 반환
     * - 차단하지 않은 회원일 시 400 반환
     *
     * @param memberId    차단하려고 하는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "회원 차단 취소", description = "회원 차단을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 취소 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(자기 자신 차단 취소, 차단하지 않은 경우)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{memberId}")
    public ResponseEntity<SuccessResponse<Void>> cancelBlockMember(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        blockService.cancelBlock(userDetails.id(), memberId);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 회원 차단 목록 조회
     * - 차단한 회원의 식별자 ID, 닉네임 목록
     *
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return
     */
    @Operation(summary = "회원 차단 목록 조회", description = "회원 차단 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<BlockListResponse>> getBlockList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BlockListResponse blockList = blockService.getBlockList(userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of(blockList));
    }
}
