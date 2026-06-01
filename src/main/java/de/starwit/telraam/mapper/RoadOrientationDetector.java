package de.starwit.telraam.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import de.starwit.telraam.dto.dave.DaveDirection;
import de.starwit.telraam.dto.telraam.SegmentInstancesResponse.SegmentGeometry;

/**
 * Derives the principal compass orientation of a road segment from its
 * GeoJSON MultiLineString geometry and maps it to a pair of DAVe direction
 * integers ({@link DaveDirection}).
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Flatten all line-strings in the MultiLineString into one ordered
 *       sequence of [lon, lat] coordinate pairs.</li>
 *   <li>Compute the net displacement vector from the first to the last
 *       coordinate: {@code Δlon} (east–west) and {@code Δlat} (north–south).
 *       Using the endpoints rather than a running sum is deliberately simple:
 *       most Telraam segments are short, straight road sections where the
 *       first-to-last vector is a reliable proxy for the road axis.</li>
 *   <li>Compare |Δlat| vs |Δlon| (scaled for the local latitude) to decide
 *       whether the dominant axis is N–S or E–W.</li>
 *   <li>Within each axis, the sign of the displacement picks the "A→B"
 *       direction (the direction in which coordinates increase along the
 *       dominant axis).  "B→A" is always the opposite.</li>
 * </ol>
 *
 * <h2>Scaling</h2>
 * One degree of latitude ≈ 111 km everywhere.
 * One degree of longitude ≈ 111 km × cos(lat) — shrinks toward the poles.
 * We apply this correction so that the comparison is in metres, not degrees.
 *
 * <h2>Limitations</h2>
 * Diagonal roads (e.g. 45°) are snapped to the nearest cardinal axis.
 * If the segment is essentially a single point (start ≈ end), the method
 * falls back to N–S.  Curved roads are represented by their chord vector.
 */
@Component
public class RoadOrientationDetector {

    /** Below this threshold (metres) the segment is treated as a point. */
    private static final double MIN_LENGTH_METRES = 1.0;

    /**
     * Result of orientation detection: a pair of DAVe direction integers
     * representing the two traffic-flow directions on this segment.
     *
     * @param directionAtoB DAVe direction integer for A→B (first→last coord)
     * @param directionBtoA DAVe direction integer for B→A (last→first coord)
     */
    public record OrientationResult(int directionAtoB, int directionBtoA) {

        @Override
        public String toString() {
            return "OrientationResult{A→B=" + directionAtoB +
                   " (" + DaveDirection.name(directionAtoB) + ")" +
                   ", B→A=" + directionBtoA +
                   " (" + DaveDirection.name(directionBtoA) + ")}";
        }
    }

    /**
     * Detects the road orientation from a GeoJSON {@link SegmentGeometry}.
     *
     * @param geometry the MultiLineString geometry from the instances API
     * @return the orientation result, never {@code null}
     * @throws IllegalArgumentException if the geometry contains no usable coordinates
     */
    public OrientationResult detect(SegmentGeometry geometry) {
        List<double[]> points = flattenCoordinates(geometry);

        if (points.size() < 2) {
            throw new IllegalArgumentException(
                    "Geometry must contain at least 2 coordinate pairs, got " + points.size());
        }

        double[] first = points.get(0);
        double[] last  = points.get(points.size() - 1);

        double lon0 = first[0], lat0 = first[1];
        double lon1 = last[0],  lat1 = last[1];

        // Convert degree differences to approximate metres
        double midLat = Math.toRadians((lat0 + lat1) / 2.0);
        double deltaLatM  = (lat1 - lat0) * 111_320.0;                     // metres N–S
        double deltaLonM  = (lon1 - lon0) * 111_320.0 * Math.cos(midLat);  // metres E–W

        double absLat = Math.abs(deltaLatM);
        double absLon = Math.abs(deltaLonM);

        if (absLat < MIN_LENGTH_METRES && absLon < MIN_LENGTH_METRES) {
            // Degenerate segment – fall back to N–S
            return new OrientationResult(DaveDirection.NORTH, DaveDirection.SOUTH);
        }

        // Determine if the road is more diagonal (∈ [22.5°, 67.5°]) or cardinal
        // Using ratio threshold: ~0.414 ≈ tan(22.5°), ~2.414 ≈ tan(67.5°)
        double ratio = absLat > 0 ? absLon / absLat : Double.POSITIVE_INFINITY;
        boolean isDiagonal = ratio > 0.414 && ratio < 2.414;

        if (isDiagonal) {
            // Diagonal direction: northeast, southeast, southwest, northwest
            boolean headingNorth = deltaLatM >= 0;
            boolean headingEast  = deltaLonM >= 0;

            if (headingNorth && headingEast) {
                return new OrientationResult(DaveDirection.NORTHEAST, DaveDirection.SOUTHWEST);
            } else if (headingNorth && !headingEast) {
                return new OrientationResult(DaveDirection.NORTHWEST, DaveDirection.SOUTHEAST);
            } else if (!headingNorth && headingEast) {
                return new OrientationResult(DaveDirection.SOUTHEAST, DaveDirection.NORTHWEST);
            } else {
                return new OrientationResult(DaveDirection.SOUTHWEST, DaveDirection.NORTHEAST);
            }
        } else if (absLat >= absLon) {
            // Dominant axis: N–S
            return deltaLatM >= 0
                    ? new OrientationResult(DaveDirection.NORTH, DaveDirection.SOUTH)
                    : new OrientationResult(DaveDirection.SOUTH, DaveDirection.NORTH);
        } else {
            // Dominant axis: E–W
            return deltaLonM >= 0
                    ? new OrientationResult(DaveDirection.EAST, DaveDirection.WEST)
                    : new OrientationResult(DaveDirection.WEST, DaveDirection.EAST);
        }
    }

    /**
     * Flattens all line-strings of a MultiLineString into a single ordered
     * list of {@code [longitude, latitude]} pairs.
     */
    private List<double[]> flattenCoordinates(SegmentGeometry geometry) {
        if (geometry == null || geometry.coordinates() == null) {
            throw new IllegalArgumentException("Geometry or coordinates must not be null");
        }

        return geometry.coordinates().stream()
                .flatMap(List::stream)
                .map(pair -> new double[]{ pair.get(0), pair.get(1) })
                .toList();
    }
}

