package de.starwit.telraam.client;

import de.starwit.telraam.dto.dave.DetectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Sends traffic detections to the Starwit DAVe backend via the
 * {@code DetectorController} REST API.
 *
 * <p>Two endpoints are available:</p>
 * <ul>
 *   <li>{@code POST /detector/save-detection} – single record</li>
 *   <li>{@code POST /detector/save-latest-detections} – batch (preferred)</li>
 * </ul>
 *
 * <p>Authentication: DAVe uses Keycloak / OAuth2. The {@link WebClient} bean
 * named {@code daveWebClient} must be configured with a client-credentials
 * token relay (see {@link de.starwit.telraam.config.WebClientConfig}).
 * The caller must hold the {@code FACHADMIN} role.</p>
 */
@Service
public class DaveApiClient {

    private static final Logger log = LoggerFactory.getLogger(DaveApiClient.class);

    private static final String BATCH_PATH  = "/detector/save-latest-detections";
    private static final String SINGLE_PATH = "/detector/save-detection";

    private final WebClient webClient;

    public DaveApiClient(@Qualifier("daveWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Sends a batch of {@link DetectionDTO}s to DAVe in a single HTTP call.
     * This is the preferred path — one call per 15-minute transfer run.
     *
     * @param detections list to submit (must not be empty)
     * @return {@code true} on HTTP 2xx, {@code false} on any error
     */
    public boolean sendBatch(List<DetectionDTO> detections) {
        if (detections.isEmpty()) {
            log.debug("sendBatch called with empty list – nothing to send");
            return true;
        }

        log.debug("Sending batch of {} detection(s) to DAVe", detections.size());

        try {
            webClient.post()
                    .uri(BATCH_PATH)
                    .bodyValue(detections)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new RuntimeException("DAVe API error " +
                                                    resp.statusCode() + ": " + body)))
                    )
                    .toBodilessEntity()
                    .block();

            log.info("DAVe accepted batch of {} detection(s)", detections.size());
            return true;

        } catch (Exception ex) {
            log.error("Failed to send batch of {} detection(s) to DAVe: {}",
                    detections.size(), ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Sends a single {@link DetectionDTO} to DAVe.
     * Prefer {@link #sendBatch} for normal operation.
     *
     * @param detection the record to submit
     * @return {@code true} on HTTP 2xx
     */
    public boolean sendSingle(DetectionDTO detection) {
        try {
            webClient.post()
                    .uri(SINGLE_PATH)
                    .bodyValue(detection)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new RuntimeException("DAVe API error " +
                                                    resp.statusCode() + ": " + body)))
                    )
                    .toBodilessEntity()
                    .block();

            log.debug("DAVe accepted single detection {}", detection);
            return true;

        } catch (Exception ex) {
            log.error("Failed to send single detection to DAVe: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
