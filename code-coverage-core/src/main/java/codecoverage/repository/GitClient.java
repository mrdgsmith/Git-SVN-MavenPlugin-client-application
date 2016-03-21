package codecoverage.repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK;
import static org.eclipse.jgit.api.Git.cloneRepository;
import static org.eclipse.jgit.api.Git.open;

public final class GitClient implements RepositoryClient {

    private final UsernamePasswordCredentialsProvider credentialsProvider;

    public GitClient(final String username, final String password) {
        credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        trustSslManager();
    }

    @Override
    public final void checkOutAtCommit(final String url, final File localPath, final String revision) throws GitAPIException {
        try (final Git git = cloneRepository()
                .setURI(url)
                .setDirectory(localPath)
                .setCredentialsProvider(credentialsProvider)
                .setBranch(revision)
                .setCloneAllBranches(true)
                .setProgressMonitor(new TextProgressMonitor())
                .call()) {
            checkout(git, revision);
        }
    }

    @Override
    public final void updateProjectToCommit(final File localDirectory, final String revision) throws Exception {
        try (final Git git = open(localDirectory)) {
            git.branchCreate().setName(revision)
                    .setStartPoint("origin/" + revision)
                    .setUpstreamMode(TRACK)
                    .setForce(true)
                    .call();
            checkout(git, revision);
            if (!git.pull().setCredentialsProvider(credentialsProvider).call().isSuccessful()) {
                throw new IllegalStateException("Failed to successfully pull updates");
            }
        }
    }

    private void checkout(final Git git, final String revision) throws GitAPIException {
        git.checkout().setName(revision).call();
    }
}
