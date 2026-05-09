package launch;

import java.time.Duration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import utils.ConfigReader;

/**
 * Core Initialization Class for WebDriver Sessions.
 * ---------------------------------------------------------
 * This class handles the setup, configuration, and teardown of browsers 
 * for your automated workflows. It includes specialized ChromeOptions 
 * to bypass bot detection, suppress errors, and optimize page load speeds.
 */
public class Launch {

	protected WebDriver driver;

	/**
	 * Initializes the WebDriver based on the provided browser name.
	 * Includes built-in stability tweaks, anti-bot measures, and logger muting.
	 * * @param browser The name of the browser to launch (e.g., "chrome", "firefox", "edge").
	 * @return A fully configured WebDriver instance.
	 */
	public WebDriver launchByBrowserName(String browser) {

		// =====================================================================
		// 1. LOGGER CONFIGURATION
		// =====================================================================
		// Mutes standard Selenium warnings (specifically hides the red CDP/WebSocket 
		// errors that occur when a session is closed abruptly).
		System.setProperty("webdriver.chrome.silentOutput", "true");
		Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);

		// =====================================================================
		// 2. BROWSER INITIALIZATION SWITCH
		// =====================================================================
		switch (browser.toLowerCase()) {

		case "chrome":
			WebDriverManager.chromedriver().setup();
			ChromeOptions options = new ChromeOptions();
			
			// --- UI & Environment Options ---
			options.addArguments("--start-maximized");       // Opens browser in full screen
			options.addArguments("--disable-notifications"); // Blocks system dialogs/popups (e.g., "Allow Location")
			options.addArguments("--disable-infobars");      // Hides "Chrome is being controlled by automated software"
			options.addArguments("--disable-extensions");    // Prevents installed extensions from interfering
			options.addArguments("--remote-allow-origins=*");// Fixes WebSocket connection issues introduced in Selenium 4+

			// --- Anti-Bot & Headless Stability Options ---
			// Crucial for bypassing strict web application firewalls (WAF) that block automated scripts
			options.addArguments("--disable-blink-features=AutomationControlled"); 
			options.addArguments("--disable-gpu");           // Recommended for stability, especially in CI/CD environments
			options.addArguments("--no-sandbox");            // Bypasses OS security model constraints (needed for Linux/Docker)
			options.addArguments("--disable-dev-shm-usage"); // Overcomes limited memory resource problems in Docker

			// --- Network & Performance Enhancements ---
			// Speeds up navigation by disabling background network fetching
			options.addArguments("--dns-prefetch-disable");
			options.addArguments("--disable-features=NetworkService");

			// --- Page Load Strategy ---
			// EAGER tells Selenium to proceed as soon as the DOM is ready (HTML loaded).
			// It will NOT wait for heavy resources like images or stylesheets to finish downloading, speeding up tests.
			options.setPageLoadStrategy(PageLoadStrategy.EAGER);
			
			// Disables the BiDi/WebSocket connection to prevent "Connection Reset" exceptions on driver.close()
			options.setCapability("se:cdpEnabled", false);
			
			driver = new ChromeDriver(options);
			break;

		case "firefox":
			WebDriverManager.firefoxdriver().setup();
			driver = new FirefoxDriver();
			break;

		case "edge":
			WebDriverManager.edgedriver().setup();
			driver = new EdgeDriver();
			break;

		case "ie":
			WebDriverManager.iedriver().setup();
			driver = new InternetExplorerDriver();
			break;

		default:
			throw new IllegalArgumentException("Browser not supported: " + browser);
		}

		// =====================================================================
		// 3. GLOBAL TIMEOUTS & NAVIGATION
		// =====================================================================
		
		// Implicit Wait: Maximum time to poll the DOM when searching for elements before throwing an exception
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
		
		// Page Load Timeout: Maximum time to wait for a page to completely render (based on the EAGER strategy above)
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));

		// Navigate to the base URL fetched from the properties configuration
		driver.get(ConfigReader.getConfig("naukri.login.url"));

		System.out.println("=== Browser launched: " + browser);

		return driver;
	}

	// =========================================================================
	// COOKIE MANAGEMENT SECTION
	// =========================================================================

	/**
	 * Wipes all cookies from the current browsing context.
	 * Useful for ensuring a completely clean session before attempting to log in.
	 */
	private void manageInitialCookies() {
		driver.manage().deleteAllCookies();
		System.out.println("All initial cookies cleared.");
	}

	/**
	 * Injects a specific cookie into the browser session.
	 * Can be used to bypass login screens if a valid authentication token is known.
	 * * @param name  The name of the cookie.
	 * @param value The value/token of the cookie.
	 */
	public void addCustomCookie(String name, String value) {
		Cookie customCookie = new Cookie(name, value);
		driver.manage().addCookie(customCookie);
		System.out.println("Added cookie: " + name);
	}

	/**
	 * Prints all active cookies to the console.
	 * Highly useful for debugging session drops or verifying auth token generation.
	 */
	public void printAllCookies() {
		Set<Cookie> cookies = driver.manage().getCookies();
		System.out.println("Current Cookies Count: " + cookies.size());
		for (Cookie ck : cookies) {
			System.out.println(String.format("Name: %s | Domain: %s | Expiry: %s", ck.getName(), ck.getDomain(), ck.getExpiry()));
		}
	}

	/**
	 * Targets and removes a specific cookie by its exact name.
	 * * @param name The name of the cookie to remove.
	 */
	public void deleteCookieNamed(String name) {
		driver.manage().deleteCookieNamed(name);
	}

	// =========================================================================
	// TEARDOWN SECTION
	// =========================================================================

	/**
	 * Safely terminates the WebDriver session and closes all associated windows.
	 * Note: Currently commented out to keep the browser open for post-execution visual debugging.
	 */
	public void exitBrowser() {
//		if (driver != null) {
//			driver.quit();
//			System.out.println("✅ Browser closed successfully.");
//		}
	}
}