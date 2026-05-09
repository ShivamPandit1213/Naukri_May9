package publicmethod;

// [SOURCE: Standard Java Libraries]
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

// [SOURCE: External Apache & Selenium Libraries]
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.LocatorReader;

/**
 * PublicMethod A centralized utility class containing reusable Selenium
 * WebDriver interactions. Handles common operations like explicit waits, window
 * switching, scrolling, and file uploads.
 */
public class PublicMethod {

	// Browser instance to be used across all utility methods
	private WebDriver driver;

	public PublicMethod(WebDriver driver) {
		this.driver = driver;
	}

	public WebElement getIdLocator(String idLocator, int i) {
		// 1. Fetch the exact 'id' string from the properties file
		String id = LocatorReader.getLocator(idLocator);
		// 2. Find and return the element using the fetched 'id'
		List<WebElement> element = driver.findElements(By.id(id));
		// Get the element based on index (index i)
		WebElement wb = element.get(i);
		return wb;
	}

	public WebElement getNameLocator(String nameLocator, int i) {
		// 1. Fetch the exact 'name' string from the properties file
		String name = LocatorReader.getLocator(nameLocator);
		// 2. Find and return the element using the fetched 'name'
		List<WebElement> element = driver.findElements(By.name(name));
		// Get the element based on index (index i)
		WebElement wb = element.get(i);
		return wb;
	}

	public WebElement getElement(String locatorKey) {
		// 1. Fetch the exact XPath string from the properties file
		String xpathValue = LocatorReader.getLocator(locatorKey);

		// 2. Find and return the element using the fetched XPath
		return driver.findElement(By.xpath(xpathValue));
	}

	/**
	 * Default method: Uses "Text from element: "
	 */
	public String getText(String locator) {
		// Calls the other method and passes the default prefix
		return getText(locator, "Text from element: ");
	}

	/**
	 * Overloaded method: Allows you to pass a custom prefix like "Ques: "
	 */
	public String getText(String locator, String prefix) {
		WebElement element = driver.findElement(By.xpath(locator));
		String text = element.getText();
		System.out.println(prefix + text);
		return text;
	}

	// ====================================================================================
	// 📸 SCREENSHOT UTILITIES
	// ====================================================================================
	public File getScreenshot(String fileNamePrefix) throws IOException {
		File screenshotDir = new File("src/test/resources/screenshots");

		// Ensure the directory exists before attempting to save
		if (!screenshotDir.exists()) {
			screenshotDir.mkdirs();
			System.out.println("Screenshot folder created.");
		}

		// Fallback protection against null inputs
		if (fileNamePrefix == null) {
			fileNamePrefix = "Screenshot_";
		}

		// Generate a unique timestamp to prevent file overwriting
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		File destination = new File(screenshotDir, fileNamePrefix + timestamp + ".png");

		FileUtils.copyFile(source, destination);
		System.out.println("\nScreenshot: " + destination);
		return destination;
	}

	public File getScreenshot() throws IOException {
		return getScreenshot("Screenshot_");
	}

	// ====================================================================================
	// ⏳ WAIT & VERIFICATION UTILITIES
	// ====================================================================================
	public WebElement verifyAndGetElement(String xpath) throws IOException {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
		// wait.until(ExpectedConditions.elementToBeClickable(element));

		if (element.isDisplayed() && element.isEnabled()) {
			System.out.println("Element ready: " + xpath);
			return element;
		} else {
			System.err.println("Element not interactable: " + xpath);
			return null;
		}
	}

	public WebElement waitForElementVisible(String locator) throws Throwable {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		WebElement element = null;
		try {
			element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
			Thread.sleep(2000); // Brief pause for UI stabilization
			getScreenshot();
		} catch (TimeoutException e) {
			throw new RuntimeException("Element not visible: " + locator);
		}
		return element;
	}

	public WebElement waitUntilElementClickable(String locator) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
	}

	public WebElement waitForElementVisible(WebDriver driver, String locator) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		System.out.println("Element visible: " + locator);
		return element;
	}

	public WebElement waitUntilElementClickable(WebDriver driver, String locator) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
		System.out.println("Element clickable: " + locator);
		return element;
	}

	// ====================================================================================
	// 🖱️ BASIC ACTIONS & INTERACTIONS
	// ====================================================================================
	public void click(String locator) throws IOException {
		WebElement element = waitUntilElementClickable(locator);
		element.click();
		getScreenshot();
		System.out.println("Clicked element: " + locator);
	}

	public void moveToElement(WebElement element) {
		Actions actions = new Actions(driver);
		actions.moveToElement(element).perform();
	}

	public void moveToElement(WebDriver driver, WebElement element) {
		Actions actions = new Actions(driver);
		actions.moveToElement(element).perform();
		System.out.println("Moved to element: " + element);
	}

	public void handleSlider(String locator, int slidePixel) throws Throwable {
		WebElement slider = waitForElementVisible(locator);
		Actions action = new Actions(driver);
		// Click and hold the slider, move horizontally (X-axis), stay on same Y-axis
		// (0), then drop
		action.clickAndHold(slider).moveByOffset(slidePixel, 0).release().perform();
		getScreenshot();
	}

	// ====================================================================================
	// 🪟 WINDOW & TAB HANDLING
	// ====================================================================================
	public String getParentWindow() {
		String parent = driver.getWindowHandle();
		System.out.println("Parent window handle: " + parent);
		return parent;
	}

	public void closeMultipleWindows(String parentWindow) {
		Set<String> allWindows = driver.getWindowHandles();
		for (String window : allWindows) {
			if (!window.equals(parentWindow)) {
				try {
					driver.switchTo().window(window);
					driver.close();
				} catch (WebDriverException e) {
					System.err.println("Error closing window.");
				}
			}
		}
		// Always return focus to the parent window after cleanup
		driver.switchTo().window(parentWindow);
	}

	public String switchToChildWindow(String parentWindow) throws Throwable {
	    Set<String> allWindows = driver.getWindowHandles();
	 // Joins all IDs with a comma and a space
	    System.out.println("All Window IDs: " + String.join(", ", allWindows));
	    for (String window : allWindows) {
	        if (!window.equals(parentWindow)) {
	            // Switch the driver's focus to the new window
	            driver.switchTo().window(window);
	            String window2 = window;
	            // Wait for the DOM to settle
	            Thread.sleep(1500);
	            // PRINTS THE WINDOW HANDLE (The unique ID for this specific tab)
	            System.out.println("Child window: " + window2);
	            System.out.println("Child URL: "+driver.getCurrentUrl());
	            return window; 
	        }
	    }
	    System.err.println("No child window found.");
	    return null;
	}

	public void switchBackToParent(String parentWindow) {
		Set<String> allWindows = driver.getWindowHandles();
		for (String window : allWindows) {
			if (!window.equals(parentWindow)) {
				driver.switchTo().window(window);
				System.out.println("Switched to child window: " + driver.getTitle());
				return;
			}
		}
		System.err.println("No child window found.");
	}

	public void switchBackToParent(WebDriver driver, String parentWindow) {
		driver.switchTo().window(parentWindow);
		System.out.println("Switched back to parent window.");
	}

	// ====================================================================================
	// 📋 LIST & DATA EXTRACTION UTILITIES
	// ====================================================================================

	/**
	 * Counts how many elements on the current page match the provided locator.
	 * * @param locator The XPath string to search for.
	 * 
	 * @return int The total number of matching elements found.
	 */
	public int elementCount(String locator) {
		List<WebElement> elements = driver.findElements(By.xpath(locator));
		System.out.println("Found " + elements.size() + " elements for: " + locator);
		return elements.size();
	}

	/**
	 * Extracts text from a specific element inside a List of elements. Includes
	 * robust handling for StaleElementReferenceExceptions. * @param locator The
	 * XPath string matching multiple elements.
	 * 
	 * @param index The 0-based index of the target element in the list.
	 * @return String The text of the target element.
	 */
	public String getTextFromListElement(String locator, int index) {
		List<WebElement> elements = driver.findElements(By.xpath(locator));
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.visibilityOfAllElements(elements));

		try {
			return elements.get(index).getText();
		} catch (StaleElementReferenceException e) {
			// If the DOM refreshed while waiting, re-fetch the list and try again
			elements = driver.findElements(By.xpath(locator));
			return elements.get(index).getText();
		}
	}

	/**
	 * Strips all non-numeric characters from a string and converts it to an
	 * integer. Example: "$1,500" -> 1500 * @param text The raw string containing
	 * numbers.
	 * 
	 * @return int The parsed numeric value.
	 */
	public int textToInteger(String text) {
		// Regex [^0-9] replaces everything that is NOT a digit with an empty string
		int number = Integer.parseInt(text.replaceAll("[^0-9]", ""));
		System.out.println("Converted text to number: " + number);
		return number;
	}

	// ====================================================================================
	// ⚙️ ADVANCED JAVASCRIPT OPERATIONS
	// ====================================================================================

	/**
	 * A universal method to upload any file type by forcefully exposing hidden
	 * input fields. * @param locator The XPath locator for the <input type="file">
	 * element.
	 * 
	 * @param absoluteFilePath The absolute local path to the file to upload.
	 */
	public void uploadAnyFile(String locator, String absoluteFilePath) {
		// 1. Verify the file physically exists on the machine before attempting upload
		File fileToUpload = new File(absoluteFilePath);
		if (!fileToUpload.exists()) {
			throw new IllegalArgumentException("File not found at path: " + absoluteFilePath);
		}

		// 2. Locate the input element
		WebElement uploadInput = driver.findElement(By.xpath(locator));
		scrollToCenter(locator);
		// 3. Force the element to be visible and interactable using JavaScript.
		// This bypasses issues where modern UI frameworks hide the actual file input.
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].style.display = 'block'; arguments[0].style.visibility = 'visible';",
				uploadInput);

		// 4. Send the file path directly to the now-visible input element
		uploadInput.sendKeys(absoluteFilePath);
		System.out.println("Successfully attached file: " + fileToUpload.getName());
	}

	/**
	 * @throws IOException
	 * @throws InterruptedException
	 */
	/**
	 * Uses JavaScript to vertically scroll the viewport until the target element is
	 * placed exactly in the center of the screen. Includes stale element retry.
	 * * @param locator The XPath string of the element to scroll to.
	 */
	public void scrollToCenter(String locator) {
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// Attempt the scroll up to 3 times to handle dynamic React re-renders
		for (int i = 0; i < 3; i++) {
			try {
				// 1. Find the target element
				WebElement element = driver.findElement(By.xpath(locator));

				// 2. Scroll it into the center block of the viewport
				js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);

				// If successful, break out of the retry loop
				break;

			} catch (StaleElementReferenceException e) {
				System.out.println("Stale element caught during scroll. Retrying... (" + (i + 1) + "/3)");
				try {
					Thread.sleep(500);
				} catch (Exception ignored) {
				}
			} catch (Exception e) {
				// For any other unexpected errors, just break so the script doesn't hang
				System.out.println("Could not scroll to element: " + e.getMessage());
				break;
			}
		}
	}

	public void waitTextVisible(String locator) throws IOException, InterruptedException {
		// 1. Find the target element
		WebElement element = driver.findElement(By.xpath(locator));
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOf(element));

		Thread.sleep(1500);

		// Call the method directly without wrapping it in a System.out.println
		getScreenshot("ScreenshotAfterScroll_");
	}

	/**
	 * Waits for an element to be present in the HTML DOM. Use this for hidden
	 * elements (like file uploads) where visibility checks will fail. * @param
	 * locator The XPath string of the element.
	 * 
	 * @return WebElement The located element.
	 */
	public void waitForElementPresence(String locator) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
		System.out.println("Element is present in the DOM: " + locator);
	}
}