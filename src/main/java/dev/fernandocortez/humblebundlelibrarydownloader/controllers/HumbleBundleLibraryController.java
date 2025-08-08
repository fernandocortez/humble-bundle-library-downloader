package dev.fernandocortez.humblebundlelibrarydownloader.controllers;

import dev.fernandocortez.humblebundlelibrarydownloader.services.HumbleBundlePlaywrightBrowserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class HumbleBundleLibraryController {

  private final HumbleBundlePlaywrightBrowserService browserService;

  public HumbleBundleLibraryController(HumbleBundlePlaywrightBrowserService browserService) {
    this.browserService = browserService;
  }

  @HxRequest
  @PostMapping("/ebooks/load")
  public String getAllEbooks() {
    browserService.populateDatabase();
    return "ebooks :: loading";
  }

  @HxRequest
  @PostMapping("/ebooks/all")
  public String postMethodName(Model model) {
    var ebooks = browserService.getAllEbooks();
    model.addAttribute("ebooks", ebooks);
    return "ebooks :: all";
  }

  @PostMapping("/ebooks/download")
  public ResponseEntity<?> downloadSelectedBooks(@RequestBody List<Integer> fileIds) {
    browserService.downloadSelectedFiles(fileIds);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

}
