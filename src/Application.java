
public class Application {

	public static void main(String[] args) {	
		Versioneer versioneer= new Versioneer();
		versioneer.init();	// initializes the versioneer repository locally
		versioneer.add("test.txt");	// adds the contents of the file to the staging area for tracking. Note that the file has to be present in the project's working directory (where .versioneer folder is present)
		versioneer.commit("second commit"); // commits the changes
		versioneer.log(); // displays a list of all of the logs
		
		versioneer.showDiff();

	}
}
