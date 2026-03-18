create table if not exists validation_runs
(
    id          identity primary key,
    started_at  timestamp not null default current_timestamp,
    finished_at timestamp,
    ok          boolean   not null,
    summary     clob
);

create table if not exists validation_messages
(
    id                identity primary key,
    validation_run_id bigint      not null,
    severity          varchar(16) not null,
    message           clob        not null,
    rule_id           bigint,
    example_id        bigint,
    constraint fk_validation_messages_run foreign key (validation_run_id) references validation_runs (id)
);

create index if not exists idx_validation_messages_run_id on validation_messages (validation_run_id);
