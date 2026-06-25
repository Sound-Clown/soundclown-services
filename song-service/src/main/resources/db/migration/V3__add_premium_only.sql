ALTER TABLE songs
    ADD COLUMN premium_only BIT(1) NOT NULL DEFAULT 0;

-- Mark a scattered ~10% of the seeded catalog as premium-only for the demo.
UPDATE songs SET premium_only = 1 WHERE id % 10 = 0;
