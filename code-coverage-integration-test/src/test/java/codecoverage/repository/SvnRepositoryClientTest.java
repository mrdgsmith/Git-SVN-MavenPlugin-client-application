package codecoverage.repository;

import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.tmatesoft.svn.core.SVNDepth.INFINITY;
import static org.tmatesoft.svn.core.SVNURL.parseURIEncoded;
import static org.tmatesoft.svn.core.wc.SVNClientManager.newInstance;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;
import static org.tmatesoft.svn.core.wc.SVNRevision.create;

public class SvnRepositoryClientTest extends RepositoryClientTestCase {

    private static final String binaryPassword = "0111000001100001011100110111001101110111011011110111001001100100";
    private static final String binaryUserName = "0111010101110011011001010111001001101110011000010110110101100101";
    private String password;
    private String userName;
    private SvnClient svnClientUnderTest;

    @Before
    public void setUp() throws Exception {
        userName = generateAsciiString(binaryUserName);
        password = generateAsciiString(binaryPassword);
        svnClientUnderTest = new SvnClient(userName, password);
    }

    @Test
    public void whenCheckingOutProjectThenDirectoryIsCreated() throws Exception {
        final File actualSubversionDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/test1");
        svnClientUnderTest.checkOutAtCommit("http://CHANGE-ME.com/test1", actualSubversionDirectory, "HEAD");
        assertThat(actualSubversionDirectory.isDirectory(), is(equalTo(true)));
    }

    @Test
    public void whenCheckingOutProjectTwiceThenOverwrite() throws Exception {
        final File actualSubversionDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/test2");
        svnClientUnderTest.checkOutAtCommit("http://CHANGE-ME.com/test2", actualSubversionDirectory, "HEAD");
        svnClientUnderTest.checkOutAtCommit("http://CHANGE-ME.com/test2", actualSubversionDirectory, "HEAD");
        assertThat(actualSubversionDirectory.isDirectory(), is(equalTo(true)));
    }

    @Test
    public void whenCheckingOutProjectAtCommitRevisionThenRepositoryIsCheckedOut() throws Exception {
        final File actualSubversionDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/test3");
        svnClientUnderTest.checkOutAtCommit("http://CHANGE-ME.com/test3", actualSubversionDirectory, "258720");
        final SVNClientManager svnClientManager = newInstance();
        final SVNStatusClient statusClient = svnClientManager.getStatusClient();
        final SVNStatus svnStatus = statusClient.doStatus(actualSubversionDirectory, false);
        assertThat(svnStatus.getRevision().getNumber(), is(equalTo(258720L)));
        assertThat(actualSubversionDirectory.isDirectory(), is(equalTo(true)));
    }

    @Test
    public void givenExistingSubversionProjectCheckedOutAtBranchWhenUpdatingProjectToLatestThenProjectBranchIsLatest() throws Exception {
        final File actualSubversionDirectory = new File(ELFF_TEST_DIRECTORY_PATH + "/Iteration-1/test4");
        givenExistingProject(actualSubversionDirectory, "258720", "http://CHANGE-ME.com/test4");
        svnClientUnderTest.updateProjectToCommit(actualSubversionDirectory, "HEAD");
        final SVNClientManager svnClientManager = newInstance();
        final SVNStatusClient statusClient = svnClientManager.getStatusClient();
        final SVNStatus svnStatus = statusClient.doStatus(actualSubversionDirectory, false);
        assertThat(svnStatus.getRevision().getNumber(), is(greaterThan(258720L)));
    }

    @Override
    protected final void givenExistingProject(final File localPath, final String revision, final String url) throws Exception {
        final SVNClientManager svnClientManager = newInstance();
        svnClientManager.setAuthenticationManager(BasicAuthenticationManager.newInstance(userName, password.toCharArray()));
        final SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        final SVNURL svnUrl = parseURIEncoded(url);
        SVNRevision svnRevision = null;
        if (revision.equals("HEAD")) {
            svnRevision = HEAD;
        } else if (isNumeric(revision)) {
            svnRevision = create(parseLong(revision));
        }
        updateClient.doCheckout(svnUrl, localPath, svnRevision, svnRevision, INFINITY, true);
    }
}
