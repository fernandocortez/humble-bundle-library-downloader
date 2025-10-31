package dev.fernandocortez.humblebundlelibrarydownloader.repositories;

import dev.fernandocortez.humblebundlelibrarydownloader.entities.PublisherEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PublisherRepository implements PanacheRepository<PublisherEntity> {

  /**
   * Finds a single publisher by their unique name.
   *
   * @param name The name of the publisher.
   * @return The Publisher entity or null if not found.
   */
  public PublisherEntity findByName(String name) {
    // Panache automatically uses the entity field name
    return find("name", name).firstResult();
  }

}
