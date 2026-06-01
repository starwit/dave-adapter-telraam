package de.starwit.telraam.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Maps each Telraam segment_id to the corresponding DAVe Zählung UUID and the
 * two DAVe direction integers that represent A→B and B→A traffic flow on that
 * segment.
 *
 * <p>
 * Example configuration in {@code application.yml}:
 * </p>
 * 
 * <pre>
 * telraam:
 *   segment-mapping:
 *     - segment-id: 9000001463
 *       zaehlung-id: "550e8400-e29b-41d4-a716-446655440000"
 *       direction-a-to-b: 1    # North
 *       direction-b-to-a: 2    # South
 *     - segment-id: 9000001464
 *       zaehlung-id: "550e8400-e29b-41d4-a716-446655440001"
 *       direction-a-to-b: 3    # East
 *       direction-b-to-a: 4    # West
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "telraam")
public class SegmentMappingProperties {

    private List<SegmentMapping> segmentMapping = new ArrayList<>();

    public List<SegmentMapping> getSegmentMapping() {
        return segmentMapping;
    }

    public void setSegmentMapping(List<SegmentMapping> segmentMapping) {
        this.segmentMapping = segmentMapping;
    }

    /**
     * Looks up the mapping for a given Telraam segment ID.
     *
     * @param segmentId Telraam segment_id (long)
     * @return the mapping, or empty if not configured
     */
    public Optional<SegmentMapping> findBySegmentId(String segmentId) {
        return segmentMapping.stream()
                .filter(m -> m.getSegmentId() != null && m.getSegmentId().equals(segmentId))
                .findFirst();
    }

    public static class SegmentMapping {

        /** Telraam numeric segment_id. */
        private String segmentId;

        /** The DAVe Zählung UUID this segment's data should be stored under. */
        private UUID zaehlungId;

        /**
         * DAVe direction integer for A→B traffic (lft when sensor direction=1,
         * rgt when direction=0). See {@link de.starwit.telraam.dto.dave.DaveDirection}.
         */
        private Integer directionAtoB;

        /**
         * DAVe direction integer for B→A traffic (opposite of directionAtoB).
         */
        private Integer directionBtoA;

        public String getSegmentId() {
            return segmentId;
        }

        public void setSegmentId(String segmentId) {
            this.segmentId = segmentId;
        }

        public UUID getZaehlungId() {
            return zaehlungId;
        }

        public void setZaehlungId(UUID zaehlungId) {
            this.zaehlungId = zaehlungId;
        }

        public Integer getDirectionAtoB() {
            return directionAtoB;
        }

        public void setDirectionAtoB(Integer directionAtoB) {
            this.directionAtoB = directionAtoB;
        }

        public Integer getDirectionBtoA() {
            return directionBtoA;
        }

        public void setDirectionBtoA(Integer directionBtoA) {
            this.directionBtoA = directionBtoA;
        }

        @Override
        public String toString() {
            return "SegmentMapping [segmentId=" + segmentId + ", zaehlungId=" + zaehlungId + ", directionAtoB="
                    + directionAtoB + ", directionBtoA=" + directionBtoA + "]";
        }
    }
}
