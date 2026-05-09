package helper;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import publicmethod.PublicMethod;
import utils.LocatorReader;

public class Filter {
	WebDriverWait wait;
	private WebDriver driver;

	public Filter(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * Uses JavaScript to vertically scroll the viewport until the target element is
	 * placed exactly in the center of the screen. * @param locator The XPath string
	 * of the element to scroll to.
	 * 
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

	public void callFilter() throws Throwable {
		PublicMethod callCustomMethod = new PublicMethod(driver);
		// ====================================================================
		// STEP 3: SEARCH EXECUTION
		// ====================================================================

		// Dynamically wait for the dashboard to load instead of hard sleeping for 8
		// seconds
		wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		String searchFieldStr = LocatorReader.getLocator("searchField");
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(searchFieldStr)));
		callCustomMethod.click(searchFieldStr);
		WebElement keyword = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//input[@placeholder='Enter keyword / designation / companies']")));
		keyword.sendKeys("automation, manual, test, qa, quality assurance, software, ");
		String search = LocatorReader.getLocator("searchSubmit");
		callCustomMethod.waitUntilElementClickable(search);
		driver.findElement(By.xpath(search)).click();

		// ====================================================================
		// STEP 4: EXPERIENCE SLIDER
		// ====================================================================
		String slider = LocatorReader.getLocator("sliderHandle");
		callCustomMethod.waitForElementVisible(slider);
		callCustomMethod.handleSlider(slider, -182);
		Thread.sleep(2000);

		// ====================================================================
		// STEP 5: DYNAMIC POPUP FILTERS (Department, Role, Industry)
		// Note: We strictly use explicit waits here because React aggressively
		// re-renders the DOM upon keystrokes, which causes StaleElementExceptions.
		// ====================================================================
		// --- Department Filter ---
		String viewMoreDepartment = LocatorReader.getLocator("viewMoreDepartment");
		scrollToCenter(viewMoreDepartment);
		WebElement deptBtn = callCustomMethod.waitUntilElementClickable(viewMoreDepartment);
		deptBtn.click();

		String searchDepartment = LocatorReader.getLocator("searchDepartment");
		WebElement deptSearchBox = callCustomMethod.waitForElementVisible(searchDepartment);
		Thread.sleep(500);

		deptSearchBox.sendKeys("Engineering - Software & QA");
		String engQA_Department = LocatorReader.getLocator("engQA_Department");
		callCustomMethod.waitForElementVisible(engQA_Department).click();

		deptSearchBox.clear();
		deptSearchBox.sendKeys("Quality Assurance");
		String qa_Department = LocatorReader.getLocator("qa_Department");
		callCustomMethod.waitForElementVisible(qa_Department).click();

		String applyFilter = LocatorReader.getLocator("applyFilter");
		WebElement applyBtnDept = callCustomMethod.waitUntilElementClickable(applyFilter);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyBtnDept); // Force click Apply
		Thread.sleep(2000); // Allow popup to fully dismantle before scrolling to next filter

		// --- Role Filter ---
		String viewMoreRole = LocatorReader.getLocator("viewMoreRole");
		scrollToCenter(viewMoreRole);
		WebElement roleBtn = callCustomMethod.waitUntilElementClickable(viewMoreRole);
		roleBtn.click();

		String searchRole = LocatorReader.getLocator("searchRole");
		WebElement enterRole = callCustomMethod.waitForElementVisible(searchRole);
		Thread.sleep(500);

		enterRole.sendKeys("Quality Assurance and Testing");
		String qa_Role = LocatorReader.getLocator("qa_Role");
		callCustomMethod.waitForElementVisible(qa_Role).click();

		enterRole.clear();
		enterRole.sendKeys("Quality Assurance - Other");
		String qaOther_Role = LocatorReader.getLocator("qaOther_Role");
		callCustomMethod.waitForElementVisible(qaOther_Role).click();

		enterRole.clear();
		enterRole.sendKeys("Software Development");
		String softwareDevelopment_Role = LocatorReader.getLocator("softwareDevelopment_Role");
		callCustomMethod.waitForElementVisible(softwareDevelopment_Role).click();

		enterRole.clear();
		enterRole.sendKeys("Business Process Quality");
		String businessProcess_Role = LocatorReader.getLocator("businessProcess_Role");
		callCustomMethod.waitForElementVisible(businessProcess_Role).click();

		enterRole.clear();
		enterRole.sendKeys("DevOps");
		String devOps_Role = LocatorReader.getLocator("devOps_Role");
		callCustomMethod.waitForElementVisible(devOps_Role).click();

		WebElement applyBtnRole = callCustomMethod.waitUntilElementClickable(applyFilter);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyBtnRole); // Force click Apply
		Thread.sleep(2000);

		// --- Industry Filter ---
		String viewMoreIndustry = LocatorReader.getLocator("viewMoreIndustry");
		scrollToCenter(viewMoreIndustry);
		WebElement indBtn = callCustomMethod.waitUntilElementClickable(viewMoreIndustry);
		indBtn.click();

		String searchIndustry = LocatorReader.getLocator("searchIndustry");
		WebElement enterIndustry = callCustomMethod.waitForElementVisible(searchIndustry);
		Thread.sleep(500);

		enterIndustry.sendKeys("IT Services & Consulting");
		String itService_Industry = LocatorReader.getLocator("itService_Industry");
		callCustomMethod.waitForElementVisible(itService_Industry).click();

		enterIndustry.clear();
		enterIndustry.sendKeys("Software Product");
		String software_Industry = LocatorReader.getLocator("software_Industry");
		callCustomMethod.waitForElementVisible(software_Industry).click();

		WebElement applyBtnInd = callCustomMethod.waitUntilElementClickable(applyFilter);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyBtnInd); // Force click Apply
		Thread.sleep(2000);

		// ====================================================================
		// STEP 4: SORT BY DATE
		// ====================================================================
		String filter = LocatorReader.getLocator("filter");
		scrollToCenter(filter);
		Thread.sleep(3000); // Short sleep kept to allow scrolling animation to finish
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(filter))).click();
		callCustomMethod.getScreenshot();
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[@title='Date']/a"))).click();
		System.out.println("Sorted by Date");
	}
}