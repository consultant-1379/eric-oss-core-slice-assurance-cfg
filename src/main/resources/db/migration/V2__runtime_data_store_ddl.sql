SET search_path TO "${runtimeDatastoreSchemaName}";

GRANT USAGE ON SCHEMA "${runtimeDatastoreSchemaName}" TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS rt_profile (
  name text constraint pk_prof_nm PRIMARY KEY,
  description text
);

ALTER TABLE IF EXISTS rt_profile OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS rt_prof_kpi (
  prof_name text,
  kpi_def_name text,
  PRIMARY KEY (prof_name, kpi_def_name),
  CONSTRAINT fk_prof_kpi
    FOREIGN KEY(kpi_def_name)
    REFERENCES ${dictionarySchemaName}.kpi_def (name)
    ON DELETE RESTRICT,
  CONSTRAINT fk_prof_kpi_prof
    FOREIGN KEY(prof_name)
    REFERENCES rt_profile (name)
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS rt_prof_kpi OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS rt_prof_agg (
  prof_name text CONSTRAINT pk_prof_agg PRIMARY KEY,
  agg_fields text[] NOT NULL CHECK (cardinality(agg_fields) > 0),
  CONSTRAINT fk_prof_agg
    FOREIGN KEY(prof_name)
    REFERENCES rt_profile (name)
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS rt_prof_agg OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS rt_kpi_inst (
  kpi_instance_id varchar(59) PRIMARY KEY,
  kpi_def_name text NOT NULL,
  agg_fields text[] NOT NULL CHECK (cardinality(agg_fields) > 0),
  pmsc_kpi_def json NOT NULL,
  UNIQUE (kpi_def_name, agg_fields),
  CONSTRAINT fk_kpi_inst_kpi
    FOREIGN KEY(kpi_def_name)
    REFERENCES ${dictionarySchemaName}.kpi_def (name)
    ON DELETE RESTRICT
);

ALTER TABLE IF EXISTS rt_kpi_inst OWNER TO "${databaseUser}";
