package dev.fernandocortez.humblebundlelibrarydownloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class HumbleBundleLibraryDownloaderApplication {

  public static void main(String[] args) {
    SpringApplication.run(HumbleBundleLibraryDownloaderApplication.class, args);
  }

}
