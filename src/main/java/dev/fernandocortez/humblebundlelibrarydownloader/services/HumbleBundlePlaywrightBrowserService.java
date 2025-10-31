package dev.fernandocortez.humblebundlelibrarydownloader.services;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import dev.fernandocortez.humblebundlelibrarydownloader.dto.HumbleBundleLibraryEbook;
import dev.fernandocortez.humblebundlelibrarydownloader.helpers.OtpGenerator;
import dev.fernandocortez.humblebundlelibrarydownloader.repositories.HumbleBundleLibraryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HumbleBundlePlaywrightBrowserService {

  private static final Logger LOG = Logger.getLogger(HumbleBundlePlaywrightBrowserService.class);

  @Inject
  HumbleBundleLibraryRepository repository;

  @ConfigProperty(name = "humblebundle.username")
  private String humbleBundleUsername;

  @ConfigProperty(name = "humblebundle.password")
  private String humbleBundlePassword;

  @ConfigProperty(name = "humblebundle.totp")
  private String humbleBundleTotpSecret;

  @ConfigProperty(name = "humblebundle.basefilepath")
  private String baseFilePath;

  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;
  private boolean isLibraryLoaded = false;

  public HumbleBundlePlaywrightBrowserService() {
    playwright = Playwright.create();
    browser = playwright.firefox()
        .launch(new BrowserType.LaunchOptions().setHeadless(true));
//    browser = playwright.firefox().launch(
//        new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
    context = browser.newContext(new Browser.NewContextOptions().setUserAgent(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
        .setBaseURL("https://www.humblebundle.com"));
    page = context.newPage();
  }

  private void navigateToLibraryAndWaitForProductsToLoad() {
    final String url = page.url();
    if (!url.contains("/home/library")) {
      // go to library page
      page.navigate("/home/library");
      LOG.debug("Navigated to library page");
    }

    if (isLibraryLoaded) {
      LOG.debug("Library already loaded");
      return;
    }

    // handle library page
    // wait for library items to start loading
    page.waitForTimeout(20_000);

    // filter library items to only ebooks using the dropdown
    page.locator("#switch-platform").selectOption("ebook");
    LOG.debug("Filtering library to display only ebooks");

    for (int i = 0; i < 6; i++) {
      // scroll to the bottom of the page; loads more items
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
              .setName("Terms of Service").setExact(true))
          .scrollIntoViewIfNeeded();
      page.evaluate("window.scrollTo(0, document.body.scrollHeight);");
      // wait for library items to continue loading
      page.waitForTimeout(10_000);
    }

    // scroll to the top of the page
    page.locator("#switch-platform").scrollIntoViewIfNeeded();
    LOG.debug("Scrolled back to top of library page");
    this.isLibraryLoaded = true;
  }

  //  @Async
  public void populateDatabase() {
    this.navigateToLibraryAndWaitForProductsToLoad();

    final var products = page.locator(".subproduct-selector");
    final int productCount = products.count();
    for (int i = 0; i < productCount; i++) {
      final var product = products.nth(i + 1); // nth is not zero based
      product.click();

      final String productTextContent = product.textContent();
      final List<String> details = Arrays.stream(productTextContent.trim().split("\n"))
          .map(String::trim).toList();
      final String title = details.getFirst();
      final String publisher = details.getLast();

      final long publisherId = repository.insertPublisher(publisher);
      final long ebookId = repository.insertEbook(publisherId, title);

      if (ebookId < 1) {
        continue; // go to next ebook
      }
      final var downloadOptions = page.locator(".download-section .download-button h4");
      final int downloadsCount = downloadOptions.count();
      for (int j = 0; j < downloadsCount; ++j) {
        final var downloadOption = downloadOptions.nth(j);
        final String downloadOptionType = downloadOption.allInnerTexts().getFirst();
        repository.insertEbookFile(ebookId, downloadOptionType);
      }
    }
    LOG.debug("Finished populating database");
  }

  //  @Async
  public void downloadSelectedFiles(Set<Long> fileIds) {
    this.navigateToLibraryAndWaitForProductsToLoad();

    final List<HumbleBundleLibraryEbook> ebooksToDownload = repository.getSelectedEbooksFromFileIds(
        fileIds);
    ebooksToDownload.forEach(ebook -> {
      final String filePath = baseFilePath + "/" + ebook.getPublisher() + "/" + ebook.getTitle();
      try {
        new File(filePath).mkdirs();
      } catch (Exception e) {
        LOG.error(String.format("Unable to create directory %s: %s", filePath, e.getMessage()));
      }

      // click from product list along left
      page.locator(".subproduct-selector")
          .filter(new Locator.FilterOptions().setHasText(ebook.getTitle()))
          .filter(new Locator.FilterOptions().setHasText(ebook.getPublisher()))
          .click();

      final Download download = page.waitForDownload(() -> {
        // click download option under selected product
        page.locator(".download-section .download-button h4")
            .filter(new Locator.FilterOptions().setHasText(ebook.getDownloadOption()))
            .click();
      });
      download.saveAs(Paths.get(filePath, download.suggestedFilename()));
    });
  }

  @PostConstruct
  void init() {
    LOG.debug("Initializing Humble Bundle session");
    page.navigate("/");
    // go to login page
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Log In"))
        .click();
    LOG.debug("Navigated to login page");

    // login page
    LOG.debug("Entering user credentials");
    page.getByPlaceholder("Email", new Page.GetByPlaceholderOptions().setExact(true))
        .fill(humbleBundleUsername);
    page.getByPlaceholder("Password", new Page.GetByPlaceholderOptions().setExact(true))
        .fill(humbleBundlePassword);
    page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Log In").setExact(true))
        .click();

    // 2fa page
    LOG.debug("Entering 2FA token");
    final String totp = OtpGenerator.generateTOTP(humbleBundleTotpSecret);
    page.getByPlaceholder("Enter Google Authenticator code",
        new Page.GetByPlaceholderOptions().setExact(true)).fill(totp);
    page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Verify").setExact(true))
        .click();
  }

  @PreDestroy
  void cleanup() {
    LOG.debug("Cleaning up browser session");
    try {
      LOG.debug("Attempting to log out");
      page.navigate("/home/library");
      // open account dropdown
      page.getByLabel("Account Access").click();
      // click the logout link
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout")
          .setExact(true)).click();
      // wait to be redirected to login page
      page.waitForURL(url -> url.contains("/login"));
      LOG.debug("Log out should be successful");
    } catch (Exception e) {
      LOG.error("Unable to log out; most likely session was not logged in");
    } finally {
      LOG.debug("Closing browser instance");
      context.close();
      browser.close();
      playwright.close();
      LOG.debug("Browser instance closed");
    }
  }

}
