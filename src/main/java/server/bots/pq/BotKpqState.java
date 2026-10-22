package server.bots.pq;

/** Mutable state bag for KPQ bot automation. One instance per bot, held in BotEntry. */
public final class BotKpqState {
    public int mapId = -1;
    public boolean recruiting;
    public boolean humanLed;
    public int leaderId;
    public java.util.List<Integer> crewIds = java.util.List.of();
    // Stage 1
    public int   state               = 0;
    public int   couponTarget        = -1;
    public long  waitUntilMs         = 0;
    public int   lastReportedCoupons = 0;
    // Stage 5
    public boolean stage5Claimed     = false;
}
