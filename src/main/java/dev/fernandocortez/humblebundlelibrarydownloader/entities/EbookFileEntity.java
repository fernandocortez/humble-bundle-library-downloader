package dev.fernandocortez.humblebundlelibrarydownloader.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ebook_files")
public class EbookFileEntity extends PanacheEntityBase {

  @Id
  @Column(name = "id")
  public Long id;

  @Column(name = "download_option", nullable = false)
  public String downloadOption;

  @Column(name = "file_name")
  public String fileName;

  // Relationship: Many EbookFiles to One Ebook
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ebook_id", nullable = false)
  public EbookEntity ebook;

  public EbookFileEntity(EbookEntity ebook, String downloadOption) {
    this.ebook = ebook;
    this.downloadOption = downloadOption;
  }

}
