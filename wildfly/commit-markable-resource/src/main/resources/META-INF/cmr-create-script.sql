CREATE TABLE xids (xid bytea, transactionManagerID varchar(64), actionuid bytea);
CREATE UNIQUE INDEX index_xid ON xids (xid);