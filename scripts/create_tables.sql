\c drugs

DROP TABLE IF EXISTS fass_drugs;
DROP TABLE IF EXISTS fass_effects;
DROP TABLE IF EXISTS fass_substances;


CREATE TABLE fass_substances (
  id        INT PRIMARY KEY,
  substance TEXT  -- yes, TEXT, because some substances are enormous or consist of repeating elements
);

CREATE TABLE fass_effects (
  fass_substances_id    INT REFERENCES fass_substances (id) ON DELETE CASCADE PRIMARY KEY,
  interactions     TEXT,
  side_effects     TEXT
);

CREATE TABLE fass_drugs (
  id               VARCHAR(255) PRIMARY KEY,     -- this is FASS' nplId
  fass_substances_id    INT REFERENCES fass_substances (id) ON DELETE CASCADE,
  atc              VARCHAR(255),  -- One substance can have multiple ATC codes
  tradename        TEXT,
  form             TEXT    -- form contains strength as well
);
CREATE INDEX tradename_idx ON fass_drugs (tradename);
CREATE INDEX form_idx ON fass_drugs (form);