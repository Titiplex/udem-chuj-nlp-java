create table if not exists examples
(
    id               identity primary key,
    external_id      varchar(120),
    surface_text     clob        not null,
    normalized_text  clob,
    gloss_text       clob,
    translation_text clob,
    notes            clob,
    source_name      varchar(255),
    source_ref       varchar(255),
    status           varchar(32) not null,
    created_at       timestamp   not null default current_timestamp,
    updated_at       timestamp   not null default current_timestamp
);

create index if not exists idx_examples_external_id on examples (external_id);
