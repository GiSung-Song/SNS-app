package com.outsta.sns.config.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outsta.sns.config.DBContainerExtension;
import com.outsta.sns.config.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TestDataFactory testDataFactory;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void clearAuthentication() {
        testDataFactory.clearAuthentication();
    }
}
