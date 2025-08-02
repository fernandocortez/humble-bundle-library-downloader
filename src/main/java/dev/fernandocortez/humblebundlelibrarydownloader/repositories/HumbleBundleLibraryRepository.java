package dev.fernandocortez.humblebundlelibrarydownloader.repositories;

import dev.fernandocortez.humblebundlelibrarydownloader.models.HumbleBundleLibraryEbook;
import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HumbleBundleLibraryRepository {

  private final JdbcTemplate jdbcTemplate;

  public HumbleBundleLibraryRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // RowMapper to convert ResultSet rows to Ebook objects
  private final RowMapper<HumbleBundleLibraryEbook> productRowMapper = (rs, rowNum) -> {
    HumbleBundleLibraryEbook product = new HumbleBundleLibraryEbook();
    product.setTitle(rs.getString("title"));
    product.setPublisher(rs.getString("publisher"));
    product.setDownloadOption(rs.getString("download_option"));
    product.setFileId(rs.getInt("file_id"));
    return product;
  };

  public List<HumbleBundleLibraryEbook> findAllEbooks() {
    final String sql = """
        SELECT
        	e.ebook_title AS title,
        	e.ebook_publisher AS publisher,
        	ef.download_option AS download_option,
          ef.file_id AS file_id
        FROM
        	ebooks e
        JOIN ebook_files ef ON
        	e.ebook_id = ef.ebook_id
        ORDER BY
        	publisher,
        	title,
        	download_option;
        """;
    return jdbcTemplate.query(sql, productRowMapper);
  }

  public int saveEbook(String title, String publisher) {
    final String sql = """
        INSERT INTO ebooks (ebook_title, ebook_publisher)
        SELECT ?, ?
        WHERE NOT EXISTS (SELECT 1 FROM ebooks WHERE ebook_title = ? AND ebook_publisher = ?);
        """;
    return jdbcTemplate.update(connection -> {
      final PreparedStatement ps = connection.prepareStatement(sql);
      ps.setString(1, title);
      ps.setString(2, publisher);
      ps.setString(3, title);
      ps.setString(4, publisher);
      return ps;
    });
  }

  public int getEbookId(String title, String publisher) {
    final String sql = "SELECT ebook_id FROM ebooks WHERE ebook_title = ? AND ebook_publisher = ?;";
    final Integer id = jdbcTemplate.queryForObject(sql, Integer.class, title, publisher);
    return id == null ? 0 : id;
  }

  public int saveEbookDownloadOption(int ebookId, String option) {
    final String sql = """
        INSERT INTO ebook_files (ebook_id, download_option)
        SELECT ?, ?
        WHERE NOT EXISTS (SELECT 1 FROM ebook_files WHERE ebook_id = ? AND download_option = ?);
        """;
    return jdbcTemplate.update(connection -> {
      final PreparedStatement ps = connection.prepareStatement(sql);
      ps.setInt(1, ebookId);
      ps.setString(2, option);
      ps.setInt(3, ebookId);
      ps.setString(4, option);
      return ps;
    });
  }
}
