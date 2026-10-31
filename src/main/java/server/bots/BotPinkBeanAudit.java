package server.bots;

import scripting.event.EventInstanceManager;
import server.life.Monster;
import java.nio.file.*;
import java.time.Instant;

/** Read-only, local expedition result snapshots; never drives combat. */
public final class BotPinkBeanAudit {
    private BotPinkBeanAudit() {}
    public static synchronized void record(EventInstanceManager event, String outcome) {
        try {
            String started = event.getProperty("otonoAuditStarted");
            if (started == null) return;
            long elapsed = (System.currentTimeMillis() - Long.parseLong(started)) / 1000;
            var monsters = new StringBuilder();
            for (var object : event.getMapInstance(270050100).getMonsters()) {
                if (object instanceof Monster mob && mob.getId() >= 8820001 && mob.getId() <= 8820006)
                    monsters.append(mob.getId()).append(':').append(mob.getHp()).append(' ');
            }
            long alive = event.getPlayers().stream().filter(p -> p.isAlive()).count();
            String line = Instant.now() + " run=" + started + " outcome=" + outcome
                    + " elapsedSeconds=" + elapsed + " wave=" + event.getProperty("stage")
                    + " deaths=" + event.getIntProperty("fallenPlayers") + " alive=" + alive
                    + " bossHP=[" + monsters.toString().trim() + "]" + System.lineSeparator();
            Files.createDirectories(Path.of("logs"));
            Files.writeString(Path.of("logs", "pinkbean-audit.log"), line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception error) {
            org.slf4j.LoggerFactory.getLogger(BotPinkBeanAudit.class).warn("Pink Bean audit snapshot failed", error);
        }
    }
}
