package dev.fernandocortez.humblebundlelibrarydownloader.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "ebooks")
public class EbookEntity extends PanacheEntityBase {

  @Id
  @Column(name = "id")
  public Long id;

  @Column(name = "title", unique = true, nullable = false)
  public String title;

  // Relationship: Many Ebooks to One Publisher
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "publisher_id", nullable = false)
  public PublisherEntity publisher;

  // Relationship: One Ebook to Many EbookFiles
  @OneToMany(mappedBy = "ebook", fetch = FetchType.LAZY)
  public List<EbookFileEntity> ebookFiles;

  public EbookEntity(PublisherEntity publisher, String title) {
    this.title = title;
    this.publisher = publisher;
  }

}
