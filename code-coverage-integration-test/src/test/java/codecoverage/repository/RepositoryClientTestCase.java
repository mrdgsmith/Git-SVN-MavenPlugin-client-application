package codecoverage.repository;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.nio.file.Path;

import static java.lang.System.getProperty;
import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.Files.createDirectory;

public abstract class RepositoryClientTestCase {

    public static final Path ELFF_TEST_DIRECTORY_PATH = getDefault().getPath(getProperty("user.home") + "/TestElff/");

    public static boolean removeDirectory(File directory) {
        if (directory == null)
            return false;
        if (!directory.exists())
            return true;
        if (!directory.isDirectory())
            return false;

        String[] list = directory.list();
        if (list != null) {
            for (String aList : list) {
                File entry = new File(directory, aList);
                if (entry.isDirectory()) {
                    if (!removeDirectory(entry))
                        return false;
                } else {
                    if (!entry.delete())
                        return false;
                }
            }
        }
        return directory.delete();
    }

    @Before
    public void setUpInfrastructure() throws Exception {
        cleanInfrastructure();
        createDirectory(ELFF_TEST_DIRECTORY_PATH);
    }

    @After
    public void tearDown() throws Exception {
        cleanInfrastructure();
    }

    private boolean cleanInfrastructure() {
        return removeDirectory(ELFF_TEST_DIRECTORY_PATH.toFile());
    }

    protected String generateAsciiString(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i += 8) {
            output.append((char) Integer.parseInt(input.substring(i, i + 8), 2));
        }
        return output.toString();
    }

    abstract protected void givenExistingProject(final File localPath, final String revision, final String url) throws Exception;
}
