package com.outsta.sns;

import com.outsta.sns.config.DBContainerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class SnsApplicationTests {

	@Test
	void contextLoads() {
	}

}
