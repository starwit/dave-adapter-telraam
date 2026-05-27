package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request body for {@code POST /segments/area} endpoint.
 * Uses GeoJSON Polygon geometry to query segments within a geographic area.
 */
public record SegmentsAreaRequest(
        @JsonProperty("polygon") Polygon polygon
) {

    public record Polygon(
            @JsonProperty("type") String type,
            @JsonProperty("coordinates") List<List<List<Double>>> coordinates
    ) {
        public Polygon(List<List<List<Double>>> coordinates) {
            this("Polygon", coordinates);
        }
    }

    /**
     * Creates a SegmentsAreaRequest for a rectangular bounding box.
     *
     * @param minLon minimum longitude
     * @param minLat minimum latitude
     * @param maxLon maximum longitude
     * @param maxLat maximum latitude
     * @return request with polygon covering the bbox
     */
    public static SegmentsAreaRequest fromBoundingBox(double minLon, double minLat, double maxLon, double maxLat) {
        // Create a rectangle polygon: [maxLon, maxLat] → [minLon, maxLat] → [minLon, minLat] → [maxLon, minLat] → close
        List<List<Double>> ring = List.of(
                List.of(maxLon, maxLat),
                List.of(minLon, maxLat),
                List.of(minLon, minLat),
                List.of(maxLon, minLat),
                List.of(maxLon, maxLat)  // close the ring
        );
        return new SegmentsAreaRequest(new Polygon(List.of(ring)));
    }
}
