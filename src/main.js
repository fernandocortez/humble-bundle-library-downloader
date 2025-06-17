import "dotenv/config";
import sqlite from "node:sqlite";
import { firefox } from "playwright";
import { generateTOTP } from "./otp.js";

const database = new sqlite.DatabaseSync("humble_bundle_library.db");
database.exec(`
	CREATE TABLE IF NOT EXISTS ebooks(
		title TEXT,
		publisher TEXT,
		PRIMARY KEY (title, publisher)
	) STRICT
`);

(async () => {
	const browser = await firefox.launch({ headless: true });
	const context = await browser.newContext({
		userAgent:
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
	});
	const page = await context.newPage();

	try {
		await page.goto("https://www.humblebundle.com/");
		await page.getByRole("link", { name: "Log In" }).click(); // go to login page

		// login page
		await page
			.getByPlaceholder("Email", { exact: true })
			.fill(process.env.HB_USERNAME);
		await page
			.getByPlaceholder("Password", { exact: true })
			.fill(process.env.HB_PASSWORD);
		await page.getByRole("button", { name: "Log In", exact: true }).click();

		// 2fa page
		const totp = generateTOTP({ key: process.env.HB_TOTP_SECRET });
		await page
			.getByPlaceholder("Enter Google Authenticator code", {
				exact: true,
			})
			.fill(totp);
		await page.getByRole("button", { name: "Verify", exact: true }).click();

		// home screen, logged in
		await page.getByLabel("Account Access").click();
		await page.getByRole("link", { name: "Library", exact: true }).click(); // go to Library page

		// library page
		await page.waitForTimeout(60_000); // wait for library items to start loading
		await page.locator("#switch-platform").selectOption("ebook"); // filter library items to only ebooks using dropdown
		await page.evaluate("window.scrollTo(0, document.body.scrollHeight);");
		await page.waitForTimeout(60_000); // wait for library items to finish loading

		const insert = database.prepare(
			"INSERT OR IGNORE INTO ebooks (title, publisher) VALUES (?, ?)",
		);
		const products = await page.locator(".subproduct-selector");
		const count = await products.count();
		for (let i = 0; i < count; ++i) {
			// nth is not zero based
			const textContent = await products.nth(i).textContent();
			const [title, publisher] = textContent
				.trim()
				.split("\n")
				.map((s) => s.trim());
			insert.run(title, publisher);
			// await products.nth(i).click();
		}
	} catch (e) {
		console.error(e);
	} finally {
		await page.getByLabel("Account Access").click();
		await page.getByRole("link", { name: "Logout", exact: true }).click();
		await page.waitForURL((url) => {
			return url.pathname.includes("/login");
		});
		await browser.close();
	}
})();
