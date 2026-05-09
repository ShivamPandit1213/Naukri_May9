package naukrijob;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import login.Login;
import publicmethod.PublicMethod;
import testng_frame.TestNG_Annotations;
import utils.LocatorReader;

public class UploadCV extends TestNG_Annotations {
	@Test(priority = 1)
	public void uploadCV() throws Throwable {
		PublicMethod callMethod = new PublicMethod(driver);
		Login callLogin = new Login(driver);
		callLogin.naukriLogin();

		String profile = LocatorReader.getLocator("profile");
		callMethod.waitForElementVisible(profile);
		driver.findElement(By.xpath(profile)).click();

		String uploadCV = LocatorReader.getLocator("uploadCV");
		callMethod.waitForElementPresence(uploadCV);
		// Selenium's file upload mechanism strictly requires an absolute path (e.g., C:\Users\Name\Project\src\...)
		String fileLocation = "C:\\Users\\shiva\\OneDrive\\Documents\\ShivamCV_Automation.pdf";
		callMethod.uploadAnyFile(uploadCV, fileLocation);
	}
}