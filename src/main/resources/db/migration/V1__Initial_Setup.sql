CREATE TABLE IF NOT EXISTS ebooks(
    ebook_id SERIAL UNIQUE,
    ebook_title VARCHAR(255) NOT NULL,
    ebook_publisher VARCHAR(255) NOT NULL,
    PRIMARY KEY(ebook_title, ebook_publisher)
);

CREATE TABLE IF NOT EXISTS ebook_files(
    ebook_id INTEGER REFERENCES ebooks(ebook_id) NOT NULL,
    file_id SERIAL UNIQUE,
    download_option VARCHAR(255) NOT NULL,
    file_name VARCHAR(511),
    PRIMARY KEY(ebook_id, download_option)
);
