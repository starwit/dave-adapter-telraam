package de.starwit.telraam.dto.telraam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * One aggregated traffic row returned by the Telraam traffic report endpoint.
 *
 * <p>Counts are split into {@code lft} (left-going) and {@code rgt}
 * (right-going) from the sensor's point of view. The {@code direction} field
 * on the parent segment/instance tells us how lft/rgt map to A→B in
 * geographical terms (see {@code TrafficDirectionMapper}).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrafficRecord(

        /** Start timestamp of the aggregation window (ISO-8601 with offset). */
        @JsonProperty("date") OffsetDateTime date,

        /** Segment or instance identifier. */
        @JsonProperty("segment_id") Long segmentId,

        /**
         * Camera orientation flag:
         * <ul>
         *   <li>1 = "True" side – lft maps to A→B</li>
         *   <li>0 = opposite side – rgt maps to A→B</li>
         * </ul>
         */
        @JsonProperty("direction") Integer direction,

        // ----- pedestrians -----
        @JsonProperty("pedestrian") Double pedestrian,
        @JsonProperty("pedestrian_lft") Double pedestrianLft,
        @JsonProperty("pedestrian_rgt") Double pedestrianRgt,

        // ----- bicycles -----
        @JsonProperty("bike") Double bike,
        @JsonProperty("bike_lft") Double bikeLft,
        @JsonProperty("bike_rgt") Double bikeRgt,

        // ----- motorised 2-wheel -----
        @JsonProperty("motorbike") Double motorbike,
        @JsonProperty("motorbike_lft") Double motorbikeLft,
        @JsonProperty("motorbike_rgt") Double motorbikeRgt,

        // ----- cars -----
        @JsonProperty("car") Double car,
        @JsonProperty("car_lft") Double carLft,
        @JsonProperty("car_rgt") Double carRgt,

        // ----- heavy vehicles -----
        @JsonProperty("heavy") Double heavy,
        @JsonProperty("heavy_lft") Double heavyLft,
        @JsonProperty("heavy_rgt") Double heavyRgt,

        /** Fraction of the interval during which the sensor was operational (0–1). */
        @JsonProperty("uptime") Double uptime,

        /** v85 speed in km/h (cars only, may be null). */
        @JsonProperty("v85") Double v85
) {}
