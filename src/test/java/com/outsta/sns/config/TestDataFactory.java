package com.outsta.sns.config;

import com.outsta.sns.domain.enums.Gender;
import com.outsta.sns.domain.member.dto.repository.MemberRepository;
import com.outsta.sns.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TestDataFactory {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Member createMember() {
        Member member = Member.builder()
                .name("테스터")
                .password(passwordEncoder.encode("password"))
                .nickname("테스터")
                .email("tester@email.com")
                .birth(LocalDate.of(1989, 11, 3))
                .gender(Gender.MALE)
                .build();

        return memberRepository.save(member);
    }
}
