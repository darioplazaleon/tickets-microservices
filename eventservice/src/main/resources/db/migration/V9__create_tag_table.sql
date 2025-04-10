CREATE TABLE event_tags
(
    event_id UUID NOT NULL,
    tag_id   UUID NOT NULL,
    PRIMARY KEY (event_id, tag_id),
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);