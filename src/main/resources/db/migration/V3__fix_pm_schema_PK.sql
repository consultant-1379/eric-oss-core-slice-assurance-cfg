SET search_path TO "${dictionarySchemaName}";

ALTER TABLE pm_schema DROP CONSTRAINT pm_schema_pkey;
ALTER TABLE pm_schema ADD PRIMARY KEY (pm_name);
