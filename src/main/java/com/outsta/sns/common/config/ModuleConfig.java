package com.outsta.sns.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleConfig {

    /**
     * ObjectMapper 빈 등록
     * - JavaTimeModule 등록하여 날짜/시간 타입 지원
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // LocalDate, LocalDateTime 등 직렬화/역직렬화 가능하도록 설정
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
