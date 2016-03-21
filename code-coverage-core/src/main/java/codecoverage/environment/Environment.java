package codecoverage.environment;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;

public class Environment {

    public static void createEnvironment(String directory) throws Exception {
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.home") + "/" + directory);
        if (!path.toFile().exists()) {
            createDirectory(path);
        }
    }
}
