package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for {@code POST /reports/traffic}.
 *
 * <p>Use {@code format = "per-15min"} to obtain 15-minute resolution counts
 * (requires the Telraam Advanced/Data subscription). Fall back to
 * {@code "per-hour"} if only the free tier is available.</p>
 */
public record TrafficRequest(
        @JsonProperty("id") String segmentId,
        @JsonProperty("time_start") String timeStart,
        @JsonProperty("time_end") String timeEnd,
        @JsonProperty("level") String level,
        @JsonProperty("format") String format
) {
    /** Convenience factory for a 15-minute window at segment level. */
    public static TrafficRequest forSegment(String segmentId, String timeStart, String timeEnd) {
        return new TrafficRequest(segmentId, timeStart, timeEnd, "segments", "per-15min");
    }
}
