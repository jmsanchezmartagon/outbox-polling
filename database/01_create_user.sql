alter session set "_oracle_script"=true;

CREATE TABLESPACE JAMBO DATAFILE
  '/opt/oracle/oradata/XE/JAMBO_001.dbf' SIZE 50M AUTOEXTEND OFF
LOGGING
ONLINE
PERMANENT
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
BLOCKSIZE 8K
SEGMENT SPACE MANAGEMENT AUTO
FLASHBACK OFF;


ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_002.dbf' size 100M;
ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_003.dbf' size 100M;
ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_004.dbf' size 100M;
ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_005.dbf' size 100M;


ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_006.dbf' size 100M;
ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_007.dbf' size 100M;
ALTER TABLESPACE JAMBO ADD DATAFILE '/opt/oracle/oradata/XE/JAMBO_008.dbf' size 100M;

CREATE USER JAMBO IDENTIFIED BY "JAMBO"
       DEFAULT TABLESPACE JAMBO
       TEMPORARY TABLESPACE TEMP
         ACCOUNT UNLOCK;
GRANT CONNECT TO JAMBO;
GRANT RESOURCE TO JAMBO;
GRANT CREATE ANY TYPE TO JAMBO;
ALTER USER JAMBO DEFAULT ROLE ALL;
GRANT UNLIMITED TABLESPACE TO JAMBO;