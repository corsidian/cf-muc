--   $Revision: 1650 $
--   $Date: 2005-07-20 00:18:17 -0300 (Wed, 20 Jul 2005) $

-- MUC tables

CREATE TABLE ofEmiMucService (
  serviceID           INTEGER       NOT NULL,
  subdomain           VARCHAR(255)  NOT NULL,
  description         VARCHAR(255),
  isHidden            INTEGER       NOT NULL,
  CONSTRAINT ofEmiMucService_pk PRIMARY KEY (subdomain)
);
CREATE INDEX ofEmiMucService_serviceid_idx ON ofEmiMucService(serviceID);

CREATE TABLE ofEmiMucServiceProp (
  serviceID           INTEGER       NOT NULL,
  name                VARCHAR(100)  NOT NULL,
  propValue           VARCHAR(2000) NOT NULL,
  CONSTRAINT ofEmiMucSrvProp_pk PRIMARY KEY (serviceID, name)
);

CREATE TABLE ofEmiMucRoom (
  serviceID           INTEGER       NOT NULL,
  roomID              INTEGER       NOT NULL,
  creationDate        CHAR(15)      NOT NULL,
  modificationDate    CHAR(15)      NOT NULL,
  name                VARCHAR(50)   NOT NULL,
  naturalName         VARCHAR(255)  NOT NULL,
  description         VARCHAR(255),
  lockedDate          CHAR(15)      NOT NULL,
  emptyDate           CHAR(15),
  canChangeSubject    INTEGER       NOT NULL,
  maxUsers            INTEGER       NOT NULL,
  publicRoom          INTEGER       NOT NULL,
  moderated           INTEGER       NOT NULL,
  membersOnly         INTEGER       NOT NULL,
  canInvite           INTEGER       NOT NULL,
  roomPassword        VARCHAR(50),
  canDiscoverJID      INTEGER       NOT NULL,
  logEnabled          INTEGER       NOT NULL,
  subject             VARCHAR(100),
  rolesToBroadcast    INTEGER       NOT NULL,
  useReservedNick     INTEGER       NOT NULL,
  canChangeNick       INTEGER       NOT NULL,
  canRegister         INTEGER       NOT NULL,
  CONSTRAINT ofEmiMucRoom_pk PRIMARY KEY (serviceID, name)
);
CREATE INDEX ofEmiMucRoom_roomid_idx ON ofEmiMucRoom (roomID);
CREATE INDEX ofEmiMucRoom_srvid_idx ON ofEmiMucRoom (serviceID);


CREATE TABLE ofEmiMucRoomProp (
  roomID                INTEGER         NOT NULL,
  name                  VARCHAR(100)    NOT NULL,
  propValue             VARCHAR(2000)   NOT NULL,
  CONSTRAINT ofEmiMucRoomProp_pk PRIMARY KEY (roomID, name)
);


CREATE TABLE ofEmiMucAffiliation (
  roomID              INTEGER       NOT NULL,
  jid                 VARCHAR(1000) NOT NULL,
  affiliation         INTEGER       NOT NULL,
  CONSTRAINT ofEmiMucAffil_pk PRIMARY KEY (roomID, jid)
);


CREATE TABLE ofEmiMucMember (
  roomID              INTEGER       NOT NULL,
  jid                 VARCHAR(1000) NOT NULL,
  nickname            VARCHAR(255),
  firstName           VARCHAR(100),
  lastName            VARCHAR(100),
  url                 VARCHAR(100),
  email               VARCHAR(100),
  faqentry            VARCHAR(100),
  CONSTRAINT ofEmiMucMember_pk PRIMARY KEY (roomID, jid)
);


CREATE TABLE ofEmiMucConversationLog (
  roomID              INTEGER       NOT NULL,
  sender              VARCHAR(2000) NOT NULL,
  nickname            VARCHAR(255),
  logTime             CHAR(15)      NOT NULL,
  subject             VARCHAR(255),
  body                CLOB
);
CREATE INDEX ofEmiMucConvLog_time_idx ON ofEmiMucConversationLog (logTime);
