package com.outsta.sns.domain.member.controller;

import com.outsta.sns.common.config.security.CustomUserDetails;
import com.outsta.sns.common.response.SuccessResponse;
import com.outsta.sns.domain.member.dto.request.*;
import com.outsta.sns.domain.member.dto.response.CheckEmailResponse;
import com.outsta.sns.domain.member.dto.response.CheckNicknameResponse;
import com.outsta.sns.domain.member.dto.response.MemberIdResponse;
import com.outsta.sns.domain.member.dto.response.MemberInfoResponse;
import com.outsta.sns.domain.member.service.MemberQueryService;
import com.outsta.sns.domain.member.service.MemberService;
import com.outsta.sns.domain.profile.dto.response.ProfileImageResponse;
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
 * 회원 관련 API 컨트롤러
 *
 * <p>회원가입, 중복체크(이메일, 닉네임), 수정(비밀번호, 닉네임) 등 기능 제공</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;
    private final MemberQueryService memberQueryService;

    /**
     * 회원 가입 처리
     * - 회원가입 정상 요청 시 해당 이메일로 인증코드 전송
     *
     * @param request 회원가입 요청 DTO (이메일, 닉네임, 비밀번호, 이메일, 생년월일, 성별)
     */
    @Operation(summary = "회원가입", description = "회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락/유효하지 않은 형식"),
            @ApiResponse(responseCode = "409", description = "중복된 이메일/닉네임"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> signUp(
            @RequestBody @Valid SignUpRequest request
    ) {
        memberService.signUpMember(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(HttpStatus.CREATED));
    }

    /**
     * 닉네임 중복 체크 처리
     *
     * @param request 닉네임 중복 체크 DTO (닉네임)
     * @return 닉네임 중복일 시 true, 아니면 false
     */
    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복을 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 중복 체크 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임 형식/파라미터 누락"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<SuccessResponse<CheckNicknameResponse>> checkNicknameDuplicate(
            @Valid @ModelAttribute NicknameCheckRequest request
    ) {
        CheckNicknameResponse response = memberService.checkDuplicateNickname(request.nickname());

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    /**
     * 이메일 중복 체크 처리
     *
     * @param request 이메일 중복 체크 DTO (이메일)
     * @return 이메일 중복일 시 true, 아니면 false
     */
    @Operation(summary = "이메일 중복 체크", description = "이메일 중복을 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 중복 체크 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 형식/파라미터 누락"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/check-email")
    public ResponseEntity<SuccessResponse<CheckEmailResponse>> checkEmailDuplicate(
            @Valid @ModelAttribute EmailCheckRequest request
    ) {
        CheckEmailResponse response = memberService.checkDuplicateEmail(request.email());

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    /**
     * 닉네임 수정 처리
     * - 닉네임 중복일 시 409 반환
     *
     * @param request     닉네임 수정 DTO (닉네임)
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 회원 식별자 ID
     */
    @Operation(summary = "닉네임 수정", description = "닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임 형식/필수값 누락"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "409", description = "닉네임 중복"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me/nickname")
    public ResponseEntity<SuccessResponse<MemberIdResponse>> updateNickname(
            @Valid @RequestBody NicknameUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberIdResponse response = memberService.updateNickname(userDetails.id(), request);

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    /**
     * 비밀번호 수정 처리
     *
     * @param request     비밀번호 수정 DTO (비밀번호)
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 회원 식별자 ID
     */
    @Operation(summary = "비밀번호 수정", description = "비밀번호를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 비밀번호 형식/필수값 누락"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<SuccessResponse<MemberIdResponse>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberIdResponse response = memberService.updatePassword(userDetails.id(), request);

        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, response));
    }

    /**
     * 회원 탈퇴 처리
     *
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     */
    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse<Void>> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        memberService.deleteMember(userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 프로필 정보 공개 범위 설정 처리
     *
     * @param request     공개 범위 설정 DTO (Visibility)
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 회원 식별자 ID
     */
    @Operation(summary = "프로필 정보 공개 범위 설정", description = "프로필 정보 공개 범위를 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개범위 설정 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 형식/필수값 누락"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me/privacy")
    public ResponseEntity<SuccessResponse<MemberIdResponse>> updatePrivacy(
            @Valid @RequestBody PrivacyUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberIdResponse response = memberService.updatePrivacy(userDetails.id(), request);

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    /**
     * 비밀번호 초기화 처리
     * - 이메일, 이름, 생년월일 비교하여 다 맞으면 이메일로 임시 비밀번호 전송
     *
     * @param request (이메일, 이름, 생년월일)
     */
    @Operation(summary = "비밀번호 초기화", description = "비밀번호를 초기화하며 이메일로 임시 비밀번호가 전송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일로 임시 비밀번호 전송"),
            @ApiResponse(responseCode = "400", description = "필수값 누락/유효하지 않은 형식"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        memberService.resetPassword(request);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 회원 탈퇴 취소 처리
     * @param request (이메일, 이름, 생년월일, 비밀번호)
     */
    @Operation(summary = "회원 탈퇴 취소", description = "회원 탈퇴를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일로 임시 비밀번호 전송"),
            @ApiResponse(responseCode = "400", description = "필수값 누락/유효하지 않은 형식/탈퇴처리가 되지 않은 회원"),
            @ApiResponse(responseCode = "401", description = "이메일 혹은 비밀번호 오류"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse<Void>> cancelDeleteMember(
            @Valid @RequestBody CancelDeleteRequest request
    ) {
        memberService.cancelDeleteMember(request);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 이메일 인증 코드 확인 처리
     * @param request (이메일, 인증코드)
     */
    @Operation(summary = "인증 코드 확인", description = "인증 코드를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 코드 확인 성공"),
            @ApiResponse(responseCode = "400", description = "필수값 누락"),
            @ApiResponse(responseCode = "401", description = "이메일 혹은 인증번호 오류"),
            @ApiResponse(responseCode = "409", description = "이미 인증된 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/code-verification")
    public ResponseEntity<SuccessResponse<Void>> checkCode(
            @Valid @RequestBody CodeCheckRequest request
    ) {
        memberService.checkCode(request);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 인증 코드 재전송 처리
     * @param request (이메일)
     */
    @Operation(summary = "인증 코드 재전송", description = "인증 코드를 재전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일로 인증 코드 재전송"),
            @ApiResponse(responseCode = "400", description = "필수값 누락"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/code-resend")
    public ResponseEntity<SuccessResponse<Void>> reSendCode(
            @Valid @RequestBody CodeReSendRequest request
    ) {
        memberService.reSendCode(request);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 회원 정보 조회
     * - 자신 혹은 상대방
     *
     * @param memberId    조회 하려는 회원의 식별자 ID
     * @param userDetails 현재 로그인한(인증된) 회원 객체 (사용자 식별자 ID, 이메일, Role)
     * @return 회원 정보
     */
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 경로 변수"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(차단/팔로워 전용/비공개)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{memberId}")
    public ResponseEntity<SuccessResponse<MemberInfoResponse>> reSendCode(
            @Parameter(description = "회원 식별자 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginId = (userDetails != null) ? userDetails.id() : null;
        boolean isMe = loginId != null && loginId.equals(memberId);

        MemberInfoResponse memberInfoResponse = isMe
                ? memberQueryService.getMyInfo(loginId)
                : memberQueryService.getMemberInfo(loginId, memberId);

        return ResponseEntity.ok(SuccessResponse.of(memberInfoResponse));
    }
}