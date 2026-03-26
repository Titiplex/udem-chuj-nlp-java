alter table corrected_entries
    add column stale BOOLEAN default false not null;

alter table corrected_entries
    add column approved_raw_updated_at TIMESTAMP null;

update corrected_entries
set stale = true
where is_correct = true;
