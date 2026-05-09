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

    // Locator for the 'Next' button to handle multi-page job results
    static String nextPagi = "//span[text()='Next']/parent::a";

    /**
     * Helper: Scrolls the page so the specific element is centered.
     * This prevents 'ElementClickInterceptedException' caused by sticky headers or footers.
     */
    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    @Test
    public void nokri() throws Throwable {
        // Initialization of helper classes and Explicit Wait (15s for network latency)
        PublicMethod callCustomMethod = new PublicMethod(driver);
        ChatBot callChatBot = new ChatBot(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // ================= STEP 1: INITIAL NAVIGATION =================
        // Browser is launched via @BeforeMethod. We navigate to the home page here.
        driver.get("https://www.naukri.com/");
        System.out.println("Naukri launched: " + driver.getCurrentUrl());

        // ================= STEP 2: SEARCH EXECUTION =================
        // 1. Wait for homepage scripts to settle, then click 'Search' to open the input field
        Thread.sleep(8000); 
        callCustomMethod.click("//span[text()='Search jobs here']/following-sibling::button");

        // 2. Input keywords into the search bar
        WebElement keyword = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Enter keyword / designation / companies']")));
        keyword.sendKeys("Automation Testing, Selenium, QA Automation");

        // 3. Click the actual Search button to load the Results page
        driver.findElement(By.xpath("//span[text()='Search']//ancestor::button")).click();

        // ================= STEP 3: RESULT FILTERING =================
        // Adjust the Experience slider to target specific career levels (e.g., 2-5 years)
        String slider = "//div[@class='slider-Container']//div[contains(@class, 'handle')]";
        callCustomMethod.waitForElementVisible(slider);
        callCustomMethod.handleSlider(slider, -182);

        // ================= STEP 4: SORTING LOGIC =================
        // We sort by 'Date' so the newest postings appear first, making 'Freshness' filters redundant.
        Thread.sleep(3000);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='filter-sort' or @title='Recommended']"))).click();

        // Take a screenshot of the applied filters for logging/evidence
        callCustomMethod.getScreenshot(); 
        
        // Select 'Date' from the sorting dropdown
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[@title='Date']/a"))).click();
        System.out.println("Results sorted by Date successfully.");

        // ================= STEP 5: PREPARE ITERATION =================
        // Save the Search Results handle so we can always return to it after closing job tabs
        String parentWindow = driver.getWindowHandle();
        String jobXpath = "//div[@class='srp-jobtuple-wrapper']//div[contains(@class,'row1')]//a";

        // OUTER LOOP: Navigates through pages (Page 1, Page 2, etc.)
        while (true) {
            // Wait until job cards are fully rendered on the page
            List<WebElement> jobs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(jobXpath)));
            System.out.println("Processing Page Jobs: " + jobs.size());

            // INNER LOOP: Processes each individual job card
            for (int j = 0; j < jobs.size(); j++) {
                // Re-locate elements to prevent 'StaleElementReferenceException' after DOM updates
                List<WebElement> jobsFresh = driver.findElements(By.xpath(jobXpath));
                WebElement job = jobsFresh.get(j);
                
                // Bring job into view and open in a new browser tab
                scrollToElement(job);
                Thread.sleep(2000);
                job.click(); 

                // WINDOW MANAGEMENT: Identify and switch to the new job detail tab
                String childWindow = "";
                Set<String> allHandles = driver.getWindowHandles();
                for (String handle : allHandles) {
                    if (!handle.equals(parentWindow)) {
                        childWindow = handle;
                        driver.switchTo().window(childWindow);
                    }
                }

                try {
                    // ================= STEP 6: JOB-LEVEL SKIP LOGIC =================
                    // XPath for External sites and Diversity/Female-focused hiring
                    String applySiteX = "//button[contains(text(),'Apply on company site')]";
                    String femaleX = "//*[contains(text(),'women') or contains(text(),'Female')]";

                    // If job is external or gender-specific, close tab and move to next
                    if (!driver.findElements(By.xpath(applySiteX)).isEmpty() || 
                        !driver.findElements(By.xpath(femaleX)).isEmpty()) {
                        System.out.println("⏩ Skipping: Job is either External or Diversity-focused.");
                        continue; // Triggers the 'finally' block to return to parent
                    }

                    // ================= STEP 7: APPLICATION ACTIONS =================
                    List<WebElement> apply = driver.findElements(By.xpath("(//button[text()='Apply'])[1]"));
                    List<WebElement> interested = driver.findElements(By.xpath("(//button[text()='I am interested'])[1]"));

                    // Click Apply/Interested and hand over control to the ChatBot logic
                    if (!apply.isEmpty() && apply.get(0).isDisplayed()) {
                        apply.get(0).click();
                        callChatBot.processChat(); 
                    } 
                    else if (!interested.isEmpty() && interested.get(0).isDisplayed()) {
                        interested.get(0).click();
                        callChatBot.processChat();
                    }

                    // ================= STEP 8: SUCCESS VERIFICATION =================
                    // If a success checkmark appears, we close the tab immediately to save time
                    String successCheck = "//span[contains(@class,'naukicon-check')]";
                    if (!driver.findElements(By.xpath(successCheck)).isEmpty()) {
                        System.out.println("✅ Success checkmark found. Closing window.");
                        driver.close();
                        childWindow = ""; // Signifies that the window is already closed
                    }

                } catch (Exception e) {
                    System.out.println("⚠️ Job Interaction Error: " + e.getMessage());
                } finally {
                    // ================= STEP 9: TAB CLEANUP =================
                    // Ensures we never leave orphan tabs open and always return to the search list
                    try {
                        if (driver.getWindowHandles().size() > 1 && !childWindow.isEmpty()) {
                            driver.switchTo().window(childWindow);
                            driver.close();
                        }
                    } catch (Exception e) {
                        // Handle cases where ChatBot already closed the window
                    }
                    driver.switchTo().window(parentWindow);
                }
            }
            
            // ================= STEP 10: PAGINATION =================
            // Check if the 'Next' button is present and clickable
            List<WebElement> nextBtn = driver.findElements(By.xpath(nextPagi));
            if (!nextBtn.isEmpty() && !nextBtn.get(0).getAttribute("class").contains("disabled")) {
                nextBtn.get(0).click();
                Thread.sleep(4000); // Wait for the new page of results to load
            } else {
                System.out.println("🏁 All pages processed.");
                break; // Exit the loop
            }
        }
    }
}