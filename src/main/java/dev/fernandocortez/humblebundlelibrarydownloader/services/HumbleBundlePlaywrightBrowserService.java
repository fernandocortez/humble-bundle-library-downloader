package dev.fernandocortez.humblebundlelibrarydownloader.services;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import dev.fernandocortez.humblebundlelibrarydownloader.helpers.OtpGenerator;
import dev.fernandocortez.humblebundlelibrarydownloader.models.HumbleBundleLibraryEbook;
import dev.fernandocortez.humblebundlelibrarydownloader.repositories.HumbleBundleLibraryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HumbleBundlePlaywrightBrowserService {

  private final HumbleBundleLibraryRepository repository;

  @Value("${humblebundle.username}")
  private String humbleBundleUsername;

  @Value("${humblebundle.password}")
  private String humbleBundlePassword;

  @Value("${humblebundle.totp}")
  private String humbleBundleTotpSecret;

  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  public HumbleBundlePlaywrightBrowserService(HumbleBundleLibraryRepository repository) {
    this.repository = repository;

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

  @Async
  public void populateDatabase() {
    // go to library page
    page.navigate("/home/library");
    log.debug("Navigated to library page");

    // handle library page
    // wait for library items to start loading
    page.waitForTimeout(20_000);

    // filter library items to only ebooks using the dropdown
    page.locator("#switch-platform").selectOption("ebook");
    log.debug("Filtering library to display only ebooks");

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
    log.debug("Scrolled back to top of library page");

    final var products = page.locator(".subproduct-selector");
    final int count = products.count();
    for (int i = 0; i < count; ++i) {
      // nth is not zero based
      final String textContent = products.nth(i).textContent();
      final List<String> details = Arrays.stream(textContent.trim().split("\n"))
          .map(String::trim).toList();
      final String title = details.getFirst();
      final String publisher = details.getLast();
      final HumbleBundleLibraryEbook ebook = new HumbleBundleLibraryEbook();
      ebook.setTitle(title);
      ebook.setPublisher(publisher);
      final int rowsAffected = repository.saveEbook(ebook);
      if (rowsAffected > 0) {
        log.debug("ebook inserted into database: {}", ebook);
      }
    }
  }

  public List<HumbleBundleLibraryEbook> getAllEbooks() {
    return repository.findAllEbooks();
  }

  @PostConstruct
  private void init() {
    log.debug("Initializing Humble Bundle session");
    page.navigate("/");
    // go to login page
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Log In"))
        .click();
    log.debug("Navigated to login page");

    // login page
    log.debug("Entering user credentials");
    page.getByPlaceholder("Email", new Page.GetByPlaceholderOptions().setExact(true))
        .fill(humbleBundleUsername);
    page.getByPlaceholder("Password", new Page.GetByPlaceholderOptions().setExact(true))
        .fill(humbleBundlePassword);
    page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Log In").setExact(true))
        .click();

    // 2fa page
    log.debug("Entering 2FA token");
    final String totp = OtpGenerator.generateTOTP(humbleBundleTotpSecret);
    page.getByPlaceholder("Enter Google Authenticator code",
        new Page.GetByPlaceholderOptions().setExact(true)).fill(totp);
    page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Verify").setExact(true))
        .click();
  }

  @PreDestroy
  private void cleanup() {
    log.debug("Cleaning up browser session");
    try {
      log.debug("Attempting to log out");
      page.navigate("/home/library");
      // open account dropdown
      page.getByLabel("Account Access").click();
      // click the logout link
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout")
          .setExact(true)).click();
      // wait to be redirected to login page
      page.waitForURL(url -> url.contains("/login"));
      log.debug("Log out should be successful");
    } catch (Exception e) {
      log.error("Unable to log out; most likely session was not logged in");
    } finally {
      log.debug("Closing browser instance");
      context.close();
      browser.close();
      playwright.close();
      log.debug("Browser instance closed");
    }
  }

}
