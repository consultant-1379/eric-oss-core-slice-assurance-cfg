SET search_path TO "${runtimeDatastoreSchemaName}";
CREATE TABLE IF NOT EXISTS rt_idx_def (
   idx_name text CONSTRAINT pk_rt_idx PRIMARY KEY,
   idx_def json NOT NULL
);

ALTER TABLE IF EXISTS rt_idx_def
  OWNER TO "${databaseUser}";
