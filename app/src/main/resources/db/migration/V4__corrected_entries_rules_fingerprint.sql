alter table corrected_entries
    add column approved_rules_fingerprint VARCHAR(128);

update corrected_entries
set stale = true
where is_correct = true;
