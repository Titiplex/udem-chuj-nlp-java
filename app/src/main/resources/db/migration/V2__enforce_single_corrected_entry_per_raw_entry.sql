alter table corrected_entries
    add constraint uk_corrected_entries_raw_entry unique (raw_entry_id);