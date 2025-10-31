CREATE TABLE IF NOT EXISTS publishers(
    id BIGINT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ebooks(
    id BIGINT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL UNIQUE,
    publisher_id BIGINT NOT NULL,

    CONSTRAINT fk_publisher
        FOREIGN KEY (publisher_id)
        REFERENCES publishers(id)
);

CREATE TABLE IF NOT EXISTS ebook_files(
    id BIGINT NOT NULL PRIMARY KEY,
    ebook_id BIGINT NOT NULL,
    download_option TEXT NOT NULL,
    file_name TEXT,
    download_status TEXT,
    last_status_update TIMESTAMP,

    CONSTRAINT fk_ebook
        FOREIGN KEY (ebook_id)
        REFERENCES ebooks(id)
);
