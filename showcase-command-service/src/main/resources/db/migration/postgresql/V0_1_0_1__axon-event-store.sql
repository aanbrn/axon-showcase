CREATE TABLE domainevententry
(
    globalindex BIGSERIAL NOT NULL,
    aggregateidentifier VARCHAR(255) NOT NULL,
    sequencenumber BIGINT NOT NULL,
    "type" VARCHAR(255),
    eventidentifier VARCHAR(255) NOT NULL,
    metadata BYTEA,
    payload BYTEA NOT NULL,
    payloadrevision VARCHAR(255),
    payloadtype VARCHAR(255) NOT NULL,
    "timestamp" VARCHAR(255) NOT NULL,
    CONSTRAINT domainevententry_pkey PRIMARY KEY (globalindex),
    CONSTRAINT domainevententry_aggregateidentifier_sequencenumber_key UNIQUE (aggregateidentifier, sequencenumber),
    CONSTRAINT domainevententry_eventidentifier_key UNIQUE (eventidentifier)
);

CREATE TABLE snapshotevententry
(
    aggregateidentifier VARCHAR(255) NOT NULL,
    sequencenumber BIGINT NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    eventidentifier VARCHAR(255) NOT NULL,
    metadata BYTEA,
    payload BYTEA NOT NULL,
    payloadrevision VARCHAR(255),
    payloadtype VARCHAR(255) NOT NULL,
    "timestamp" VARCHAR(255) NOT NULL,
    CONSTRAINT snapshotevententry_pkey PRIMARY KEY (aggregateidentifier, sequencenumber),
    CONSTRAINT snapshotevententry_eventidentifier_key UNIQUE (eventidentifier)
);

CREATE TABLE tokenentry
(
    processorname VARCHAR(255) NOT NULL,
    segment INTEGER NOT NULL,
    token BYTEA,
    tokentype VARCHAR(255),
    "timestamp" VARCHAR(255),
    "owner" VARCHAR(255),
    CONSTRAINT tokenentry_pkey PRIMARY KEY (processorname, segment)
);
