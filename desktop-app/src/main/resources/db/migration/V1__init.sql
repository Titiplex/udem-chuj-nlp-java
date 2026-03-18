create table if not exists rules
(
    id          identity primary key,
    stable_id   varchar(200) not null unique,
    name        varchar(200) not null,
    enabled     boolean      not null,
    yaml_body   clob         not null,
    source_file varchar(255),
    description clob,
    version_no  integer      not null default 1,
    created_at  timestamp    not null default current_timestamp,
    updated_at  timestamp    not null default current_timestamp
);

create index if not exists idx_rules_stable_id on rules (stable_id);

create table if not exists lexicon_entries
(
    id            identity primary key,
    lexicon_name  varchar(120) not null,
    entry_key     varchar(255) not null,
    entry_value   clob         not null,
    metadata_json clob
);

create index if not exists idx_lexicon_name on lexicon_entries (lexicon_name);
