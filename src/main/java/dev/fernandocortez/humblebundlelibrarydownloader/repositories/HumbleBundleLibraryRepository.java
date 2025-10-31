package dev.fernandocortez.humblebundlelibrarydownloader.repositories;

import dev.fernandocortez.humblebundlelibrarydownloader.dto.HumbleBundleLibraryEbook;
import dev.fernandocortez.humblebundlelibrarydownloader.entities.EbookEntity;
import dev.fernandocortez.humblebundlelibrarydownloader.entities.EbookFileEntity;
import dev.fernandocortez.humblebundlelibrarydownloader.entities.PublisherEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HumbleBundleLibraryRepository {

  private static final Logger LOG = Logger.getLogger(HumbleBundleLibraryRepository.class);

  @Inject
  PublisherRepository publisherRepository;
  @Inject
  EbookRepository ebookRepository;
  @Inject
  EbookFileRepository ebookFileRepository;

  @Transactional
  public long insertPublisher(String name) {
    PublisherEntity publisher = new PublisherEntity(name);
    publisherRepository.persist(publisher);
    return publisher.id;
  }

  @Transactional
  public long insertEbook(long publisherId, String title) {
    PublisherEntity publisher = PublisherEntity.findById(publisherId);
    EbookEntity ebook = new EbookEntity(publisher, title);
    ebookRepository.persist(ebook);
    return ebook.id;
  }

  @Transactional
  public long insertEbookFile(long ebookId, String downloadOption) {
    EbookEntity ebook = EbookEntity.findById(ebookId);
    EbookFileEntity ebookFile = new EbookFileEntity(ebook, downloadOption);
    ebookFileRepository.persist(ebookFile);
    return ebookFile.id;
  }

  public List<HumbleBundleLibraryEbook> selectAllEbooks() {
    final var ebookFiles = ebookFileRepository.findAllWithEbookAndPublisher();
    final var ebooks = ebookFiles.parallelStream()
        .map(ebookFile -> HumbleBundleLibraryEbook.builder()
            .publisher(ebookFile.ebook.publisher.name)
            .title(ebookFile.ebook.title)
            .downloadOption(ebookFile.downloadOption)
            .fileName(ebookFile.fileName)
            .fileId(ebookFile.id)
            .downloadStatus(ebookFile.downloadStatus)
            .lastStatusUpdate(ebookFile.lastStatusUpdate)
            .build())
        .toList();
    return ebooks;
  }

  public List<HumbleBundleLibraryEbook> getSelectedEbooksFromFileIds(Set<Long> fileIds) {
    if (fileIds == null || fileIds.isEmpty()) {
      return Collections.emptyList();
    }

    final var ebookFiles = ebookFileRepository.findAllWithEbookAndPublisher();
    final var ebooks = ebookFiles.parallelStream()
        .filter(ebookFile -> fileIds.contains(ebookFile.id))
        .map(ebookFile -> HumbleBundleLibraryEbook.builder()
            .publisher(ebookFile.ebook.publisher.name)
            .title(ebookFile.ebook.title)
            .downloadOption(ebookFile.downloadOption)
            .fileName(ebookFile.fileName)
            .fileId(ebookFile.id)
            .downloadStatus(ebookFile.downloadStatus)
            .lastStatusUpdate(ebookFile.lastStatusUpdate)
            .build())
        .toList();
    return ebooks;
  }
}
