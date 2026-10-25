package server.partyquest;

import client.Character;
import server.maps.MapleMap;
import java.awt.Point;

/** Normal MVPQ entrance and pit-reset spawn, reachable by every class. */
public final class MvpqEntry {
    private MvpqEntry() {}
    public static Point startingPosition() { return new Point(900, 245); }
    public static void enterCourse(Character player, MapleMap course) {
        if (course.getId() != 674030000) throw new IllegalArgumentException("Not the MVPQ course");
        player.changeMap(course, startingPosition());
    }
}
