package dev.fernandocortez.humblebundlelibrarydownloader.dto;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HumbleBundleLibraryEbook {

  private String publisher;
  private String title;
  private String downloadOption;
  private String fileName;
  private Long fileId;
  private FileDownloadStatus downloadStatus;
  private OffsetDateTime lastStatusUpdate;

}
