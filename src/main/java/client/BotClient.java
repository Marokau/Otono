package client;

import net.packet.Packet;

/**
 * A headless client for bot/companion characters that have no real network connection.
 * sendPacket is a no-op, isLoggedIn always returns true, and updateLoginState/disconnect
 * skip the DB writes and SessionCoordinator operations that require a real session.
 */
public class BotClient extends Client {
    public volatile server.bots.BotNpcInteraction.Message npcMessage;

    public BotClient(int world, int channel) {
        super(null, -1, "bot", null, world, channel);
    }

    @Override
    public void sendPacket(Packet packet) {
        server.bots.BotGuildStatues.observe(getPlayer(), packet);
        if (getPlayer() != null && (server.bots.BotZakumBattle.inInstance(getPlayer()) || server.bots.BotEllinRun.inInstance(getPlayer()) || server.bots.BotGuildCompanions.inInstance(getPlayer()) || server.bots.BotMvpqRun.inInstance(getPlayer()) || server.bots.BotHorntailRun.inInstance(getPlayer()) || server.bots.BotPinkBeanRun.inInstance(getPlayer())))
            server.bots.BotMobAttackObserver.observe(getPlayer(), packet);
        if (getPlayer() != null && getPlayer().getMapId() == 922010800)
            server.bots.BotLpqPuzzle.observeEffect(getPlayer(), packet);
        if (getPlayer() != null && (getPlayer().getMapId() == 920010500 || getPlayer().getMapId() == 920010700))
            server.bots.BotOpqHuman.observeEffect(getPlayer(), packet);
        if (getCM() != null) {
            var message = server.bots.BotNpcInteraction.read(packet);
            if (message != null) npcMessage = message;
        }
    }

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public void updateLoginState(int newState) {
        // skip DB write and SessionCoordinator registration for bot clients
    }

    @Override
    public void disconnectSession() {
        // no-op: bot has no ioChannel — calling ioChannel.disconnect() would NPE
    }

    /**
     * Always report "just now" so TimeoutTask never considers this bot inactive.
     * Bots have no real network I/O and must never be auto-disconnected by the
     * inactivity checker.
     */
    @Override
    public long getLastPacket() {
        return System.currentTimeMillis();
    }
}
