package codecoverage.repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.eclipse.jgit.api.Git.cloneRepository;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GitRepositoryClientTest extends RepositoryClientTestCase {

    private static final String binaryPassword = "0111000001100001011100110111001101110111011011110111001001100100";
    private static final String binaryUserName = "0111010101110011011001010111001001101110011000010110110101100101";
    private String password;
    private String userName;
    private GitClient gitClientUnderTest;

    @Before
    public void setUp() throws Exception {
        userName = generateAsciiString(binaryUserName);
        password = generateAsciiString(binaryPassword);
        gitClientUnderTest = new GitClient(userName, password);
    }

    @Test
    public void whenCheckingOutProjectAtBranchMasterThenDirectoryIsCreated() throws Exception {
        final File actualGitLocalDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/jgit-cookbook");
        gitClientUnderTest.checkOutAtCommit("https://github.com/centic9/jgit-cookbook.git", actualGitLocalDirectory, "master");
        assertThat(actualGitLocalDirectory.isDirectory(), is(equalTo(true)));
        final Git actualLocalGitRepository = new Git(new FileRepository(actualGitLocalDirectory + "/.git"));
        assertThat(actualLocalGitRepository.getRepository().getBranch(), equalTo("master"));
    }

    @Test
    public void whenCheckingOutProjectAtCommitRevisionThenElementValueExistsAtRevisionAndBranchIsAtCommitNumber() throws Exception {
        final File actualGitLocalDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/jgit-cookbook");
        gitClientUnderTest.checkOutAtCommit("https://github.com/centic9/jgit-cookbook.git", actualGitLocalDirectory, "7ef0ddbb40101728c94aa3d05911caa8af150865");
        final Git actualLocalGitRepository = new Git(new FileRepository(actualGitLocalDirectory + "/.git"));
        assertThat(actualLocalGitRepository.getRepository().getBranch(), equalTo("7ef0ddbb40101728c94aa3d05911caa8af150865"));
    }

    @Test
    public void whenCheckingOutProjectAtBranchThenElementValueExistsAtBranch() throws Exception {
        final File actualGitLocalDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/jgit-cookbook");
        gitClientUnderTest.checkOutAtCommit("https://github.com/centic9/jgit-cookbook.git", actualGitLocalDirectory, "testbranch");
        final Git actualLocalGitRepository = new Git(new FileRepository(actualGitLocalDirectory + "/.git"));
        assertThat(actualLocalGitRepository.getRepository().getBranch(), equalTo("testbranch"));
    }

    @Test(expected = JGitInternalException.class)
    public void givenDirectoryAlreadyExistsWhenCheckingOutTheSameProjectThenThrowException() throws Exception {
        final File actualGitLocalDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/jgit-cookbook");
        gitClientUnderTest.checkOutAtCommit("https://github.com/centic9/jgit-cookbook.git", actualGitLocalDirectory, "7ef0ddbb40101728c94aa3d05911caa8af150865");
        gitClientUnderTest.checkOutAtCommit("https://github.com/centic9/jgit-cookbook.git", actualGitLocalDirectory, "master");
    }

    @Test
    public void givenExistingGitProjectCheckedOutAtBranchWhenUpdatingProjectToLatestThenProjectBranchIsLatest() throws Exception {
        final File gitLocalDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/hiro-build-monitor");
        givenExistingProject(gitLocalDirectory, "7ef0ddbb40101728c94aa3d05911caa8af150865", "https://github.com/centic9/jgit-cookbook.git");
        gitClientUnderTest.updateProjectToCommit(gitLocalDirectory, "master");
        final Git actualLocalGitRepository = new Git(new FileRepository(gitLocalDirectory + "/.git"));
        assertThat(actualLocalGitRepository.getRepository().getBranch(), equalTo("master"));
    }

    @Test
    public void givenExistingGitProjectCheckedOutAtBranchWhenUpdatingProjectToLatestThenProjectBranchIsLatest_githubExample() throws Exception {
        final File gitLocalDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/jgit-cookbook");
        givenExistingProject(gitLocalDirectory, "5b21502eae4973d3bc990b86315c5edae6835502", "https://github.com/centic9/jgit-cookbook.git");
        gitClientUnderTest.updateProjectToCommit(gitLocalDirectory, "testbranch");
        final Git actualLocalGitRepository = new Git(new FileRepository(gitLocalDirectory + "/.git"));
        assertThat(actualLocalGitRepository.getRepository().getBranch(), is(equalTo("testbranch")));

        final File fileContentOnlyInBranch = new File(gitLocalDirectory.toPath() + "/pom.xml");
        final Document actualPomFile = newInstance().newDocumentBuilder().parse(fileContentOnlyInBranch);
        assertThat(actualPomFile, hasXPath("/project/dependencies/dependency/version", equalTo("2.3.1.201302201838-r")));
    }

    @Override
    protected final void givenExistingProject(final File localPath, final String revision, final String url) throws GitAPIException {
        try (final Git git = cloneRepository()
                .setURI(url)
                .setDirectory(localPath)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password))
                .setBranch(revision)
                .setCloneAllBranches(true)
                .call()) {
            git.checkout().setName(revision).call();
        }
    }
}
