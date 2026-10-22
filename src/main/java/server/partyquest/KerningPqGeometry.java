package server.partyquest;

import java.awt.Rectangle;

/** Placement regions shared by Cloto's validator and the headless clients. */
public final class KerningPqGeometry {
    private KerningPqGeometry() {}

    public static Rectangle[] areas(int stage) {
        return switch (stage) {
            case 2 -> new Rectangle[]{new Rectangle(-755, -132, 4, 218), new Rectangle(-721, -340, 4, 166),
                    new Rectangle(-586, -326, 4, 150), new Rectangle(-483, -181, 4, 222)};
            case 3 -> new Rectangle[]{new Rectangle(608, -180, 140, 50), new Rectangle(791, -117, 140, 45),
                    new Rectangle(958, -180, 140, 50), new Rectangle(876, -238, 140, 45), new Rectangle(702, -238, 140, 45)};
            case 4 -> new Rectangle[]{new Rectangle(910, -236, 35, 5), new Rectangle(877, -184, 35, 5),
                    new Rectangle(946, -184, 35, 5), new Rectangle(845, -132, 35, 5),
                    new Rectangle(910, -132, 35, 5), new Rectangle(981, -132, 35, 5)};
            default -> throw new IllegalArgumentException("Not a KPQ placement stage: " + stage);
        };
    }
}
