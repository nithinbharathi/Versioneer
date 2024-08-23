import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Versioneer {
	private Path currentWorkingDirectory;
	private Path projectPath;
	private Path objectsPath;
	private Path headPath;
	private Path stagingAreaPath;
	
	public Versioneer() {
		this.currentWorkingDirectory = Paths.get("").toAbsolutePath();
		this.projectPath = currentWorkingDirectory.resolve(".versioneer");
		this.objectsPath = this.projectPath.resolve("OBJECTS");
		this.headPath =  this.projectPath.resolve("HEAD");
		this.stagingAreaPath =  this.projectPath.resolve("STAGING");
	}
	
	// Method to create the required meta data folders and files that versioneer makes use of.
	public void init() {
		try {
			Files.createDirectory(projectPath);
			Files.createDirectory(objectsPath);

			Files.createFile(headPath);
			initializeStagingArea();

			System.out.println("Versioneer repository initiliazed...");
		}catch (Exception e){
			e.printStackTrace();

		}		
	}
	
	/*Method that lets users to add the newly introduced changes. This method internally creates a new file within 
	the .versioneer/OBJECTS folder and the file contains the newly added changes. */
	public void add(String fileName) {
		if(validate(fileName)) {
			try {
				Path filePath = currentWorkingDirectory.resolve(fileName);
				byte bytes[] = Files.readAllBytes(filePath);
				
				String content = new String(bytes);
				String hashedContent = hashContent(content);
				
				Path newPath = objectsPath.resolve(hashedContent);
				Files.writeString(newPath, content);
				updateStagingArea(hashedContent);
				
				System.out.println("Add successful");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Invalid filename or file not present in the current project directory...");
		}
	}
	
	//The method generates the SHA hash of the content passed as a parameter.
	private String hashContent(String content) {
        StringBuilder hexString = new StringBuilder();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(content.getBytes());
			
			for(byte b:bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return hexString.toString();
	}
	
	private boolean validate(String fileName) {
		return Files.exists(currentWorkingDirectory.resolve(fileName));
	}
	
	private void updateStagingArea(String hashedContent) {
		List<String> stagingAreaChanges = readStagingArea();
		stagingAreaChanges.add(hashedContent);
		writeStagingArea(stagingAreaChanges);
	}
	
	private void initializeStagingArea() {
		if(!Files.exists(this.stagingAreaPath)) {
			writeStagingArea(new ArrayList<String>());
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<String> readStagingArea() {
		List<String> list = new ArrayList<>();
		try (FileInputStream fileIn = new FileInputStream(this.stagingAreaPath.toString());
	             ObjectInputStream in = new ObjectInputStream(fileIn)) {
	             list = (List<String>) in.readObject();
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
		
		return list;
	}
	
	private void writeStagingArea(List<String> stagingAreaChanges) {
		try (FileOutputStream fileOut = new FileOutputStream(this.stagingAreaPath.toString());
	            ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
	            out.writeObject(stagingAreaChanges);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
}
