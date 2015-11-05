# --- !Ups

CREATE TABLE IF NOT EXISTS Language (
  id   BIGSERIAL    NOT NULL,
  code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Category (
  id          BIGSERIAL                NOT NULL,
  description TEXT,
  --- created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Category_Name (
  id          BIGSERIAL    NOT NULL,
  name        VARCHAR(255) NOT NULL,
  name_search VARCHAR(255) NOT NULL,
  language_id BIGINT REFERENCES Language (id),
  category_id BIGINT       NOT NULL REFERENCES Category (id),
  PRIMARY KEY (id)
);
--- CREATE INDEX category_name_idx ON Category_Name USING HASH (name);
CREATE INDEX category_name_idx ON Category_Name (name);

CREATE TABLE IF NOT EXISTS Dimension (
  id          BIGSERIAL                NOT NULL,
  description TEXT,
  --- created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Dimension_Name (
  id           BIGSERIAL    NOT NULL,
  name         VARCHAR(255) NOT NULL,
  name_search  VARCHAR(255) NOT NULL,
  language_id  BIGINT REFERENCES Language (id),
  dimension_id BIGINT       NOT NULL REFERENCES Dimension (id),
  PRIMARY KEY (id)
);
--- CREATE INDEX dimension_name_idx ON Dimension_Name USING HASH (name);
CREATE INDEX dimension_name_idx ON Dimension_Name (name);

CREATE TABLE IF NOT EXISTS Dimension_Relation_Type (
  id   BIGSERIAL    NOT NULL,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
--- CREATE INDEX relation_type_name_idx ON Dimension_Relation_Type USING HASH (name);
CREATE INDEX relation_type_name_idx ON Dimension_Relation_Type (name);

CREATE TABLE IF NOT EXISTS Dimension_Relation (
  parent_id        BIGINT NOT NULL REFERENCES Dimension (id),
  child_id         BIGINT NOT NULL REFERENCES Dimension (id),
  relation_type_id BIGINT REFERENCES Dimension_Relation_Type (id),
  PRIMARY KEY (parent_id, child_id)
);


CREATE TABLE IF NOT EXISTS Unit (
  id   BIGSERIAL    NOT NULL,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS OOI (
  id          BIGSERIAL                NOT NULL,
  description TEXT,
  --- created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  unit_id     BIGINT                   NOT NULL REFERENCES Unit (id),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS OOI_Name (
  id          BIGSERIAL    NOT NULL,
  name        VARCHAR(255) NOT NULL,
  name_search VARCHAR(255) NOT NULL,
  language_id BIGINT REFERENCES Language (id),
  ooi_id      BIGINT       NOT NULL REFERENCES OOI (id),
  PRIMARY KEY (id)
);
--- CREATE INDEX ooi_name_idx ON OOI_Name USING HASH (name);
CREATE INDEX ooi_name_idx ON OOI_Name (name);

CREATE TABLE IF NOT EXISTS Fact (
  id         BIGSERIAL                NOT NULL,
  ooi_id     BIGINT                   NOT NULL REFERENCES OOI (id),
  at         TIMESTAMP,
  valueInt   BIGINT,
  valueFloat DOUBLE PRECISION,
  --- created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT has_one_value CHECK ((valueInt IS NOT NULL AND valueFloat IS NULL) OR
                                  (valueInt IS NULL AND valueFloat IS NOT NULL)),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Fact_Dimension (
  fact_id      BIGINT NOT NULL REFERENCES Fact (id),
  dimension_id BIGINT NOT NULL REFERENCES Dimension (id),
  PRIMARY KEY (fact_id, dimension_id)
);


CREATE TABLE Source (
  id           BIGSERIAL    NOT NULL,
  name         VARCHAR(255) NOT NULL,
  author_name  VARCHAR(255),
  author_email VARCHAR(255),
  is_verified  BOOLEAN      NOT NULL,
  description  TEXT,
  url          TEXT,
  score        INT          NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Fact_Source (
  fact_id   BIGINT NOT NULL REFERENCES Fact (id),
  source_id BIGINT NOT NULL REFERENCES Source (id),
  PRIMARY KEY (fact_id, source_id)
);

CREATE TABLE IF NOT EXISTS Dimension_Category (
  dimension_id BIGINT NOT NULL REFERENCES Dimension (id),
  category_id  BIGINT NOT NULL REFERENCES Category (id),
  PRIMARY KEY (dimension_id, category_id)
);


CREATE TABLE IF NOT EXISTS Queries (
  id BIGSERIAL NOT NULL,
  query  VARCHAR(255) NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  created_by  VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);


# --- !Downs

DROP TABLE IF EXISTS Dimension_Category CASCADE;
DROP TABLE IF EXISTS Fact_Source CASCADE;
DROP TABLE IF EXISTS Source CASCADE;
DROP TABLE IF EXISTS Fact_Dimension CASCADE;
DROP TABLE IF EXISTS Fact CASCADE;
DROP TABLE IF EXISTS OOI_Name CASCADE;
DROP TABLE IF EXISTS OOI CASCADE;
DROP TABLE IF EXISTS Unit CASCADE;
DROP TABLE IF EXISTS Dimension_Relation CASCADE;
DROP TABLE IF EXISTS Dimension_Name CASCADE;
DROP TABLE IF EXISTS Dimension CASCADE;
DROP TABLE IF EXISTS Category CASCADE;
DROP TABLE IF EXISTS Category_Name CASCADE;
DROP TABLE IF EXISTS Dimension_Relation_Type CASCADE;

DROP INDEX IF EXISTS relation_type_name_idx;