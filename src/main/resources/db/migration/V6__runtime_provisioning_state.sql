SET search_path TO "${runtimeDatastoreSchemaName}";

CREATE TABLE IF NOT EXISTS rt_prov_state (
  id serial primary key, 
  provisioning_start_time timestamp(3) with time zone,
  provisioning_end_time timestamp(3) with time zone,
  provisioning_state varchar(10));

ALTER TABLE IF EXISTS rt_prov_state
  OWNER TO "${databaseUser}";

INSERT INTO rt_prov_state (provisioning_start_time, provisioning_end_time, provisioning_state) 
  SELECT current_timestamp, current_timestamp, 'INITIAL'
  WHERE NOT EXISTS(SELECT * FROM rt_prov_state);
