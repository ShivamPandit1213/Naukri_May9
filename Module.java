// Search

// Filter
package naukriJob;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import helper.ChatBot;
import publicMethod.PublicMethod;
import testng_frame.TestNG_Annotations;

public class NaukriTest2 extends TestNG_Annotations {

	// Locating the 'Next' button for pagination logic
	static String nextPagi = "//span[text()='Next']/parent::a";

	/**
	 * Utility method to bring an element into the center of the screen.
	 * Helps avoid "ElementClickIntercepted" errors caused by sticky headers.
	 */
	public void scrollToElement(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
	}

	@Test
	public void nokri() throws Throwable {
		// Initialize helper objects and explicit wait
		PublicMethod callCustomMethod = new PublicMethod(driver);
		ChatBot callChatBot = new ChatBot(driver);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		// STEP 1: Launch Naukri home page
		driver.get("https://www.naukri.com/");
		Thread.sleep(5000);

		// STEP 2: Trigger the search bar and input keywords
		callCustomMethod.click("//span[text()='Search jobs here']/following-sibling::button");
		WebElement keyword = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//input[@placeholder='Enter keyword / designation / companies']")));
		keyword.sendKeys("Automation Testing, Selenium");
		driver.findElement(By.xpath("//span[text()='Search']//ancestor::button")).click();

		// STEP 3: Handle the Experience Slider (Filters out senior/irrelevant roles)
		String slider = "//div[@class='slider-Container']//div[contains(@class, 'handle')]";
		callCustomMethod.waitForElementVisible(slider);
		callCustomMethod.handleSlider(slider, -182); // Dragging slider to set experience range

		// STEP 4: Apply 'Freshness' filter (Targets jobs posted in the last 7 days)
		Thread.sleep(2000);
		WebElement latest = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//span[text()='Freshness']/parent::div/following-sibling::div//button")));
		latest.click();
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Last 7 days']/parent::a"))).click();
		System.out.println("Filter applied: Last 7 days");

		// STEP 5: Sort results by Date (To see the most recent postings first)
		Thread.sleep(3000);
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//button[@id='filter-sort' or @title='Recommended']"))).click();
		callCustomMethod.getScreenshot(); // Evidence of current state
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[@title='Date']/a"))).click();
		System.out.println("Sorted by Date");

		// Get the Main Search Window handle to return to after applying to jobs
		String parentWindow = driver.getWindowHandle();
		String jobXpath = "//div[@class='srp-jobtuple-wrapper']//div[contains(@class,'row1')]//a";

		// OUTER LOOP: Handles pagination (Moving from Page 1 to Page 2, etc.)
		while (true) {
			// Identify all job links on the current page
			List<WebElement> jobs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(jobXpath)));

			// INNER LOOP: Processes each job card one by one
			for (int j = 0; j < jobs.size(); j++) {
				// Re-locating elements to avoid StaleElementReferenceException after page updates
				List<WebElement> jobsFresh = driver.findElements(By.xpath(jobXpath));
				WebElement job = jobsFresh.get(j);

				// Prepare to click the job
				scrollToElement(job);
				Thread.sleep(2000);
				job.click(); // Opens job details in a NEW TAB

				// WINDOW SWITCHING: Move focus to the job details tab
				String childWindow = "";
				Set<String> allHandles = driver.getWindowHandles();
				for (String handle : allHandles) {
					if (!handle.equals(parentWindow)) {
						childWindow = handle;
						driver.switchTo().window(childWindow);
					}
				}

				try {
					// SKIP LOGIC: Determine if the job is worth applying for
					String applySiteX = "//button[contains(text(),'Apply on company site')]";
					String femaleX = "//*[contains(text(),'women') or contains(text(),'Female')]";

					// Check if job redirects to an external site or is gender-specific hiring
					if (!driver.findElements(By.xpath(applySiteX)).isEmpty()
							|| !driver.findElements(By.xpath(femaleX)).isEmpty()) {
						System.out.println("⏩ Skipping: External site redirection or Gender-specific job.");
						continue; // Jumps directly to 'finally' to close tab
					}

					// APPLY LOGIC: Identify which button type is available
					List<WebElement> apply = driver.findElements(By.xpath("(//button[text()='Apply'])[1]"));
					List<WebElement> interested = driver
							.findElements(By.xpath("(//button[text()='I am interested'])[1]"));

					// Trigger the application process
					if (!apply.isEmpty() && apply.get(0).isDisplayed()) {
						apply.get(0).click();
						callChatBot.processChat(); // Handover to ChatBot logic for recruiter questions
					} else if (!interested.isEmpty() && interested.get(0).isDisplayed()) {
						interested.get(0).click();
						callChatBot.processChat();
					}

				} catch (Exception e) {
					System.out.println("Job Error: " + e.getMessage());
				} finally {
					// SAFE CLEANUP: Ensure the job detail tab is closed regardless of success or failure
					try {
						if (driver.getWindowHandles().size() > 1) {
							driver.switchTo().window(childWindow);
							driver.close();
						}
					} catch (Exception e) {
						// Window might have been closed by ChatBot already
					}
					// Return focus to the search results list
					driver.switchTo().window(parentWindow);
				}
			}

			// PAGINATION: Try moving to the next set of jobs
			List<WebElement> nextBtn = driver.findElements(By.xpath(nextPagi));
			if (!nextBtn.isEmpty() && !nextBtn.get(0).getAttribute("class").contains("disabled")) {
				nextBtn.get(0).click(); // Click 'Next'
				Thread.sleep(4000); // Wait for results to refresh
			} else {
				System.out.println("No more pages found.");
				break; // Exit outer loop
			}
		}
	}
}
