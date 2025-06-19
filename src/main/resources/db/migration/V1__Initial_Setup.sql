CREATE TABLE IF NOT EXISTS ebooks(
    title TEXT NOT NULL,
    publisher TEXT NOT NULL,
    PRIMARY KEY (title, publisher)
)
