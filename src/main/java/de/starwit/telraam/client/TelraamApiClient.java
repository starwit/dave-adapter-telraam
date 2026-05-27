package de.starwit.telraam.client;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import de.starwit.telraam.config.AdapterProperties.TelraamProperties;
import de.starwit.telraam.dto.telraam.SegmentInstancesResponse;
import de.starwit.telraam.dto.telraam.SegmentsAreaRequest;
import de.starwit.telraam.dto.telraam.SegmentsAreaResponse;
import de.starwit.telraam.dto.telraam.TrafficRecord;
import de.starwit.telraam.dto.telraam.TrafficReportRequest;
import de.starwit.telraam.dto.telraam.TrafficResponse;
import reactor.core.publisher.Mono;

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
     * Fetches 15-minute traffic records for a single segment within [from, to).
     *
     * @param segmentId Telraam segment ID
     * @param from      window start (inclusive)
     * @param to        window end (exclusive)
     * @return list of traffic records (may be empty on API error)
     */
    public List<TrafficRecord> fetchTraffic(String segmentId, OffsetDateTime from, OffsetDateTime to) {
        String timeStart = from.format(TELRAAM_FMT);
        String timeEnd   = to.format(TELRAAM_FMT);

        log.debug("Fetching traffic for segment {} [{} – {}]", segmentId, timeStart, timeEnd);

        TrafficReportRequest request = TrafficReportRequest.forSegment(segmentId, timeStart, timeEnd);

        try {
            TrafficResponse response = webClient.post()
                    .uri("/advanced/reports/traffic")
                    .header("X-Api-Key", props.getApiKey())
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

    /**
     * Fetches all segments within the configured bounding box using the segments/area endpoint.
     * This endpoint returns segment geometries (road segments) within a geographic polygon.
     *
     * @return list of unique segment IDs (may be empty on API error)
     */
    public List<String> fetchSegmentsInArea() {
        var bbox = props.getBoundingBox();
        log.debug("Fetching segments in area [{},{} – {},{}]",
                bbox.getMinLon(), bbox.getMinLat(), bbox.getMaxLon(), bbox.getMaxLat());

        SegmentsAreaRequest request = SegmentsAreaRequest.fromBoundingBox(
                bbox.getMinLon(), bbox.getMinLat(), bbox.getMaxLon(), bbox.getMaxLat());

        try {
            SegmentsAreaResponse response = webClient.post()
                    .uri("/v1/segments/area")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new RuntimeException("Telraam API error " +
                                                    clientResponse.statusCode() + ": " + body)))
                    )
                    .bodyToMono(SegmentsAreaResponse.class)
                    .block();

            if (response == null || response.features() == null) {
                log.warn("Empty response from /segments/area");
                return Collections.emptyList();
            }

            List<String> segmentIds = response.features().stream()
                    .map(f -> f.properties().segmentId())
                    .distinct()
                    .toList();

            log.info("Discovered {} segment(s) in area", segmentIds.size());
            return segmentIds;

        } catch (Exception ex) {
            log.error("Failed to fetch segments from Telraam API: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public SegmentInstancesResponse fetchSegmentInstances(String segmentId) {
        try {
            return webClient.get()
                    .uri("/v1/segments/id/{id}", segmentId)
                    .header("X-Api-Key", props.getApiKey())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new RuntimeException("Telraam API error " +
                                                    clientResponse.statusCode() + ": " + body)))
                    )
                    .bodyToMono(SegmentInstancesResponse.class)
                    .block();
        } catch (Exception ex) {
            log.error("Failed to fetch segment instances for {}: {}", segmentId, ex.getMessage(), ex);
            return null;
        }
    }
}
