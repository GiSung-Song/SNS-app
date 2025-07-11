package com.outsta.sns.config;

import com.outsta.sns.common.config.security.CustomUserDetails;
import com.outsta.sns.domain.block.entity.Block;
import com.outsta.sns.domain.block.repository.BlockRepository;
import com.outsta.sns.domain.enums.Gender;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.follow.entity.Follow;
import com.outsta.sns.domain.follow.repository.FollowRepository;
import com.outsta.sns.domain.member.repository.MemberRepository;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.profile.dto.request.ProfileImageRequest;
import com.outsta.sns.domain.profile.entity.ProfileImage;
import com.outsta.sns.domain.profile.repository.ProfileImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TestDataFactory {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Member createTester() {
        Member member = Member.builder()
                .name("테스터")
                .password(passwordEncoder.encode("password"))
                .nickname("테스터")
                .email("tester@email.com")
                .birth(LocalDate.of(1989, 11, 3))
                .gender(Gender.MALE)
                .role(Role.MEMBER)
                .build();

        return memberRepository.save(member);
    }

    public Member createFaker() {
        Member member = Member.builder()
                .name("페이커")
                .password(passwordEncoder.encode("password"))
                .nickname("페이커")
                .email("faker@email.com")
                .birth(LocalDate.of(1996, 3, 7))
                .gender(Gender.MALE)
                .role(Role.MEMBER)
                .build();

        return memberRepository.save(member);
    }

    public Member createDancer() {
        Member member = Member.builder()
                .name("댄서")
                .password(passwordEncoder.encode("password"))
                .nickname("댄서")
                .email("dancer@email.com")
                .birth(LocalDate.of(2001, 7, 25))
                .gender(Gender.FEMALE)
                .role(Role.MEMBER)
                .build();

        return memberRepository.save(member);
    }

    public Member createGuest() {
        Member member = Member.builder()
                .name("게스트")
                .password(passwordEncoder.encode("password"))
                .nickname("게스트")
                .email("guest@email.com")
                .birth(LocalDate.of(2015, 6, 22))
                .gender(Gender.FEMALE)
                .role(Role.GUEST)
                .build();

        return memberRepository.save(member);
    }

    public Block createBlock(Member blocker, Member blocked) {
        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        return blockRepository.save(block);
    }

    public Follow createFollow(Member follower, Member following) {
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        return followRepository.save(follow);
    }

    public ProfileImage createProfileImage(Member member, int i, boolean represent) {
        ProfileImage profileImage = ProfileImage.builder()
                .member(member)
                .imageUrl("imageUrl" + i)
                .originName("originName" + i)
                .fileName("fileName" + i)
                .represent(represent)
                .build();

        return profileImageRepository.save(profileImage);
    }

    public void setAuthentication(Member member) {
        CustomUserDetails customUserDetails = new CustomUserDetails(member.getId(), member.getRole(), member.getEmail());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                List.of(new SimpleGrantedAuthority(member.getRole().getValue()))
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
