package de.starwit.telraam.dto.dave;

/**
 * DAVe compass-direction integer codes as used in {@link DetectionDTO#getVon()}
 * and {@link DetectionDTO#getNach()}.
 *
 * <p>The values match the direction diagram published in the dave-adapter-telraam
 * README (top = North):</p>
 * <pre>
 *        1 (N)
 *        |
 *  4(W)──┼──3(E)
 *        |
 *        2 (S)
 * </pre>
 *
 * For a simple two-way road (no intersection), only directions 1 and 2 (or
 * 3 and 4) are used. The actual mapping for each Telraam segment must be
 * configured via {@code telraam.segment-mapping[*].direction-a} and
 * {@code telraam.segment-mapping[*].direction-b}.
 */
public final class DaveDirection {

    public static final int NORTH = 1;
    public static final int SOUTH = 2;
    public static final int EAST  = 3;
    public static final int WEST  = 4;
    // Legs 5–8 exist for complex intersections; add as needed.

    /** Returns a human-readable label for a direction integer. */
    public static String name(int direction) {
        return switch (direction) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST  -> "E";
            case WEST  -> "W";
            default    -> "leg-" + direction;
        };
    }

    private DaveDirection() {}
}
