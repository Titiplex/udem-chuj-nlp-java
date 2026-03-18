create table if not exists corrections
(
    id             identity primary key,
    example_id     bigint      not null,
    rule_id        bigint,
    before_payload clob,
    after_payload  clob,
    decision       varchar(32) not null,
    origin         varchar(32) not null,
    comment        clob,
    created_at     timestamp   not null default current_timestamp,
    constraint fk_corrections_example foreign key (example_id) references examples (id),
    constraint fk_corrections_rule foreign key (rule_id) references rules (id)
);

create index if not exists idx_corrections_example_id on corrections (example_id);
