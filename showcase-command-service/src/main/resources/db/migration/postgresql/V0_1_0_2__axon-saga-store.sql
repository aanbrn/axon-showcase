CREATE TABLE sagaentry
(
    sagaid VARCHAR(255) NOT NULL,
    revision VARCHAR(255),
    sagatype VARCHAR(255),
    serializedsaga BYTEA,
    CONSTRAINT sagaentry_pkey PRIMARY KEY (sagaid)
);

CREATE TABLE associationvalueentry
(
    id BIGSERIAL NOT NULL,
    associationkey VARCHAR(255),
    associationvalue VARCHAR(255),
    sagaid VARCHAR(255),
    sagatype VARCHAR(255),
    CONSTRAINT associationvalueentry_pkey PRIMARY KEY (id)
);

CREATE INDEX associationvalueentry_sagaid_idx ON associationvalueentry (sagaid);
CREATE INDEX associationvalueentry_associationkey_associationvalue_idx
    ON associationvalueentry (associationkey, associationvalue);
