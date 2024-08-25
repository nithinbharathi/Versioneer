import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
			clearExistingDirectories();
			createDirectories();			
			initializeHeadAndStagingArea();

			System.out.println("Versioneer repository initiliazed...");
		}catch (Exception e){
			e.printStackTrace();

		}		
	}
	
	private void clearExistingDirectories() throws IOException {
		 Files.walk(this.projectPath)
         	  .sorted(Comparator.reverseOrder()) // ensure directories are deleted after their contents
         	  .map(Path::toFile)
         	  .forEach(File::delete);
	}
	
	private void createDirectories() throws Exception{
		Files.createDirectory(projectPath);
		Files.createDirectory(objectsPath);
	}
	
	/* Method that lets users to add the newly introduced changes. This method internally creates a new file within 
	the .versioneer/OBJECTS folder and the file contains the newly added changes. */
	public void add(String fileName) {
		if(validate(fileName)) {
			try {
				Path filePath = currentWorkingDirectory.resolve(fileName);
				byte bytes[] = Files.readAllBytes(filePath);
				
				String content = new String(bytes);
				String hashedContent = hashContent(content);
				
				Path newPath = objectsPath.resolve(hashedContent);
				
				write(newPath, content);
				updateStagingArea(hashedContent);
				
				System.out.println("Add successful");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Invalid filename or file not present in the current project directory...");
		}
	}
	
	// The command that is used to move all of the staging area changes into one final change.
	public void commit(String message) {
		
		List<String> changedFiles = readStagingArea();
		
		if(changedFiles.isEmpty()) {
			System.out.println("Add some changes before commit...");
			return;
		}
		
		String parentCommit = readHeadData();
		
		CommitData commitData = new CommitData(message, changedFiles, parentCommit, LocalDateTime.now());
		String commitHash = hashContent(commitData.toString());
		Path commitObjectPath = objectsPath.resolve(commitHash);
		
		writeCommitData(commitObjectPath, commitData);
		writeStagingArea(new ArrayList<String>());
		writeHeadData(commitHash);
		
		System.out.println("Commit successful with commit hash: "+commitHash);
			
	}
	
	public void log() {
		String parentCommit = readHeadData();
		CommitData commitData = readCommitData(objectsPath.resolve(parentCommit));
		
		while(commitData != null) {
			System.out.println("Changed Files: "+commitData.getChangedFiles() 
								+"\nCommit Message: "+commitData.getMessage()
								+"\nCreated Time: "+commitData.getCreatedTime()
								+"\n-----------------------------------------------------");
			
			if(commitData.getParentCommit() != null && commitData.getParentCommit().length() != 0)
				commitData = readCommitData(objectsPath.resolve(commitData.getParentCommit()));
			else 
				break;
		}
	}
	
	public void showDiff(String commitHash1, String commitHash2) {
		if(Files.exists(objectsPath.resolve(commitHash1)) && Files.exists(objectsPath.resolve(commitHash2))) {
			CommitData commitData1 = readCommitData(objectsPath.resolve(commitHash1));
			CommitData commitData2 = readCommitData(objectsPath.resolve(commitHash2));
			
			List<String>filesChanged1 = commitData1.getChangedFiles();
			List<String>filesChanged2 = commitData2.getChangedFiles();
			
			printDiff(filesChanged1, filesChanged2);
		}else {
			System.out.println("one of the commit hash or both are invalid");
		}
	}
	
	// TODO: only changes in the content that was changed should be highlighted.
	private void printDiff(List<String>files1 , List<String>files2) {
	   final String BLUE = "\033[0;34m"; 
	   final String GREEN = "\033[0;32m";   // GREEN
	   final String RESET = "\033[0m";  // Text Reset
	       
		System.out.println("Contents of First Commit: ");
        for(String fileHash:files1) {
        	System.out.println("File hash: "+fileHash);
        	System.out.println(BLUE+""+(String)read(this.objectsPath.resolve(fileHash))+""+RESET);
        }      


        System.out.println("Contents of Second Commit: ");
        for(String fileHash:files2) {
        	System.out.println("File hash: "+fileHash);
        	System.out.println(GREEN +""+(String)read(this.objectsPath.resolve(fileHash))+""+RESET);
        }
	}
		
	//The method generates the SHA hash of the content passed as parameter.
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
	
	private void initializeHeadAndStagingArea() {
		writeStagingArea(new ArrayList<String>());
		writeHeadData(null);
	}
	
	private String readHeadData() {
		return (String)read(this.headPath);
	}

	
	private void writeHeadData(String content) {
		write(this.headPath, content);
	}
	
	@SuppressWarnings("unchecked")
	private List<String> readStagingArea() {
		Object stagingAreaObject = read(stagingAreaPath);
		return (ArrayList<String>)(stagingAreaObject);
	}
	
	private void writeStagingArea(List<String> stagingAreaChanges) {
		write(stagingAreaPath, stagingAreaChanges);
	}
	
	private CommitData readCommitData(Path path) {				
		return (CommitData)read(path);
	}
	
	private void writeCommitData(Path path, CommitData stagingAreaChanges) {
		write(path, stagingAreaChanges);
	}
	
	private void write(Path path, Object content) {
		try(FileOutputStream fileOut = new FileOutputStream(path.toString())){
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(content);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Object read(Path path) {
		Object readObject = null;
		try(FileInputStream fileIn = new FileInputStream(path.toString())){
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			readObject = objIn.readObject();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return readObject;
	}
}

final class CommitData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private List<String> changedFiles;
    private String parentCommit;
    private LocalDateTime createdTime;

    public CommitData(String message, List<String> changedFiles, String parentCommit, LocalDateTime createdTime) {
        this.message = message;
        this.changedFiles = changedFiles;
        this.parentCommit = parentCommit;
        this.createdTime = createdTime;
    }

    public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getChangedFiles() {
		return changedFiles;
	}

	public void setChangedFiles(List<String> changedFiles) {
		this.changedFiles = changedFiles;
	}

	public String getParentCommit() {
		return parentCommit;
	}

	public void setParentCommit(String parentCommit) {
		this.parentCommit = parentCommit;
	}

	@Override
    public String toString() {
        return "CommitData{" +
                "message='" + message + '\'' +
                ", changedFiles=" + changedFiles +
                ", parentCommit='" + parentCommit + '\'' +
                '}';
    }

}