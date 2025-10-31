package dev.fernandocortez.humblebundlelibrarydownloader.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HumbleBundleLibraryEbook {

  private String title;
  private String publisher;
  private String downloadOption;
  private String fileName;

}
