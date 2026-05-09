package testng_frame;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import launch.Launch;

public class TestNG_Annotations extends Launch {
//Launch launch;
  @BeforeMethod
  public void beforeMethod() {
	  launchByBrowserName("chrome");
  }

  @AfterMethod
  public void afterMethod() {
	  exitBrowser();
  }
}