package codecoverage.repository;


import codecoverage.CodeCoverageRunner;
import codecoverage.domain.Project;
import codecoverage.domain.ProjectInformation;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

public final class RepositoryManager {

    private static final Logger logger = getLogger(CodeCoverageRunner.class.getName());

    public static void checkoutProjects(final ProjectInformation projectInformation, final ImmutablePair<RepositoryClient, Predicate<Project>> repositoryPair, final Path rootSnapshotDirectory) {
        System.out.println("Starting to checkout projects");
        logger.info("Starting to checkout projects");
        projectInformation.getProjects().stream()
                .filter(repositoryPair.getRight())
                .forEach(project -> {
                    try {
                        System.out.println("Checking out project " + project.getName());
                        logger.info("Checking out project " + project.getName());
                        repositoryPair.getLeft().checkOutAtCommit(project.getUrl(), new File(rootSnapshotDirectory + "/" + project.getName()), project.getVersion());
                        System.out.println("Successfully checked out project " + project.toString());
                        logger.info("Successfully checked out project " + project.toString());
                    } catch (Exception e) {
                        System.out.println("failed to checkout " + project.toString() + " due to " + e.getMessage());
                        logger.log(SEVERE, getStackTrace(e), e);
                    }
                });
    }

    public static RepositoryClient getRepository(final Class repositoryClientClass, final String username, final String password) {
        try {
            return getRepositoryClient(repositoryClientClass, username, password);
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot find suitable repository client for " + repositoryClientClass.getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    private static RepositoryClient getRepositoryClient(final Class repositoryClientClass, final String vcsUserName, final String vcsPassword) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (RepositoryClient) repositoryClientClass.getDeclaredConstructor(String.class, String.class).newInstance(vcsUserName, vcsPassword);
    }

    public static void updateProjects(final RepositoryClient repositoryClient, final File directoryOfProjectOnDisk, final String revision) throws Exception {
        repositoryClient.updateProjectToCommit(directoryOfProjectOnDisk, revision);
    }
}
