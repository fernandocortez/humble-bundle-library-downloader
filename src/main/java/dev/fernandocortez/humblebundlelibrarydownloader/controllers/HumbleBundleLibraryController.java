package dev.fernandocortez.humblebundlelibrarydownloader.controllers;

import dev.fernandocortez.humblebundlelibrarydownloader.services.HumbleBundlePlaywrightBrowserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class HumbleBundleLibraryController {

  private final HumbleBundlePlaywrightBrowserService browserService;

  public HumbleBundleLibraryController(HumbleBundlePlaywrightBrowserService browserService) {
    this.browserService = browserService;
  }

  @GetMapping("/ebooks/load")
  public String getAllEbooks() {
    browserService.populateDatabase();
    return "OK";
  }

}
