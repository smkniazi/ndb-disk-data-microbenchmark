delimiter $$
CREATE TABLE IF NOT EXISTS `test` ( `id` bigint(20) NOT NULL, `data` VARBINARY(8000) NOT NULL, PRIMARY KEY (`id`))  TABLESPACE ts_1 STORAGE DISK ENGINE NDBCLUSTER;$$
delimiter $$
