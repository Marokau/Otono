CREATE TABLE IF NOT EXISTS bot_pq_companion
(
    bot_char_id INT NOT NULL PRIMARY KEY,
    pq VARCHAR(16) NOT NULL,
    CONSTRAINT fk_pq_companion FOREIGN KEY (bot_char_id) REFERENCES characters (id) ON DELETE CASCADE
);

INSERT IGNORE INTO bot_pq_companion (bot_char_id, pq)
SELECT c.id, 'opq' FROM characters c JOIN accounts a ON a.id=c.accountid
WHERE c.world=0 AND c.gm=0 AND a.name=c.name
  AND c.name IN ('opmSpear1','opmCleric1','opmMage1','opmThief1','opmArcher1','opmSword1')
  AND NOT EXISTS (SELECT 1 FROM managed_bot m WHERE m.bot_char_id=c.id)
  AND NOT EXISTS (SELECT 1 FROM bot_owners o WHERE o.bot_char_id=c.id);
