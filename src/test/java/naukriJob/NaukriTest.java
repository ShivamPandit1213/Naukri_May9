package naukrijob; // Capital J to match Maven/TestNG expectations

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import helper.Chatbot2;
import helper.Filter;
import login.Login;
import publicmethod.PublicMethod;
import testng_frame.TestNG_Annotations;
import utils.LocatorReader;

public class NaukriTest extends TestNG_Annotations {
	Actions action;

	static String nextPagi = "//span[text()='Next']/parent::a";

	public void scrollToBottom(WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	public void scrollToTop(String locator) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollTo(0, 0);");
	}

	@Test(priority = 2)
	public void nokri() throws Throwable {
		PublicMethod callCustomMethod = new PublicMethod(driver);
		Chatbot2 callChatBot = new Chatbot2(driver);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		driver.get("https://www.naukri.com/");
		System.out.println("Naukri launched: " + driver.getCurrentUrl());

		Login callLoging = new Login(driver);
		callLoging.naukriLogin();

		Filter callFilter = new Filter(driver);
		callFilter.callFilter();

		int currentPage = 1;
		int maxPages = 50;
		String parentWindow = callCustomMethod.getParentWindow();
		String jobXpath = LocatorReader.getLocator("jobHeadings");

		// Outer Loop: Handles Pagination
		for (int i = 0; i < maxPages; i++) {
			System.out.println("\n=========== Page: " + (i + currentPage) + " ==========");
			List<WebElement> jobs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(jobXpath)));
			System.out.println("Total jobs: " + jobs.size());

			// Inner Loop: Handles clicking each job on the current page
			for (int j = 0; j < jobs.size(); j++) {

				try {
					wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(jobXpath)));
				} catch (Exception e) {
					System.out.println("Job cards took too long to load on parent window.");
				}

				List<WebElement> jobsFresh = driver.findElements(By.xpath(jobXpath));

				if (jobsFresh.isEmpty() || j >= jobsFresh.size()) {
					System.out.println("Job list is empty or index is out of bounds. Breaking to next page.");
					break;
				}

				WebElement job = jobsFresh.get(j);
				System.out.println("----------------------------------");
				System.out.println("Job " + (j + 1) + ": " + job.getText());

				Duration originalWait = driver.manage().timeouts().getImplicitWaitTimeout();

				try {
					// Drop wait to 0 to blast through parent window interactions
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

					callCustomMethod.closeMultipleWindows(parentWindow);
					job.click();

					// Switch context to the new job tab
					callCustomMethod.switchToChildWindow(parentWindow);

					// RESTORE wait so ChatBot elements can actually be found
					driver.manage().timeouts().implicitlyWait(originalWait);
					
					callChatBot.execute();

				} catch (RuntimeException e) {
					System.out.println("Handled RuntimeException during interaction.");
				} finally {
					// Safety net: ensure implicit wait is always restored
					driver.manage().timeouts().implicitlyWait(originalWait);

					// Close the child window and return context to the main page
					if (!driver.getWindowHandle().equals(parentWindow)) {
						System.out.println("🔄 Closing job tab and returning to parent list...");
						driver.close();
						driver.switchTo().window(parentWindow);
					}
				}
			}

			// =======================================================
			// PAGINATION LOGIC (Clicking the 'Next' button)
			// =======================================================
			try {
				System.out.println("➡️ Moving to the next page...");
				WebElement nextButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(nextPagi)));
				
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("arguments[0].scrollIntoView({block: 'center'});", nextButton);
				Thread.sleep(1000); 
				js.executeScript("arguments[0].click();", nextButton);
				
				// Wait for the new page of jobs to load
				Thread.sleep(4000); 
				
			} catch (Exception e) {
				System.out.println("✅ Reached the last page or 'Next' button not found. Exiting pagination loop.");
				break; 
			}
		}
	}
}