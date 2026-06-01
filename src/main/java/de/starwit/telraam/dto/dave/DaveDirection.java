package de.starwit.telraam.dto.dave;

/**
 * DAVe compass-direction integer codes as used in {@link DetectionDTO#getVon()}
 * and {@link DetectionDTO#getNach()}.
 *
 * <pre>
 *      8(NW) 1(N) 5(NE)
 *        \   |   /
 *    4(W) ──┼── 2(E)
 *        /   |   \
 *      7(SW) 3(S) 6(SE)
 * </pre>
 *
 * For a simple two-way road (no intersection), opposite directions are used
 * (e.g., 1+3, 2+4, or 5+7, 6+8). The actual mapping for each Telraam segment
 * must be configured via {@code telraam.segment-mapping[*].direction-a} and
 * {@code telraam.segment-mapping[*].direction-b}.
 */
public final class DaveDirection {

    public static final int NORTH     = 1;
    public static final int EAST      = 2;
    public static final int SOUTH     = 3;
    public static final int WEST      = 4;
    public static final int NORTHEAST = 5;
    public static final int SOUTHEAST = 6;
    public static final int SOUTHWEST = 7;
    public static final int NORTHWEST = 8;

    /** Returns a human-readable label for a direction integer. */
    public static String name(int direction) {
        return switch (direction) {
            case NORTH     -> "N";
            case EAST      -> "E";
            case SOUTH     -> "S";
            case WEST      -> "W";
            case NORTHEAST -> "NE";
            case SOUTHEAST -> "SE";
            case SOUTHWEST -> "SW";
            case NORTHWEST -> "NW";
            default        -> "leg-" + direction;
        };
    }

    private DaveDirection() {}
}
