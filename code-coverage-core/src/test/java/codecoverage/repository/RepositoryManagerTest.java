package codecoverage.repository;

import codecoverage.TestCase;
import codecoverage.domain.Project;
import codecoverage.domain.ProjectInformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static codecoverage.repository.RepositoryManager.checkoutProjects;
import static codecoverage.repository.RepositoryManager.getRepository;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;


public class RepositoryManagerTest extends TestCase {

    @Test
    public void givenThreeProjectsButOnly2ValidWhenCheckingOutThenCallCheckoutTwoTimes() throws Exception {
        final ProjectInformation projectInformation = mock(ProjectInformation.class);

        final List<Project> projects = new ArrayList<>();

        final Project project1 = mock(Project.class);
        when(project1.getName()).thenReturn("project1");
        when(project1.getVersion()).thenReturn("master1");
        when(project1.getUrl()).thenReturn("github.com1");
        when(project1.getRepositoryType()).thenReturn("yes");
        projects.add(project1);

        final Project project2 = mock(Project.class);
        when(project2.getName()).thenReturn("project2");
        when(project2.getVersion()).thenReturn("master2");
        when(project2.getUrl()).thenReturn("github.com2");
        when(project2.getRepositoryType()).thenReturn("no");
        projects.add(project2);

        final Project project3 = mock(Project.class);
        when(project3.getName()).thenReturn("project3");
        when(project3.getVersion()).thenReturn("master3");
        when(project3.getUrl()).thenReturn("github.com3");
        when(project3.getRepositoryType()).thenReturn("yes");
        projects.add(project3);

        when(projectInformation.getProjects()).thenReturn(projects);

        final RepositoryClient repositoryClient = mock(RepositoryClient.class);

        final ImmutablePair<RepositoryClient, Predicate<Project>> repositoryPair = new ImmutablePair<>(repositoryClient, (Predicate<Project>) project -> project.getRepositoryType().equals("yes"));
        final Path rootSnapshotDirectory = mock(Path.class);

        checkoutProjects(projectInformation, repositoryPair, rootSnapshotDirectory);

        final InOrder inOrderRepositoryClient = inOrder(repositoryClient);
        inOrderRepositoryClient.verify(repositoryClient, times(1)).checkOutAtCommit(eq("github.com1"), any(File.class), eq("master1"));
        inOrderRepositoryClient.verify(repositoryClient, never()).checkOutAtCommit(eq("github.com2"), any(File.class), eq("master2"));
        inOrderRepositoryClient.verify(repositoryClient, times(1)).checkOutAtCommit(eq("github.com3"), any(File.class), eq("master3"));
        verifyNoMoreInteractions(repositoryClient);
    }

    @Test
    public void updateProject() throws Exception {
        final RepositoryClient repositoryClient = mock(RepositoryClient.class);
        final String revision = "revision";
        final File directoryOfProjectOnDisk = mock(File.class);
        RepositoryManager.updateProjects(repositoryClient, directoryOfProjectOnDisk, revision);
        verify(repositoryClient, times(1)).updateProjectToCommit(any(File.class), eq(revision));
    }

    @Test
    public void whenPassingClassGitThenReturnGitInstance() throws Exception {
        final String username = "username";
        final String password = "password";
        final Class<? extends RepositoryClient> actualClass = getRepository(GitClient.class, username, password).getClass();
        assertThat(actualClass, equalTo(GitClient.class));
    }

    @Test
    public void whenPassingClassSvnThenReturnSvnInstance() throws Exception {
        final String username = "username";
        final String password = "password";
        final Class<? extends RepositoryClient> actualClass = getRepository(SvnClient.class, username, password).getClass();
        assertThat(actualClass, equalTo(SvnClient.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPassingClassUnknownThenThrowException() throws Exception {
        final String username = "username";
        final String password = "password";
        getRepository(Object.class, username, password).getClass();
    }
}