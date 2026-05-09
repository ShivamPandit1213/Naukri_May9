package helper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import publicmethod.PublicMethod;
import utils.LocatorReader;

public class Chatbot2 {
    WebDriver driver;
    PublicMethod callMethod;

    // ====================================================================
    // 1. CONSTRUCTOR
    // ====================================================================
    public Chatbot2(WebDriver driver) {
        this.driver = driver;
    }

    // ====================================================================
    // 2. MAIN ENTRY POINT
    // ====================================================================
    public void execute() throws Throwable {
        preCondition();
    }

    // ====================================================================
    // 3. JOB SCREENING & PRE-CHECKS
    // ====================================================================
    private void preCondition() throws Throwable {
        callMethod = new PublicMethod(driver);
        String callHeading = LocatorReader.getLocator("jobHeading");

        // WHY: Zero-Wait ensures we don't hang for 10s if the job isn't already applied.
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        boolean isAlreadyApplied = !driver.findElements(By.xpath(LocatorReader.getLocator("alreadyAppliedXpath"))).isEmpty();
        boolean isPrefersWomen = !driver.findElements(By.xpath(LocatorReader.getLocator("prefersWomenXpath"))).isEmpty();
        boolean isPythonTitle = !driver.findElements(By.xpath(LocatorReader.getLocator("pythonTitleXpath"))).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        if (isAlreadyApplied || isPrefersWomen || isPythonTitle) {
            System.out.println("🚫 Job skipped: Matches negative criteria.");
            return; 
        }

        String applyInterested = LocatorReader.getLocator("applyBtnXpath") + " | " + LocatorReader.getLocator("interestedBtnXpath");

        if (!driver.findElements(By.xpath(applyInterested)).isEmpty()) {
            WebElement callApplyInterested = driver.findElement(By.xpath(applyInterested));
            if (callApplyInterested.isEnabled() && callApplyInterested.isDisplayed()) {
                fetchHeadings(callHeading);
                scrollToCenter(callHeading);
                
                // JS Click bypasses potential UI overlays
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", callApplyInterested);
                System.out.println("👆 Clicked the Apply/Interested button.");

                try {
                    WebElement platformlogo = callMethod.getElement("platformlogo");
                    if (platformlogo.isDisplayed()) {
                        Thread.sleep(1500);
                        System.out.println("[Chatbot] Starting to execute....");
                        processChatbot(); 
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Chatbot screen is not displayed.");
                }
            }
        }
    }

    // ====================================================================
    // 4. THE CHATBOT ENGINE LOOP
    // ====================================================================
    private void processChatbot() {
        boolean isChatbotActive = true;
        int questionCount = 0;
        while (isChatbotActive && questionCount < 15) {
            try {
                fetchQuestion();
                Thread.sleep(800); 

                // WHY: We use a short wait to find the Save button that just appeared.
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
                String saveXpath = LocatorReader.getLocator("saveChatBtn");
                List<WebElement> saveElements = driver.findElements(By.xpath(saveXpath));
                
                for (WebElement saveBtn : saveElements) {
                    if (saveBtn.isDisplayed()) {
                        try {
                            saveBtn.click();
                        } catch (Exception ex) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
                        }
                        System.out.println("Clicked on Save button.");
                        break; 
                    }
                }
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                Thread.sleep(2000); 
                questionCount++;
            } catch (Exception e) {
                System.out.println("✅ Chatbot processing completed.");
                isChatbotActive = false;
            }
        }
    }

    // ====================================================================
    // 5. DATA MAPPING & ROUTING
    // ====================================================================
    private void fetchQuestion() {
        WebElement latestQuestion = callMethod.getElement("latestQuestion");
        String questionPrint = latestQuestion.getText();

        if (questionPrint.toLowerCase().contains("thank you")) {
            System.out.println(" ------ " + questionPrint + " ------");
            throw new RuntimeException("Finished"); 
        }

        // Detect if active input is Radio, Checkbox, or Text
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        boolean hasRadio = false;
        for (WebElement el : driver.findElements(By.xpath(LocatorReader.getLocator("radioType")))) {
            if (el.isDisplayed()) { hasRadio = true; break; }
        }
        boolean hasMultiCheckbox = false;
        for (WebElement el : driver.findElements(By.xpath(LocatorReader.getLocator("multiCheckBox")))) {
            if (el.isDisplayed()) { hasMultiCheckbox = true; break; }
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        String inputType = hasRadio ? "Radio" : (hasMultiCheckbox ? "MultiCheckbox" : "TextField");
        System.out.println("[" + inputType + "] Ques. \"" + questionPrint + "\"");

        // WHERE VALUES COME FROM: Candidate Personal Profile Map
        Map<String, String> myApplicationData = new HashMap<>();
        myApplicationData.put("notice", "15 days or less");
        myApplicationData.put("experience", "3.9 Years"); 
        
        // Priority logic: "Expected" keyword will take 8.5, others will take 5.2
        myApplicationData.put("expected", "8.5 LPA"); 
        myApplicationData.put("current", "5.2 LPA"); 
        myApplicationData.put("ctc", "5.2 LPA"); 

        myApplicationData.put("location", "Delhi NCR");
        myApplicationData.put("playwright", "Yes"); 
        myApplicationData.put("relocate", "All"); 
        myApplicationData.put("manual testing", "3-5 years"); 

        if (hasRadio) {
            radioField(questionPrint, myApplicationData);
        } else if (hasMultiCheckbox) {
			multiCheckboxField(questionPrint, myApplicationData);
		} else {
            fillTextFields(questionPrint, myApplicationData);
        }
    }

    // ====================================================================
    // 6. INPUT HANDLERS
    // ====================================================================

    private void radioField(String questionText, Map<String, String> formData) {
        String lowerCaseQuestion = questionText.toLowerCase();
        String targetValue = ""; 

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (lowerCaseQuestion.contains(entry.getKey().toLowerCase())) {
                targetValue = entry.getValue();
                break; 
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        boolean isClicked = false;

        // Dynamic Label Search
        if (!targetValue.isEmpty()) {
            try {
                String dynamicXpath = "//label[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + targetValue.toLowerCase() + "')]";
                List<WebElement> labels = driver.findElements(By.xpath(dynamicXpath));
                for (WebElement label : labels) {
                    if (label.isDisplayed()) {
                        try { label.click(); } catch (Exception e) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
                        }
                        System.out.println("[Radio] Ans. clicked on \"" + label.getText() + "\"");
                        isClicked = true;
                        break;
                    }
                }
            } catch (Exception e) { }
        }

        // Fallback to static locators from properties
        if (!isClicked) {
            String[] radioKeys = { "noticePeriod_Radio", "salutation_Radio", "howManyYears_Radio" };
            for (String key : radioKeys) {
                try {
                    WebElement radioBtn = callMethod.getElement(key);
                    if (radioBtn != null && radioBtn.isDisplayed()) {
                        try { radioBtn.click(); } catch (Exception e) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radioBtn);
                        }
                        System.out.println("[Radio] Ans. clicked via Locator: " + key);
                        isClicked = true;
                        break; 
                    }
                } catch (Exception e) { }
            }
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

	public void multiCheckboxField(String questionText, Map<String, String> formData) {
		String lowerCaseQuestion = questionText.toLowerCase();
		String targetValues = ""; 
		for (Map.Entry<String, String> entry : formData.entrySet()) {
			if (lowerCaseQuestion.contains(entry.getKey().toLowerCase())) {
				targetValues = entry.getValue();
				break; 
			}
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1)); 
		try {
			String containerXpath = LocatorReader.getLocator("multiCheckBox");
			if (targetValues.equalsIgnoreCase("All")) {
				List<WebElement> allLabels = driver.findElements(By.xpath(containerXpath + "//label"));
				for (WebElement label : allLabels) {
					if (label.isDisplayed()) {
						try { label.click(); } catch (Exception e) {
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
						}
						System.out.println("[MultiCheckbox] Ans. checked \"" + label.getText() + "\"");
					}
				}
			} else {
				String[] answers = targetValues.split(",");
				for (String answer : answers) {
					String specificOptionXpath = containerXpath + "//*[contains(text(), '" + answer.trim() + "')]";
					List<WebElement> options = driver.findElements(By.xpath(specificOptionXpath));
					for (WebElement option : options) {
						if (option.isDisplayed()) {
							try { option.click(); } catch (Exception e) {
								((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
							}
							break;
						}
					}
				}
			}
		} catch (Exception e) { }
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); 
	}

    public void fillTextFields(String questionText, Map<String, String> formData) {
        String lowerCaseQuestion = questionText.toLowerCase();
        String textToType = "NA"; 

        // WHY: Longest match logic handles "ctc" vs "expected ctc" overlap
        int longestKey = 0;
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (lowerCaseQuestion.contains(key)) {
                if (key.length() > longestKey) {
                    textToType = entry.getValue();
                    longestKey = key.length();
                }
            }
        }

        try {
            // Note: We use a general input XPath but filter for the one that is currently visible.
            String chatInputXpath = "//div[@contenteditable='true' and contains(@class,'textArea')]";
            List<WebElement> textAreas = driver.findElements(By.xpath(chatInputXpath));
            WebElement activeTextArea = null;

            // Search backwards to find the bottom-most (latest) visible input
            for (int i = textAreas.size() - 1; i >= 0; i--) {
                if (textAreas.get(i).isDisplayed()) {
                    activeTextArea = textAreas.get(i);
                    break;
                }
            }

            if (activeTextArea != null) {
                activeTextArea.clear(); 
                activeTextArea.sendKeys(textToType);
                System.out.println("[TextField] Ans. \"" + textToType + "\"");
            }
        } catch (Exception e) { }
    }

    // ====================================================================
    // 7. HELPER UTILS
    // ====================================================================
    private void scrollToCenter(String xpathLocator) {
        WebElement element = driver.findElement(By.xpath(xpathLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest'});", element);
        try { Thread.sleep(500); } catch (Exception e) { }
    }

    private void fetchHeadings(String xpathLocator) {
        String getHeading = driver.findElement(By.xpath(xpathLocator)).getText();
        System.out.println("Current apply job page Heading: " + getHeading);
    }
}