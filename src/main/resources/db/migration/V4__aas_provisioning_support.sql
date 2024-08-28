SET search_path TO "${dictionarySchemaName}";
CREATE TABLE IF NOT EXISTS aug_def (
   name text CONSTRAINT pk_aug PRIMARY KEY,
   def json NOT NULL
);

ALTER TABLE IF EXISTS aug_def OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS prof_def (
    name text CONSTRAINT pk_prof PRIMARY KEY,
    def json NOT NULL
);

ALTER TABLE IF EXISTS prof_def OWNER TO "${databaseUser}";

INSERT INTO prof_def (name, def) SELECT t.name, t.profile FROM
(SELECT j.name, row_to_json(j) AS profile FROM
(SELECT p.name, p.description, a.agg_fields, json_agg(json_build_object('ref',k.kpi_def_name)) AS kpis
FROM ${runtimeDatastoreSchemaName}.rt_profile p, ${runtimeDatastoreSchemaName}.rt_prof_kpi k, ${runtimeDatastoreSchemaName}.rt_prof_agg a
WHERE name=k.prof_name AND name=a.prof_name
GROUP BY name, a.agg_fields) j) t;


SET search_path TO "${runtimeDatastoreSchemaName}";
CREATE TABLE IF NOT EXISTS rt_aug (
  name text CONSTRAINT pk_rt_aug PRIMARY KEY,
  def json NOT NULL,
  CONSTRAINT fk_rt_aug
     FOREIGN KEY(name)
     REFERENCES ${dictionarySchemaName}.aug_def (name)
     ON DELETE CASCADE
);

ALTER TABLE IF EXISTS rt_aug OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS rt_prof_aug (
  prof_name  text CONSTRAINT pk_prof_aug PRIMARY KEY,
  aug_name text NOT NULL,
  CONSTRAINT fk_prof_aug_prof
     FOREIGN KEY(prof_name)
     REFERENCES ${dictionarySchemaName}.prof_def (name)
     ON DELETE CASCADE,
  CONSTRAINT fk_prof_aug_aug
       FOREIGN KEY(aug_name)
       REFERENCES rt_aug (name)
       ON DELETE CASCADE
);

ALTER TABLE IF EXISTS rt_prof_aug OWNER TO "${databaseUser}";
