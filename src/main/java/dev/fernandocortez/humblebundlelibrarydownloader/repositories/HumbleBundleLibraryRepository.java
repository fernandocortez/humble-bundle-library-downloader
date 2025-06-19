package dev.fernandocortez.humblebundlelibrarydownloader.repositories;

import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import dev.fernandocortez.humblebundlelibrarydownloader.models.HumbleBundleLibraryEbook;

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
        return product;
    };

    public List<HumbleBundleLibraryEbook> findAllEbooks() {
        final String sql = "SELECT title, publisher FROM ebooks";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    public void saveEbook(HumbleBundleLibraryEbook ebook) {
        final String sql =
                "INSERT INTO ebooks (title, publisher) VALUES (?, ?) ON CONFLICT (title, publisher) DO NOTHING";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, ebook.getTitle());
            ps.setString(2, ebook.getPublisher());
            return ps;
        });
    }
}
