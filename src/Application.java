
public class Application {

	public static void main(String[] args) {	
		Versioneer versioneer= new Versioneer();
		/*versioneer.init();	// initializes the versioneer repository locally
		versioneer.add("test.txt");	// adds the contents of the file to the staging area for tracking. Note that the file has to be present in the project's working directory (where .versioneer folder is present)
		versioneer.commit("second commit"); // commits the changes
		versioneer.log(); // displays a list of all of the logs*/
		
		versioneer.showDiff("5001e39718201851c8693353ce66daa7ec12a68e91749390f69e0680e6cb4cc0","7d89aa2d5507f16e77c7f26d513b2433ddfeb287cdaef11f19d23cd5ef94180e");
	}
}
