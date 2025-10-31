package dev.fernandocortez.humblebundlelibrarydownloader.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "publishers")
public class PublisherEntity extends PanacheEntityBase {

  @Id
  @Column(name = "id")
  public Long id;

  @Column(name = "name", unique = true, nullable = false)
  public String name;

  // Relationship: One Publisher to Many Ebooks
  // 'mappedBy' indicates the field in the Ebook entity that owns the relationship (publisher_id)
  @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY)
  public List<EbookEntity> ebooks;

  public PublisherEntity(String name) {
    this.name = name;
  }

}
