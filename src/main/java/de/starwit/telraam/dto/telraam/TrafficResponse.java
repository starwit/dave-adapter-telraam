package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Top-level response wrapper for {@code POST /reports/traffic}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrafficResponse(
        @JsonProperty("report") List<TrafficRecord> report,
        @JsonProperty("status_code") Integer statusCode,
        @JsonProperty("message") String message
) {}
