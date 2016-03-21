package codecoverage.repository;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import java.io.File;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.tmatesoft.svn.core.SVNDepth.INFINITY;
import static org.tmatesoft.svn.core.SVNURL.parseURIEncoded;
import static org.tmatesoft.svn.core.auth.BasicAuthenticationManager.newInstance;
import static org.tmatesoft.svn.core.wc.SVNClientManager.newInstance;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;
import static org.tmatesoft.svn.core.wc.SVNRevision.create;

public class SvnClient implements RepositoryClient {

    private final SVNClientManager svnClientManager;
    private final SVNUpdateClient updateClient;

    public SvnClient(final String userName, final String password) {
        svnClientManager = newInstance();
        svnClientManager.setAuthenticationManager(newInstance(userName, password.toCharArray()));
        updateClient = svnClientManager.getUpdateClient();
        trustSslManager();
    }

    @Override
    public void checkOutAtCommit(final String url, final File localDirectory, final String revision) throws SVNException {
        final SVNURL svnUrl = parseURIEncoded(url);
        final SVNRevision svnRevision = getSvnRevision(revision);
        updateClient.doCheckout(svnUrl, localDirectory, svnRevision, svnRevision, INFINITY, true);
    }

    @Override
    public void updateProjectToCommit(final File localDirectory, final String revision) throws Exception {
        final SVNRevision svnRevision = getSvnRevision(revision);
        updateClient.doUpdate(localDirectory, svnRevision, INFINITY, true, true);
    }

    private SVNRevision getSvnRevision(String revision) {
        if (revision.equalsIgnoreCase("HEAD")) {
            return HEAD;
        } else if (isNumeric(revision)) {
            return create(parseLong(revision));
        }
        throw new IllegalArgumentException(revision + " is not a valid revision number for subversion");
    }

}
