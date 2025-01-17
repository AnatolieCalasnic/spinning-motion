package org.myexample.spinningmotion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpinningMotionApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test to ensure the entire Spring application can be initialized
        // helps catch configuration errors early in the development process
    }

}
