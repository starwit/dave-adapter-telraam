package de.starwit.telraam.service;

import de.starwit.telraam.client.DaveApiClient;
import de.starwit.telraam.client.TelraamApiClient;
import de.starwit.telraam.dto.dave.DetectionDTO;
import de.starwit.telraam.dto.telraam.TrafficRecord;
import de.starwit.telraam.mapper.TrafficDirectionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full data-transfer pipeline for one 15-minute window:
 *
 * <ol>
 *   <li>Discover active segment IDs inside the configured bounding box.</li>
 *   <li>For each segment: fetch 15-minute traffic data from the Telraam API.</li>
 *   <li>Map each record to two {@link DetectionDTO}s (A→B and B→A) using the
 *       exact field format expected by the Starwit DAVe {@code DetectorController}.</li>
 *   <li>Send the entire window's detections in one batch call to DAVe's
 *       {@code POST /detector/save-latest-detections} endpoint.</li>
 * </ol>
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TelraamApiClient telraamClient;
    private final DaveApiClient daveClient;
    private final TrafficDirectionMapper mapper;

    public TransferService(TelraamApiClient telraamClient,
                           DaveApiClient daveClient,
                           TrafficDirectionMapper mapper) {
        this.telraamClient = telraamClient;
        this.daveClient = daveClient;
        this.mapper = mapper;
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
        List<Long> segmentIds = telraamClient.findSegmentIdsInBoundingBox();
        if (segmentIds.isEmpty()) {
            log.warn("No segments found in bounding box – nothing to transfer");
            return;
        }

        // 2+3. Fetch and map all records across all segments
        List<DetectionDTO> batch = new ArrayList<>();
        for (Long segmentId : segmentIds) {
            List<TrafficRecord> records =
                    telraamClient.fetchTraffic(segmentId, windowStart, windowEnd);

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
