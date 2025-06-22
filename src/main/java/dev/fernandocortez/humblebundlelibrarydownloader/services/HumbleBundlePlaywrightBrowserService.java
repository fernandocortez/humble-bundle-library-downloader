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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HumbleBundlePlaywrightBrowserService {

  private static Logger logger =
      LoggerFactory.getLogger(HumbleBundlePlaywrightBrowserService.class);
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
    browser = playwright.firefox().launch(
        new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
    context = browser.newContext(new Browser.NewContextOptions().setUserAgent(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
        .setBaseURL("https://www.humblebundle.com"));
    page = context.newPage();
  }

  public void populateDatabase() {
    // go to library page
    page.navigate("/home/library");

    // handle library page
    // wait for library items to start loading
    page.waitForTimeout(20_000);

    // filter library items to only ebooks using the dropdown
    page.locator("#switch-platform").selectOption("ebook");

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
      repository.saveEbook(ebook);
    }
  }

  @PostConstruct
  private void init() {
    page.navigate("/");
    // go to login page
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Log In"))
        .click();

    // login page
    page.getByPlaceholder("Email", new Page.GetByPlaceholderOptions().setExact(true))
        .fill(humbleBundleUsername);
    page.getByPlaceholder("Password", new Page.GetByPlaceholderOptions().setExact(true))
        .fill(humbleBundlePassword);
    page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Log In").setExact(true))
        .click();

    // 2fa page
    final String totp = OtpGenerator.generateTOTP(humbleBundleTotpSecret);
    page.getByPlaceholder("Enter Google Authenticator code",
        new Page.GetByPlaceholderOptions().setExact(true)).fill(totp);
    page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Verify").setExact(true))
        .click();
  }

  @PreDestroy
  private void cleanup() {
    logger.debug("Cleaning up browser session");
    try {
      logger.debug("Attempting to log out");
      page.navigate("/home/library");
      // open account dropdown
      page.getByLabel("Account Access").click();
      // click the logout link
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout")
          .setExact(true)).click();
      // wait to be redirected to login page
      page.waitForURL(url -> url.contains("/login"));
    } catch (Exception e) {
      logger.error("Unable to log out; most likely session was not logged in");
    } finally {
      context.close();
      browser.close();
      playwright.close();
    }
  }

}
