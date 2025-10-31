package dev.fernandocortez.humblebundlelibrarydownloader.repositories;

import dev.fernandocortez.humblebundlelibrarydownloader.entities.EbookEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EbookRepository implements PanacheRepository<EbookEntity> {

  /**
   * Finds all ebooks by the publisher's ID.
   *
   * @param publisherId The ID of the publisher.
   * @return A list of Ebook entities.
   */
  public List<EbookEntity> findByPublisherId(Long publisherId) {
    // Panache Query Language (PQL) method
    return find("publisher.id", publisherId).list();
  }

  /**
   * Finds a single ebook by its title. Panache automatically generates the query: SELECT e FROM
   * Ebook e WHERE e.title = ?1
   *
   * @param title The title of the ebook.
   * @return The Ebook entity or null if not found.
   */
  public EbookEntity findByTitle(String title) {
    return find("title", title).firstResult();
  }

}
