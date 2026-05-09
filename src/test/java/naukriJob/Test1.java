package naukrijob;

import org.testng.annotations.Test;

import helper.Chatbot2;
import login.Login;
import publicmethod.PublicMethod;
import testng_frame.TestNG_Annotations;

public class Test1 extends TestNG_Annotations {

	@Test
	public void test1() throws Throwable {
		PublicMethod callMethod = new PublicMethod(driver);
		
		// 1. Log into the application
		Login callLoging = new Login(driver);
		callLoging.naukriLogin();

		// 2. Navigate to the specific job posting
		driver.get("https://www.naukri.com/job-listings-manual-test-engineer-celcom-solutions-global-chennai-1-to-4-years-220426014483?src=sortby&sid=17768397576195063_1&xp=3&px=1&nignbevent_src=jobsearchDeskGNB");
		
		// 3. Wait for the new job page to settle completely
		Thread.sleep(3000);

		// 4. Hand over control to the ChatBot engine!
		// The ChatBot will automatically:
		//   - Run the pre-check (Skip if already applied/wrong role)
		//   - Click the Apply/Interested button
		//   - Answer the Chatbot questions OR log a Direct Apply success.
		Chatbot2 chat = new Chatbot2(driver);
		
		// ✅ UPDATED: Call the newly optimized execute() method
		chat.execute();
		
		// Pause briefly before closing out the test
		Thread.sleep(3000); 
	}
}