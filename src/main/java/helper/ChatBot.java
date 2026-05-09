package helper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChatBot {

    WebDriver driver;
    WebDriverWait wait;
    Map<String, String> answers; 

    // ====================================================================
    // 1. INITIALIZATION
    // ====================================================================

    public ChatBot(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.answers = new HashMap<>();
        
        // Dictionary of your standard answers
        answers.put("experience", "3.9 Years"); 
        answers.put("notice", "Immediate"); 
        answers.put("location", "Delhi NCR"); 
    }

    // ====================================================================
    // 2. PUBLIC EXECUTION ENGINE
    // ====================================================================

    /**
     * Main entry point for the chat bot. 
     * To be called strictly AFTER the Apply button has been clicked.
     */
    public void handleChat() {
        System.out.println("[ChatBot] : Starting chat sequence...");
        startPollingSequence();
    }

    // ====================================================================
    // 3. THE CHAT LOOP
    // ====================================================================

    private void startPollingSequence() {
        try {
            Thread.sleep(2000); // Allow chat UI to slide in
            
            String inputLocator = "//*[@placeholder='Type message here...']"; 
            
            if (!driver.findElements(By.xpath(inputLocator)).isEmpty()) {
                
                for (int i = 1; i <= 5; i++) { 
                    System.out.println("\n[ChatBot] --- Polling Iteration: " + i + " ---");
                    
                    String questionXPath = "(//div[contains(@class, 'bot-message') or contains(@class, 'question')])[last()]";
                    List<WebElement> questions = driver.findElements(By.xpath(questionXPath));
                    
                    if (questions.isEmpty()) break; 
                    
                    String questionText = questions.get(0).getText();
                    System.out.println("Question: " + questionText);
                    
                    String answer = determineAnswer(questionText);
                    System.out.println("Typing: " + answer);

                    WebElement chatBox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputLocator)));
                    
                    try { chatBox.click(); Thread.sleep(500); } catch (Exception ignored) {}
                    
                    typeInReactInput(chatBox, answer);
                    chatBox.sendKeys(Keys.ENTER);
                    
                    Thread.sleep(2500); // Wait for bot to process and reply
                }
            } else {
                System.out.println("[ChatBot] : No chat input detected. Proceeding...");
            }
        } catch (Exception e) {
            System.out.println("[ChatBot] : Sequence aborted: " + e.getMessage());
        }
    }

    // ====================================================================
    // 4. UTILITY HELPERS
    // ====================================================================

    private String determineAnswer(String questionText) {
        String q = questionText.toLowerCase();
        if (q.contains("experience") || q.contains("years")) return answers.get("experience");
        if (q.contains("notice") || q.contains("join")) return answers.get("notice");
        if (q.contains("location") || q.contains("city")) return answers.get("location");
        return "Yes"; // Default safe answer
    }

    private void typeInReactInput(WebElement inputElement, String text) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = 
                "let input = arguments[0]; let text = arguments[1];" +    
                "let proto = input.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;" +
                "let setter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
                "if (setter) { setter.call(input, text); } else { input.value = text; }" +
                "input.dispatchEvent(new Event('input', { bubbles: true }));" +
                "input.dispatchEvent(new Event('change', { bubbles: true }));";
                
            js.executeScript(script, inputElement, text);
        } catch (Exception e) {
            System.out.println("[ChatBot] : JS Injector failed.");
        }
    }
}