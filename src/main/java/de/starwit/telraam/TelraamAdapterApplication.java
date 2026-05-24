package de.starwit.telraam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Telraam → DAVe adapter.
 *
 * <p>Every 15 minutes the scheduler fetches traffic counts from all Telraam
 * sensors inside a configured geo-fenced bounding box and forwards them to the
 * DAVe REST API in the direction format expected by DAVe (N/S/E/W).</p>
 */
@SpringBootApplication
@EnableScheduling
public class TelraamAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelraamAdapterApplication.class, args);
    }
}
