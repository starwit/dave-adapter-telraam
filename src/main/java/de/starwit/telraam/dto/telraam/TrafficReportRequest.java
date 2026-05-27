package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrafficReportRequest(
        @JsonProperty("id") String segmentId,
        @JsonProperty("time_start") String timeStart,
        @JsonProperty("time_end") String timeEnd,
        @JsonProperty("level") String level,
        @JsonProperty("format") String format
) {
    /** Convenience factory for a 15-minute window at segment level. */
    public static TrafficReportRequest forSegment(String segmentId, String timeStart, String timeEnd) {
        return new TrafficReportRequest(segmentId, timeStart, timeEnd, "segments", "per-quarter");
    }
}
