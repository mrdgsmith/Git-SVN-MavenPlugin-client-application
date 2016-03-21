package codecoverage.projectfeed;

import codecoverage.TestCase;
import codecoverage.domain.Project;
import codecoverage.domain.ProjectInformation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class ProjectJsonParserTest extends TestCase {

    private static final String LOCATION_OF_TEST_JSON_FILE = ELFF_TEST_DIRECTORY_PATH + "/TestJson.json";
    private ProjectJsonParser projectsParserUnderTest;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        projectsParserUnderTest = new ProjectJsonParser(objectMapper);
    }

    @Test
    public void whenValidJsonReceivedThenReturnDomainObject() throws Exception {
        final File jsonFile = createJsonFile();
        final ProjectInformation project = projectsParserUnderTest.parseJsonFile(jsonFile);
        assertThat(project.getProjects(), hasItems(new Project("test1", "https://git.changeme.com/team1/test1.git", "master", "git"),
                new Project("test2", "http://subversion.changeme.com/sns-test", "HEAD", "svn")));
    }

    private File createJsonFile() throws IOException {
        final Map<String, List<Map>> projects = new HashMap<>();
        final Map<String, String> project1 = new HashMap<>();
        final Map<String, String> project2 = new HashMap<>();

        project1.put("name", "test1");
        project1.put("url", "https://git.changeme.com/team1/test1.git");
        project1.put("version", "master");
        project1.put("repository type", "git");

        project2.put("name", "test2");
        project2.put("url", "http://subversion.changeme.com/sns-test");
        project2.put("version", "HEAD");
        project2.put("repository type", "svn");


        final List<Map> projectArray = new ArrayList<>();
        projectArray.add(project1);
        projectArray.add(project2);
        projects.put("projects", projectArray);
        File resultFile = new File(LOCATION_OF_TEST_JSON_FILE);
        objectMapper.writeValue(resultFile, projects);
        return resultFile;
    }
}