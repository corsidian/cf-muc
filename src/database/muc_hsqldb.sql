// $Revision: 1650 $
// $Date: 2005-07-20 00:18:17 -0300 (Wed, 20 Jul 2005) $

// MUC Tables

CREATE TABLE ofEmiMucService (
  serviceID           BIGINT        NOT NULL,
  subdomain           VARCHAR(255)  NOT NULL,
  description         VARCHAR(255),
  isHidden            INTEGER       NOT NULL,
  CONSTRAINT ofEmiMucService_pk PRIMARY KEY (subdomain)
);
CREATE INDEX ofEmiMucService_serviceid_idx ON ofEmiMucService(serviceID);

CREATE TABLE ofEmiMucServiceProp (
  serviceID           BIGINT        NOT NULL,
  name                VARCHAR(100)  NOT NULL,
  propValue           VARCHAR(4000) NOT NULL,
  CONSTRAINT ofEmiMucServiceProp_pk PRIMARY KEY (serviceID, name)
);

CREATE TABLE ofEmiMucRoom (
  serviceID           BIGINT        NOT NULL,
  roomID              BIGINT        NOT NULL,
  creationDate        CHAR(15)      NOT NULL,
  modificationDate    CHAR(15)      NOT NULL,
  name                VARCHAR(50)   NOT NULL,
  naturalName         VARCHAR(255)  NOT NULL,
  description         VARCHAR(255),
  lockedDate          CHAR(15)      NOT NULL,
  emptyDate           CHAR(15)      NULL,
  canChangeSubject    INTEGER       NOT NULL,
  maxUsers            INTEGER       NOT NULL,
  publicRoom          INTEGER       NOT NULL,
  moderated           INTEGER       NOT NULL,
  membersOnly         INTEGER       NOT NULL,
  canInvite           INTEGER       NOT NULL,
  roomPassword        VARCHAR(50)   NULL,
  canDiscoverJID      INTEGER       NOT NULL,
  logEnabled          INTEGER       NOT NULL,
  subject             VARCHAR(100)  NULL,
  rolesToBroadcast    INTEGER       NOT NULL,
  useReservedNick     INTEGER       NOT NULL,
  canChangeNick       INTEGER       NOT NULL,
  canRegister         INTEGER       NOT NULL,
  CONSTRAINT ofEmiMucRoom_pk PRIMARY KEY (serviceID, name)
);
CREATE INDEX ofEmiMucRoom_roomid_idx ON ofEmiMucRoom (roomID);
CREATE INDEX ofEmiMucRoom_serviceid_idx ON ofEmiMucRoom (serviceID);

CREATE TABLE ofEmiMucRoomProp (
  roomID                BIGINT          NOT NULL,
  name                  VARCHAR(100)    NOT NULL,
  propValue             VARCHAR(4000)   NOT NULL,
  CONSTRAINT ofEmiMucRoomProp_pk PRIMARY KEY (roomID, name)
);

CREATE TABLE ofEmiMucAffiliation (
  roomID              BIGINT        NOT NULL,
  jid                 VARCHAR(1024) NOT NULL,
  affiliation         INTEGER       NOT NULL,
  CONSTRAINT ofEmiMucAffiliation_pk PRIMARY KEY (roomID, jid)
);

CREATE TABLE ofEmiMucMember (
  roomID              BIGINT        NOT NULL,
  jid                 VARCHAR(1024) NOT NULL,
  nickname            VARCHAR(255)  NULL,
  firstName           VARCHAR(100)  NULL,
  lastName            VARCHAR(100)  NULL,
  url                 VARCHAR(100)  NULL,
  email               VARCHAR(100)  NULL,
  faqentry            VARCHAR(100)  NULL,
  CONSTRAINT ofEmiMucMember_pk PRIMARY KEY (roomID, jid)
);

CREATE TABLE ofEmiMucConversationLog (
  roomID              BIGINT        NOT NULL,
  sender              VARCHAR(1024) NOT NULL,
  nickname            VARCHAR(255)  NULL,
  logTime             CHAR(15)      NOT NULL,
  subject             VARCHAR(255)  NULL,
  body                LONGVARCHAR   NULL
);
CREATE INDEX ofEmiMucConversationLog_time_idx ON ofEmiMucConversationLog (logTime);
