package de.starwit.telraam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=dev",
        "telraam.api-key=test-key",
        "dave.api-url=http://localhost:8080",
        "telraam.bounding-box.min-lon=11.54",
        "telraam.bounding-box.min-lat=48.13",
        "telraam.bounding-box.max-lon=11.60",
        "telraam.bounding-box.max-lat=48.17"
})
class TelraamAdapterApplicationTest {

    @Test
    void contextLoads() {
        // Verifies all beans wire up correctly with minimal configuration
    }
}
