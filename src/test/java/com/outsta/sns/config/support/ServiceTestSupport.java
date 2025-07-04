package com.outsta.sns.config.support;

import com.outsta.sns.config.DBContainerExtension;
import com.outsta.sns.config.TestDataFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(DBContainerExtension.class)
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public abstract class ServiceTestSupport {

    @Autowired
    protected TestDataFactory testDataFactory;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;
}
