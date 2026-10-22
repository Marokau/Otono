CREATE TABLE IF NOT EXISTS bot_kpq_companion
(
    bot_char_id INT NOT NULL PRIMARY KEY,
    CONSTRAINT fk_kpq_companion FOREIGN KEY (bot_char_id) REFERENCES characters (id) ON DELETE CASCADE
);
