package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from {@code POST /segments/area} endpoint.
 * Returns all segments within a geographic polygon (GeoJSON FeatureCollection).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SegmentsAreaResponse(
        @JsonProperty("status_code") Integer statusCode,
        @JsonProperty("message") String message,
        @JsonProperty("type") String type,
        @JsonProperty("features") List<SegmentFeature> features
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SegmentFeature(
            @JsonProperty("type") String type,
            @JsonProperty("geometry") Geometry geometry,
            @JsonProperty("properties") SegmentProperties properties
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            @JsonProperty("type") String type,
            @JsonProperty("coordinates") List<List<List<Double>>> coordinates
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SegmentProperties(
            @JsonProperty("segment_id") String segmentId
    ) {}
}
