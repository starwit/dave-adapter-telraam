package de.starwit.telraam.client;

import de.starwit.telraam.config.AdapterProperties.TelraamProperties;
import de.starwit.telraam.dto.telraam.ActiveCamerasResponse;
import de.starwit.telraam.dto.telraam.TrafficRecord;
import de.starwit.telraam.dto.telraam.TrafficRequest;
import de.starwit.telraam.dto.telraam.TrafficResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Wraps all HTTP calls to the Telraam API v1.
 */
@Service
public class TelraamApiClient {

    private static final Logger log = LoggerFactory.getLogger(TelraamApiClient.class);
    private static final DateTimeFormatter TELRAAM_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");

    private final WebClient webClient;
    private final TelraamProperties props;

    public TelraamApiClient(@Qualifier("telraamWebClient") WebClient webClient,
                            TelraamProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    /**
     * Discovers all active segment IDs within the configured bounding box by
     * calling the traffic snapshot endpoint.
     *
     * @return list of unique segment IDs (may be empty, never null)
     */
    public List<Long> findSegmentIdsInBoundingBox() {
        var bbox = props.getBoundingBox();
        log.debug("Discovering segments in bbox [{},{} – {},{}]",
                bbox.getMinLon(), bbox.getMinLat(), bbox.getMaxLon(), bbox.getMaxLat());

        // The snapshot endpoint accepts a bounding box and returns GeoJSON features.
        Map<String, Object> body = Map.of(
                "type", "bbox",
                "contents", Map.of(
                        "min_lon", bbox.getMinLon(),
                        "min_lat", bbox.getMinLat(),
                        "max_lon", bbox.getMaxLon(),
                        "max_lat", bbox.getMaxLat()
                )
        );

        try {
            ActiveCamerasResponse response = webClient.post()
                    .uri("/cameras/active")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ActiveCamerasResponse.class)
                    .block();

            if (response == null || response.features() == null) {
                log.warn("Empty response from /cameras/active");
                return Collections.emptyList();
            }

            List<Long> ids = response.features().stream()
                    .map(f -> f.properties().segmentId())
                    .distinct()
                    .toList();

            log.info("Discovered {} segment(s) in bounding box", ids.size());
            return ids;

        } catch (Exception ex) {
            log.error("Failed to discover segments from Telraam API: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    /**
     * Fetches 15-minute traffic records for a single segment within [from, to).
     *
     * @param segmentId Telraam segment ID
     * @param from      window start (inclusive)
     * @param to        window end (exclusive)
     * @return list of traffic records (may be empty on API error)
     */
    public List<TrafficRecord> fetchTraffic(Long segmentId, OffsetDateTime from, OffsetDateTime to) {
        String timeStart = from.format(TELRAAM_FMT);
        String timeEnd   = to.format(TELRAAM_FMT);

        log.debug("Fetching traffic for segment {} [{} – {}]", segmentId, timeStart, timeEnd);

        TrafficRequest request = TrafficRequest.forSegment(String.valueOf(segmentId), timeStart, timeEnd);

        try {
            TrafficResponse response = webClient.post()
                    .uri("/reports/traffic")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new RuntimeException("Telraam API error " +
                                                    clientResponse.statusCode() + ": " + body)))
                    )
                    .bodyToMono(TrafficResponse.class)
                    .block();

            if (response == null || response.report() == null) {
                log.warn("No traffic data returned for segment {}", segmentId);
                return Collections.emptyList();
            }

            log.debug("Received {} record(s) for segment {}", response.report().size(), segmentId);
            return response.report();

        } catch (Exception ex) {
            log.error("Failed to fetch traffic for segment {}: {}", segmentId, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
