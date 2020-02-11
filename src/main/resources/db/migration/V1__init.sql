CREATE TABLE debtors
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(250) NOT NULL
);

CREATE TABLE records
(
    value      INT       NOT NULL,
    created_at timestamp NOT NULL,
    type       INT       NOT NULL,
    reporter   varchar   NOT NULL,
    debtor_id  INT,
    CONSTRAINT deptors_events_deptor_id_fk FOREIGN KEY (debtor_id) REFERENCES debtors (id)
        on update cascade on delete cascade
);