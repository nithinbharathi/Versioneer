
public class Application {

	public static void main(String[] args) {	
		Versioneer versioneer= new Versioneer();
		/*versioneer.init();
		versioneer.add("test.txt");
		versioneer.commit("first commit");
		versioneer.add("test.txt");
		versioneer.commit("second commit");*/
		versioneer.log();

	}
}
