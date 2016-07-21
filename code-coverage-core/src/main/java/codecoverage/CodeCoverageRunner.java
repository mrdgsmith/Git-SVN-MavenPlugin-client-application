package codecoverage;

import codecoverage.domain.Project;
import codecoverage.domain.ProjectInformation;
import codecoverage.projectfeed.ProjectJsonParser;
import codecoverage.repository.GitClient;
import codecoverage.repository.RepositoryClient;
import codecoverage.repository.RepositoryManager;
import codecoverage.repository.SvnClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatusClient;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static codecoverage.credential.CredentialManager.IsValidateCredential;
import static codecoverage.environment.Environment.createEnvironment;
import static codecoverage.pom.PomModifier.modifyPomInBuildElementToAddPlugin;
import static codecoverage.pom.PomModifier.modifyPomInReportingElementToAddPlugin;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.lang.System.*;
import static java.nio.file.FileSystems.getDefault;
import static java.util.Arrays.asList;
import static java.util.logging.Level.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;
import static org.tmatesoft.svn.core.wc.SVNClientManager.newInstance;

public class CodeCoverageRunner {

    public static final Logger LOGGER = Logger.getLogger(CodeCoverageRunner.class.getName());
    private static final Path ELFF_DIRECTORY_PATH = getDefault().getPath(getProperty("user.home") + "/ELFF/");
    private static final Path ELFF_DIRECTORY_PATH_SNAPSHOT = getDefault().getPath(getProperty("user.home") + "/ELFF/snapshot/");
    private static final Scanner SCANNER = new Scanner(in);
    private static FileHandler fileHandler;
    private final Function<String, File> stringToFile = directory -> {
        final File fileDirectory = new File(ELFF_DIRECTORY_PATH_SNAPSHOT + "/" + directory);
        if (!fileDirectory.isDirectory()) {
            throw new IllegalArgumentException(fileDirectory.getAbsolutePath() + " is not an actual directory");
        }
        return fileDirectory;
    };

    private CodeCoverageRunner() throws Exception {
    }

    public static void main(final String... args) throws Exception {
        createLogger();
        final CodeCoverageRunner codeCoverageRunner = new CodeCoverageRunner();
        createEnvironment("ELFF");
        createEnvironment("/ELFF/snapshot");
        codeCoverageRunner.optionsToChoose();
    }

    private static void createLogger() throws IOException {
        LOGGER.setLevel(ALL);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
        final Date todayDate = new Date();
        final String logDate = simpleDateFormat.format(todayDate);
        fileHandler = new FileHandler("Elff-" + logDate + ".log", true);
        fileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return valueOf(new Date()) + ' ' + record.getLevel() + ' ' + formatMessage(record) + '\n';
            }
        });
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(fileHandler);
        LOGGER.info("Application started and logging is enabled");
    }

    private void optionsToChoose() {
        System.out.println("Press 1 to checkout a list of projects from a json file");
        System.out.println("Press 2 to add cobertura plugin to projects");
        System.out.println("Press 3 to update existing projects to latest csv revisions");
        System.out.println("Press 10 exit system");
        menu();
    }

    private int getNumber(final String inputFromConsole) {
        if (isNumber(inputFromConsole)) {
            return parseInt(inputFromConsole);
        }
        throw new IllegalArgumentException("Invalid choice received was " + inputFromConsole);
    }

    private void menu() {
        final String input = SCANNER.next();
        try {
            final int optionNumber = getNumber(input);
            options(optionNumber);
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid Choice received was " + input + ": Needs to be a number");
            LOGGER.log(SEVERE, getStackTrace(ex), ex);
            optionsToChoose();
        }
    }

    private void options(final int optionNumber) {
        switch (optionNumber) {
            case 1: {
                checkoutProjects();
                break;
            }

            case 2: {
                addCoberturaPlugin();
                break;
            }

            case 3: {
                updateExistingProjectsToLatest();
                break;
            }

            case 10: {
                LOGGER.info("Application stopped");
                fileHandler.close();
                exit(0);
                break;
            }

            default: {
                System.out.println("Not a valid option " + optionNumber);
                LOGGER.info("Not a valid option " + optionNumber);
                System.out.println("Please choose a valid option");
                optionsToChoose();
            }
        }
    }

    private void updateExistingProjectsToLatest() {
        System.out.println("Updating projects in " + ELFF_DIRECTORY_PATH_SNAPSHOT);
        final List<File> projectDirectories = Stream.of(ELFF_DIRECTORY_PATH_SNAPSHOT.toFile().list())
                .map(stringToFile::apply).collect(toList());

        updateGitProjects(projectDirectories.stream()
                .filter(project -> new File(project + "/.git").isDirectory())
                .collect(toList())
        );

        updateSvnProjects(projectDirectories.stream()
                .filter(this::isWorkingCopy)
                .collect(toList()));
        optionsToChoose();
    }

    private void updateSvnProjects(final List<File> svnDirectoryProjects) {
        System.out.println("Updating svn projects in " + ELFF_DIRECTORY_PATH_SNAPSHOT);
        final RepositoryClient svnClient = getRepository(SvnClient.class);
        svnDirectoryProjects.stream()
                .forEach(project -> {
                    try {
                        System.out.println("Updating svn project " + project.getName());
                        LOGGER.info("Updating svn project " + project.getName());
                        RepositoryManager.updateProjects(svnClient, project, "HEAD");
                        System.out.println("Successfully updated svn project " + project.getName() + " to HEAD");
                        LOGGER.info("Successfully updated svn project " + project.getName() + " to HEAD");
                    } catch (Exception e) {
                        System.out.println("failed to update project " + project.getName() + " due to " + e.getMessage());
                        LOGGER.log(SEVERE, getStackTrace(e), e);
                    }
                });
    }

    private boolean isWorkingCopy(final File projectDirectory) {
        final SVNClientManager svnClientManager = newInstance();
        final SVNStatusClient statusClient = svnClientManager.getStatusClient();
        try {
            return statusClient.doStatus(projectDirectory, false).isVersioned();
        } catch (SVNException e) {
            return false;
        }
    }

    private void updateGitProjects(final List<File> gitDirectoryProjects) {
        System.out.println("Updating git projects in " + ELFF_DIRECTORY_PATH_SNAPSHOT);
        final RepositoryClient gitClient = getRepository(GitClient.class);
        gitDirectoryProjects.stream()
                .forEach(project -> {
                    try {
                        System.out.println("Updating git project " + project.getName());
                        LOGGER.info("Updating git project " + project.getName());
                        RepositoryManager.updateProjects(gitClient, project, "master");
                        System.out.println("Successfully updated git project " + project.getName() + " to master");
                        LOGGER.info("Successfully updated git project " + project.getName() + " to master");
                    } catch (Exception e) {
                        System.out.println("failed to update project " + project.getName() + " due to " + e.getMessage());
                        LOGGER.log(SEVERE, getStackTrace(e), e);
                    }
                });
    }

    private RepositoryClient getRepository(final Class clazz) {
        final String clientName = clazz.getSimpleName();
        System.out.println("Getting credentials for " + clientName);
        final ImmutablePair<String, String> userNameAndPasswordImmutablePair = collectCredentials();
        return RepositoryManager.getRepository(clazz, userNameAndPasswordImmutablePair.getLeft(), userNameAndPasswordImmutablePair.getRight());
    }

    private void addCoberturaPlugin() {
        System.out.println("Adding cobertura to every pom project in directory " + ELFF_DIRECTORY_PATH_SNAPSHOT);
        asList(ELFF_DIRECTORY_PATH_SNAPSHOT.toFile().list()).stream()
                .filter(file -> new File(ELFF_DIRECTORY_PATH_SNAPSHOT + "/" + file).isDirectory())
                .forEach(file -> {
                    try {
                        System.out.println("Adding cobertura to project " + file);
                        modifyPomInReportingElementToAddPlugin(new File(ELFF_DIRECTORY_PATH_SNAPSHOT + "/" + file + "/pom.xml"), createReportingPlugin());
                        modifyPomInBuildElementToAddPlugin(new File(ELFF_DIRECTORY_PATH_SNAPSHOT + "/" + file + "/pom.xml"), createBuildPlugin());
                        System.out.println("Successfully added cobertura to project " + file);
                        LOGGER.info("Successfully added cobertura to project " + file);
                    } catch (Exception e) {
                        System.out.println("failed to add cobertura to project " + file + " due to " + e.getMessage());
                        LOGGER.log(SEVERE, getStackTrace(e), e);
                    }
                });
        optionsToChoose();
    }

    private Plugin createReportingPlugin() {
        final Plugin reportingPlugin = new Plugin();
        reportingPlugin.setGroupId("org.codehaus.mojo");
        reportingPlugin.setArtifactId("cobertura-maven-plugin");
        reportingPlugin.setVersion("2.7");
        return reportingPlugin;
    }

    private Plugin createBuildPlugin() {
        final Plugin buildPlugin = new Plugin();
        buildPlugin.setGroupId("org.codehaus.mojo");
        buildPlugin.setArtifactId("cobertura-maven-plugin");
        buildPlugin.setVersion("2.7");
        final Xpp3Dom configuration = new Xpp3Dom("configuration");
        final Xpp3Dom reports = new Xpp3Dom("formats");
        final Xpp3Dom report = new Xpp3Dom("format");
        report.setValue("xml");
        reports.addChild(report);
        configuration.addChild(reports);
        buildPlugin.setConfiguration(configuration);
        return buildPlugin;
    }

    private void checkCredentials(final String userName, final String password) throws IllegalArgumentException {
        if (!IsValidateCredential(userName) || !IsValidateCredential(password)) {
            throw new IllegalArgumentException("Either username or password is empty");
        }
    }

    private ImmutablePair<String, String> collectCredentials() {
        System.out.println("Enter vcs username client");
        final String userName = SCANNER.next();
        final Console console = console();
        final String password = valueOf(console.readPassword("Enter vcs password for username " + userName));
        try {
            checkCredentials(userName, password);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            LOGGER.log(WARNING, getStackTrace(exception), exception);
            System.out.println("Please try again");
            collectCredentials();
        }
        return new ImmutablePair<>(userName, password);
    }

    private void checkoutGitRepository(final ProjectInformation projectInformation) {
        System.out.println("Getting credentials for git client");
        final ImmutablePair<String, String> gitUserNameAndPasswordImmutablePair = collectCredentials();

        final RepositoryClient gitClient = new GitClient(gitUserNameAndPasswordImmutablePair.getLeft(), gitUserNameAndPasswordImmutablePair.getRight());
        final Predicate<Project> gitPredicate = project -> project.getRepositoryType().equals("git");
        final ImmutablePair<RepositoryClient, Predicate<Project>> gitPair = new ImmutablePair<>(gitClient, gitPredicate);
        RepositoryManager.checkoutProjects(projectInformation, gitPair, ELFF_DIRECTORY_PATH_SNAPSHOT);
    }

    private void checkoutSvnRepository(final ProjectInformation projectInformation) {
        System.out.println("Getting credentials for svn client");
        final ImmutablePair<String, String> svnUserNameAndPasswordImmutablePair = collectCredentials();

        final RepositoryClient subversionClient = new SvnClient(svnUserNameAndPasswordImmutablePair.getLeft(), svnUserNameAndPasswordImmutablePair.getRight());
        final Predicate<Project> svnPredicate = project -> project.getRepositoryType().equals("svn");
        final ImmutablePair<RepositoryClient, Predicate<Project>> subversionPair = new ImmutablePair<>(subversionClient, svnPredicate);
        RepositoryManager.checkoutProjects(projectInformation, subversionPair, ELFF_DIRECTORY_PATH_SNAPSHOT);
    }

    private void checkoutProjects() {
        try {
            final ProjectInformation projectInformation = createProjectInformation(getJsonFile());
            checkoutGitRepository(projectInformation);
            checkoutSvnRepository(projectInformation);
        } catch (final IOException ex) {
            LOGGER.log(SEVERE, getStackTrace(ex), ex);
        }
        optionsToChoose();
    }

    private ProjectInformation createProjectInformation(final File jsonFile) throws IOException {
        final ProjectJsonParser projectJsonParser = new ProjectJsonParser(new ObjectMapper());
        try {
            final ProjectInformation projectInformation = projectJsonParser.parseJsonFile(jsonFile);
            projectInformation.getProjects().stream()
                    .forEach(project -> {
                                System.out.println("project extracted from json to be checkout is " + project.getName() + " at revision " + project.getVersion() + " from repository " + project.getUrl());
                                LOGGER.info("project extracted from json to be checkout is " + project.getName() + " at revision " + project.getVersion() + " from repository " + project.getUrl());
                            }
                    );

            LOGGER.info("json received is " + projectInformation.toString());
            return projectInformation;
        } catch (IOException e) {
            System.out.println("json not in valid format");
            throw e;
        }
    }

    private File getJsonFile() throws FileNotFoundException {
        final String DID_NOT_CHOOSE_A_FILE = "Did not choose a file";
        final JPanel panel = new JPanel();
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(ELFF_DIRECTORY_PATH.toFile());
        final FileFilter fileFilter = new FileNameExtensionFilter("json file extension", "json");
        fileChooser.addChoosableFileFilter(fileFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int result = fileChooser.showOpenDialog(panel);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        System.out.println(DID_NOT_CHOOSE_A_FILE);
        throw new FileNotFoundException(DID_NOT_CHOOSE_A_FILE);
    }

}
