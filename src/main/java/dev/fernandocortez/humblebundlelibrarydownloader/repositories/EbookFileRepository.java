package dev.fernandocortez.humblebundlelibrarydownloader.repositories;

import dev.fernandocortez.humblebundlelibrarydownloader.entities.EbookFileEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EbookFileRepository implements PanacheRepository<EbookFileEntity> {

  /**
   * Finds all file options associated with a specific ebook.
   *
   * @param ebookId The ID of the parent ebook.
   * @return A list of EbookFile entities.
   */
  public List<EbookFileEntity> findByEbookId(Long ebookId) {
    // Uses the relationship field 'ebook' to query on its ID.
    return find("ebook.id", ebookId).list();
  }

  /**
   * Retrieves all EbookFile records, eagerly fetching the related Ebook and its Publisher using
   * JOIN FETCH.
   *
   * @return A list of EbookFile entities with Ebook and Publisher data loaded.
   */
  public List<EbookFileEntity> findAllWithEbookAndPublisher() {

    // PQL Query Explanation:
    // 'SELECT ef FROM EbookFileEntity ef' - Select the root entity
    // 'JOIN FETCH ef.ebook e' - Eagerly fetch the associated Ebook (aliased as 'e')
    // 'JOIN FETCH e.publisher' - Eagerly fetch the Ebook's Publisher

    final String pql = """
        SELECT ef FROM EbookFileEntity ef
          JOIN FETCH ef.ebook e
          JOIN FETCH e.publisher
          ORDER BY e.publisher, e.title, ef.downloadOption
        """;

    // The list() method executes the query and returns the results.
    return find(pql).list();
  }
}
