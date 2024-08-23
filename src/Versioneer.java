import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Versioneer {
	private Path projectPath;
	private String objectsPath;
	private String headPath;
	private String stagingPath;
	
	public Versioneer() {
		this.projectPath = Paths.get("").toAbsolutePath().resolve(".versioneer");
		this.objectsPath = this.projectPath + "";
		this.headPath =  this.projectPath + "";
		this.stagingPath =  this.projectPath+ "";
	}
	
	// Method to create the required meta data folders and files that versioneer makes use of.
	public void init() {
		try {
			Files.createDirectory(projectPath);
			System.out.println("versioneer repository initiliazed...");
		}catch (Exception e){
			e.printStackTrace();

		}		
	}
	
	public void add() {}
	
	
}
