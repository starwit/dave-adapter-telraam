package de.starwit.telraam.service;

import de.starwit.telraam.client.DaveApiClient;
import de.starwit.telraam.client.TelraamApiClient;
import de.starwit.telraam.config.SegmentMappingProperties;
import de.starwit.telraam.dto.dave.DetectionDTO;
import de.starwit.telraam.dto.telraam.SegmentInstancesResponse;
import de.starwit.telraam.dto.telraam.TrafficRecord;
import de.starwit.telraam.mapper.RoadOrientationDetector;
import de.starwit.telraam.mapper.TrafficDirectionMapper;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full data-transfer pipeline for one 15-minute window:
 *
 * <ol>
 * <li>Discover active segment IDs inside the configured bounding box.</li>
 * <li>For each segment: fetch 15-minute traffic data from the Telraam API.</li>
 * <li>Map each record to two {@link DetectionDTO}s (A→B and B→A) using the
 * exact field format expected by the Starwit DAVe
 * {@code DetectorController}.</li>
 * <li>Send the entire window's detections in one batch call to DAVe's
 * {@code POST /detector/save-latest-detections} endpoint.</li>
 * </ol>
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    @Autowired
    private TelraamApiClient telraamClient;

    @Autowired
    private DaveApiClient daveClient;

    @Autowired
    private SegmentMappingProperties segmentMappingProperties;

    @Autowired
    private TrafficDirectionMapper mapper;

    @Autowired
    private RoadOrientationDetector orientationDetector;

    private Map<String, RoadOrientationDetector.OrientationResult> segmentOrientations = new HashMap<>();

    @PostConstruct
    private void init() throws InterruptedException {
        log.info("Running initial data collection to prime caches and detect orientations");
        runDataCollection();
    }

    private void runDataCollection() throws InterruptedException {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int minuteOffset = now.getMinute() % 15;
        OffsetDateTime windowEnd = now
                .minusMinutes(minuteOffset)
                .withSecond(0)
                .withNano(0);
        var segments = telraamClient.fetchSegmentsInArea();
        Thread.sleep(2000); // avoid hitting API rate limits during startup
        for (String segment : segments) {
            //if segment is not in config, skip
            if (segmentMappingProperties.findBySegmentId(segment).isEmpty()) {
                log.warn("Segment {} not found in configuration – skipping, add new mapping!", segment);
                continue;
            }
            // Fetch geometry and detect orientation for each segment, caching the results
            SegmentInstancesResponse segmentResponse = telraamClient.fetchSegmentInstances(segment);
            log.debug(segmentResponse.toString());
            var orientationResult = orientationDetector.detect(segmentResponse.features().get(0).geometry());
            segmentOrientations.put(segment, orientationResult);
            log.debug(orientationResult.toString());
            var mapping = segmentMappingProperties.findBySegmentId(segment);
            mapping.get().setDirectionAtoB(orientationResult.directionAtoB());
            mapping.get().setDirectionBtoA(orientationResult.directionBtoA());
            Thread.sleep(2000);
            // load traffic report
            List<TrafficRecord> result = telraamClient.fetchTraffic(segment, windowEnd.minusMinutes(30),
                    windowEnd.minusMinutes(15));
            log.debug("Traffic report for segment {}: {}", segment, result.toString());
            Thread.sleep(2000);
            
            DetectionDTO daveDTO = mapper.map(result.get(0)).get(0);

            daveClient.sendSingle(daveDTO);
        }
    }

    /**
     * Fires every 15 minutes (at :00, :15, :30, :45).
     * Spring cron format: {@code second minute hour day month weekday}
     */
    @Scheduled(cron = "${telraam.scheduler.cron:0 */15 * * * *}")
    public void runTransfer() {
        log.info("Scheduled transfer triggered");
        try {
            transferLatest();
        } catch (Exception ex) {
            // Catch-all so the scheduler stays alive even on unexpected errors
            log.error("Unexpected error during scheduled transfer: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Runs one full transfer for the 15-minute window ending at {@code windowEnd}.
     *
     * @param windowEnd end of the window (exclusive); start = windowEnd − 15 min
     */
    public void transfer(OffsetDateTime windowEnd) {
        OffsetDateTime windowStart = windowEnd.minusMinutes(15);
        log.info("Starting transfer run for window [{} – {}]", windowStart, windowEnd);

        // 1. Auto-discover segments in the bounding box
        List<String> segmentIds = telraamClient.fetchSegmentsInArea();
        if (segmentIds.isEmpty()) {
            log.warn("No segments found in bounding box – nothing to transfer");
            return;
        }

        // 2+3. Fetch and map all records across all segments
        List<DetectionDTO> batch = new ArrayList<>();
        for (String segmentId : segmentIds) {
            List<TrafficRecord> records = telraamClient.fetchTraffic(segmentId, windowStart, windowEnd);

            for (TrafficRecord record : records) {
                List<DetectionDTO> detections = mapper.map(record);
                batch.addAll(detections);
            }
        }

        if (batch.isEmpty()) {
            log.info("No detections produced for window [{} – {}] – nothing to send",
                    windowStart, windowEnd);
            return;
        }

        log.info("Collected {} detection DTO(s) from {} segment(s) – sending batch to DAVe",
                batch.size(), segmentIds.size());

        // 4. Single batch call to DAVe
        boolean ok = daveClient.sendBatch(batch);
        if (ok) {
            log.info("Transfer run complete – {} detection(s) accepted by DAVe", batch.size());
        } else {
            log.error("Transfer run failed – DAVe rejected the batch for window [{} – {}]",
                    windowStart, windowEnd);
        }
    }

    /**
     * Derives the current 15-minute window from the system clock (UTC) and runs
     * a transfer for it.
     */
    public void transferLatest() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int minuteOffset = now.getMinute() % 15;
        OffsetDateTime windowEnd = now
                .minusMinutes(minuteOffset)
                .withSecond(0)
                .withNano(0);
        transfer(windowEnd);
    }
}
