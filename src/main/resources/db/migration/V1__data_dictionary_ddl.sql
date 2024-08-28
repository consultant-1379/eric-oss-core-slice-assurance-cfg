SET search_path TO "${dictionarySchemaName}";

GRANT USAGE ON SCHEMA "${dictionarySchemaName}" TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS pm_def (
  name text CONSTRAINT pk_pm PRIMARY KEY,
  source text NOT NULL,
  description text
);

ALTER TABLE IF EXISTS pm_def OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS pm_schema (
  schema text NOT NULL,
  pm_name text NOT NULL,
  PRIMARY KEY (schema, pm_name),
  CONSTRAINT fk_pm_name
    FOREIGN KEY(pm_name)
    REFERENCES pm_def(name)
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS pm_schema OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS kpi_def (
  name text CONSTRAINT pk_kpi PRIMARY KEY,
  description text,
  display_name text,
  expression text NOT NULL,
  aggregation_type text NOT NULL,
  is_visible boolean NOT NULL DEFAULT true
);

ALTER TABLE IF EXISTS kpi_def OWNER TO "${databaseUser}";

CREATE TYPE inp_metric_type AS ENUM ('pm_data', 'kpi');

CREATE TABLE IF NOT EXISTS kpi_input_metric (
  kpi_name text NOT NULL,
  id text NOT NULL,
  alias text,
  inp_type inp_metric_type NOT NULL,
  PRIMARY KEY (kpi_name, id),
  CONSTRAINT fk_kpi_inp_metric
    FOREIGN KEY(kpi_name)
    REFERENCES kpi_def(name)
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS kpi_input_metric OWNER TO "${databaseUser}";
