package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from {@code POST /cameras/active} (snapshot/bounding-box endpoint).
 * Used for auto-detecting all sensors within a geo-fenced region.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveCamerasResponse(
        @JsonProperty("features") List<CameraFeature> features
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CameraFeature(
            @JsonProperty("type") String type,
            @JsonProperty("properties") CameraProperties properties
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CameraProperties(
            @JsonProperty("segment_id") Long segmentId,
            @JsonProperty("instance_id") Long instanceId,
            @JsonProperty("direction") Integer direction,
            @JsonProperty("mac") String mac,
            @JsonProperty("last_data_package") String lastDataPackage
    ) {}
}
