import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

//overrides the property "files.basepath" to use temp dir
class BaseDirInitialzer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
			setBasePathAsTempDir(applicationContext);
	}
	
	
	public static void setBasePathAsTempDir(ConfigurableApplicationContext applicationContext) {
		Path tempDirPath;
		try {
			tempDirPath = Files.createTempDirectory("_nasnav_test_");
		} catch (IOException e) {			
			e.printStackTrace();
			tempDirPath = Paths.get("src/test/resources/test_files_base_dir");
		}	
		
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
		        "files.basepath=" + tempDirPath.toString().replace("\\", "/") );
	}

}