package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from {@code GET /v1/instances/segment/{segment_id}}.
 *
 * <p>
 * Returns a GeoJSON FeatureCollection where each feature carries the
 * segment geometry (MultiLineString) and a map of active camera instances.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SegmentInstancesResponse(
                @JsonProperty("status_code") Integer statusCode,
                @JsonProperty("message") String message,
                @JsonProperty("features") List<SegmentFeature> features) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SegmentFeature(
                        @JsonProperty("geometry") SegmentGeometry geometry,
                        @JsonProperty("properties") SegmentProperties properties) {
        }

        /**
         * GeoJSON MultiLineString geometry.
         * {@code coordinates} is a list of line-strings; each line-string is a
         * list of [lon, lat] pairs.
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SegmentGeometry(
                        @JsonProperty("type") String type,
                        /** coordinates[lineIndex][pointIndex] = [longitude, latitude] */
                        @JsonProperty("coordinates") List<List<List<Double>>> coordinates) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SegmentProperties(
                        @JsonProperty("oidn") Long oidn
        // instance_ids omitted – not needed for orientation detection
        ) {
        }
}
