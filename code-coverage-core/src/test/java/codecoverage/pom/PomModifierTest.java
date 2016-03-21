package codecoverage.pom;

import codecoverage.TestCase;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static codecoverage.pom.PomModifier.modifyPomInBuildElementToAddPlugin;
import static codecoverage.pom.PomModifier.modifyPomInReportingElementToAddPlugin;
import static java.lang.System.getProperty;
import static java.nio.file.FileSystems.getDefault;
import static java.util.Collections.singletonList;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasXPath;

public class PomModifierTest extends TestCase {

    private static final Path ELFF_TEST_DIRECTORY_PATH = getDefault().getPath(getProperty("user.home") + "/TestElff/");
    private static final String LOCATION_OF_TEST_POM_FILE = ELFF_TEST_DIRECTORY_PATH + "/TestPom.xml";

    @Test
    public void givenReportingElementNotThereWhenModifyingPomWithReportingPluginThenInsert() throws Exception {
        primePomFile(new Model());
        final File pomToModify = new File(LOCATION_OF_TEST_POM_FILE);
        final Plugin pluginToInject = new Plugin();
        pluginToInject.setGroupId("org.codehaus.mojo");
        pluginToInject.setArtifactId("cobertura-maven-plugin");
        pluginToInject.setVersion("2.7");
        modifyPomInReportingElementToAddPlugin(pomToModify, pluginToInject);
        final Document document = newInstance().newDocumentBuilder().parse(pomToModify);
        assertThat(document, hasXPath("/project/reporting/plugins/plugin/artifactId", equalTo("cobertura-maven-plugin")));
    }

    @Test
    public void givenBuildElementNotThereWhenModifyPomInBuildElementIsCalledThenElementIsThere() throws Exception {
        primePomFile(new Model());

        final File pomToModify = new File(LOCATION_OF_TEST_POM_FILE);
        final Plugin pluginToInject = new Plugin();
        pluginToInject.setGroupId("org.codehaus.mojo");
        pluginToInject.setArtifactId("cobertura-maven-plugin");
        pluginToInject.setVersion("2.7");
        final PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.addGoal("clean");
        pluginExecution.addGoal("install");
        pluginToInject.setExecutions(singletonList(pluginExecution));

        modifyPomInBuildElementToAddPlugin(pomToModify, pluginToInject);
        final Document document = newInstance().newDocumentBuilder().parse(pomToModify);
        assertThat(document, hasXPath("/project/build/plugins/plugin/artifactId", equalTo("cobertura-maven-plugin")));
        assertThat(document, hasXPath("/project/build/plugins/plugin/executions/execution/goals/goal[2]", equalTo("install")));
        assertThat(document, hasXPath("/project/build/plugins/plugin/executions/execution/goals/goal[1]", equalTo("clean")));
    }

    @Test
    public void givenBuildPluginIsThereAndTheCoberturaPluginIsPresentWhenModifyingPomWithBuildPluginThenOnlyOnePluginIsThere() throws Exception {
        final Model modelPrime = new Model();
        modelPrime.setBuild(new Build());
        final Plugin primePlugin = new Plugin();
        primePlugin.setGroupId("org.codehaus.mojo");
        primePlugin.setArtifactId("cobertura-maven-plugin");
        primePlugin.setVersion("2.7");
        modelPrime.getBuild().addPlugin(primePlugin);
        primePomFile(modelPrime);

        final File pomToModify = new File(LOCATION_OF_TEST_POM_FILE);
        final Plugin pluginToInject = new Plugin();
        pluginToInject.setGroupId("org.codehaus.mojo");
        pluginToInject.setArtifactId("cobertura-maven-plugin");
        pluginToInject.setVersion("2.7");
        final PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.addGoal("clean");
        pluginExecution.addGoal("install");
        pluginToInject.setExecutions(singletonList(pluginExecution));

        modifyPomInBuildElementToAddPlugin(pomToModify, pluginToInject);
        final Document document = newInstance().newDocumentBuilder().parse(pomToModify);
        assertThat(document, hasXPath("count(/project/build/plugins/plugin/executions/execution/goals/goal[2])", equalTo("1")));
        assertThat(document, hasXPath("count(/project/build/plugins/plugin/executions/execution/goals/goal[1])", equalTo("1")));
        assertThat(document, hasXPath("count(//project/build/plugins/plugin[artifactId='cobertura-maven-plugin'])", equalTo("1")));
    }

    @Test
    public void givenBuildPluginIsThereButNotTheCoberturaPluginWhenModifyingPomWithBuildPluginThenPluginIsThere() throws Exception {
        final Model modelPrime = new Model();
        modelPrime.setBuild(new Build());
        primePomFile(modelPrime);

        final File pomToModify = new File(LOCATION_OF_TEST_POM_FILE);
        final Plugin pluginToInject = new Plugin();
        pluginToInject.setGroupId("org.codehaus.mojo");
        pluginToInject.setArtifactId("cobertura-maven-plugin");
        pluginToInject.setVersion("2.7");
        final PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.addGoal("clean");
        pluginExecution.addGoal("install");
        pluginToInject.setExecutions(singletonList(pluginExecution));

        modifyPomInBuildElementToAddPlugin(pomToModify, pluginToInject);
        final Document document = newInstance().newDocumentBuilder().parse(pomToModify);
        assertThat(document, hasXPath("/project/build/plugins/plugin/artifactId", equalTo("cobertura-maven-plugin")));
        assertThat(document, hasXPath("/project/build/plugins/plugin/executions/execution/goals/goal[2]", equalTo("install")));
        assertThat(document, hasXPath("/project/build/plugins/plugin/executions/execution/goals/goal[1]", equalTo("clean")));
    }

    private void primePomFile(final Model model) throws Exception {
        final MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
        try (FileWriter out = new FileWriter(new File(LOCATION_OF_TEST_POM_FILE))) {
            mavenXpp3Writer.write(out, model);
        }
    }
}
