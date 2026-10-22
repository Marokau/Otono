CREATE TABLE IF NOT EXISTS bot_lpq_companion
(
    bot_char_id INT NOT NULL PRIMARY KEY,
    CONSTRAINT fk_lpq_companion FOREIGN KEY (bot_char_id) REFERENCES characters (id) ON DELETE CASCADE
);

-- Adopt the owner's existing six test fixtures. Never enroll population or owned bots.
INSERT IGNORE INTO bot_lpq_companion (bot_char_id)
SELECT c.id FROM characters c JOIN accounts a ON a.id = c.accountid
WHERE c.world = 0 AND c.gm = 0 AND a.name = c.name
  AND c.name IN ('lpmSpear1', 'lpmCleric1', 'lpmMage1', 'lpmThief1', 'lpmArcher1', 'lpmSword1')
  AND NOT EXISTS (SELECT 1 FROM managed_bot m WHERE m.bot_char_id = c.id)
  AND NOT EXISTS (SELECT 1 FROM bot_owners o WHERE o.bot_char_id = c.id);
