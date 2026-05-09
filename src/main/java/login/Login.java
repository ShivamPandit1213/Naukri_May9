package login;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import publicmethod.PublicMethod;
import utils.ConfigReader;
import utils.LocatorReader;

public class Login {
	// 🔹 Step 1: Declare WebDriver (browser instance)
	// This will be received from Test class (NOT created here)
	private WebDriver driver;

	// 🔹 Step 2: Constructor Injection
	// When Login object is created, driver is passed from Test class
	public Login(WebDriver driver) {
		this.driver = driver; // Assign passed driver to class variable
	}

	// 🔹 Step 3: Main Login Method
	public void naukriLogin() throws Throwable {
		System.out.println("===============================================");
		// 🔹 Step 4: Create object of reusable methods class
		// Used for waits, screenshots, etc.
		PublicMethod callCustomMethod = new PublicMethod(driver);

		// 🔹 Step 5: Get URL from config.properties
		// Example: naukri.login.url=https://www.naukri.com
		String naukriURL = ConfigReader.getConfig("naukri.login.url");

		// 🔹 Step 6: Open the URL in browser
		driver.get(naukriURL);

		// =========================================================
		// 🔹 Step 7: Click on Login link
		// Get locator from locator file
		String loginLink = LocatorReader.getLocator("loginLink");

		// Wait until element is visible
		WebElement wb = callCustomMethod.waitForElementVisible(loginLink);

		// Click on Login link
		wb.click();
		//Thread.sleep(1000);
		callCustomMethod.getScreenshot();
		System.out.println("Login form field is visible.");

		// 🔹 Step 8: Enter Email
		// Get email locator
		String emailLocator = LocatorReader.getLocator("emailLocator");

		// Wait until element is clickable
		WebElement email = callCustomMethod.waitUntilElementClickable(emailLocator);

		// Get username from config file
		String user = ConfigReader.getConfig("naukri.user");

		// Enter username
		email.sendKeys(user);
		System.out.println("Entered Email Id: " + user);

		// 🔹 Step 9: Enter Password
		// Get password locator
		String passLocator = LocatorReader.getLocator("passLocator");

		// Find password field using driver
		WebElement passElement = driver.findElement(By.xpath(passLocator));

		// Get password from config file
		String pass = ConfigReader.getConfig("naukri.password");

		// Enter password
		passElement.sendKeys(pass);
		callCustomMethod.getScreenshot();
		System.out.println("Entered Password: " + pass);

		// =========================================================
		// 🔹 Step 11: Click Login button
		// Get submit button locator
		String submitLogin = LocatorReader.getLocator("submitLogin");
		callCustomMethod.waitUntilElementClickable(submitLogin);
		// Find element
		driver.findElement(By.xpath(submitLogin)).click();
		//Thread.sleep(3000);
		String profile = LocatorReader.getLocator("profile");
		callCustomMethod.waitForElementVisible(profile);
		callCustomMethod.getScreenshot();
		System.out.println("Clicked on Login button -> Login successful");
		System.out.println("===============================================");
	}
}