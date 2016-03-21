package codecoverage.pom;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public final class PomModifier {

    private PomModifier() {
    }

    public static void modifyPomInReportingElementToAddPlugin(final File pomToModify, final Plugin pluginToInject) throws Exception {
        final MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        final Model model = mavenXpp3Reader.read(new FileReader(pomToModify));
        pluginForReportingElement(model, pluginToInject);
        writeModifyPomToDisk(model, pomToModify);
    }

    public static void modifyPomInBuildElementToAddPlugin(final File pomToModify, final Plugin pluginToInject) throws Exception {
        final MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        final Model model = mavenXpp3Reader.read(new FileReader(pomToModify));
        pluginForBuildElement(model, pluginToInject);
        writeModifyPomToDisk(model, pomToModify);
    }

    private static void pluginForReportingElement(final Model model, final Plugin pluginToInject) throws IOException {
        addReportingElementToPom(model);
        addPluginToReportingElement(model, pluginToInject);
    }

    private static void pluginForBuildElement(final Model model, final Plugin pluginToInject) {
        addBuildElementToPom(model);
        addPluginToBuildElement(model, pluginToInject);
    }

    private static void addBuildElementToPom(final Model model) {
        if (model.getBuild() == null) {
            model.setBuild(new Build());
        }
    }

    private static void addPluginToBuildElement(final Model model, final Plugin pluginToInject) {
        final List<Predicate<Plugin>> predicateList = Arrays.asList(
                thePlugin -> thePlugin.getGroupId().equals(pluginToInject.getGroupId())
                , thePlugin -> thePlugin.getArtifactId().equals(pluginToInject.getArtifactId())
        );

        final List<Plugin> buildPlugins = model.getBuild().getPlugins().stream()
                .filter(predicateList.stream().reduce(Predicate::and).orElse(t -> false))
                .collect(toList());

        if (!buildPlugins.isEmpty()) {
            model.getBuild().removePlugin(pluginToInject);
        }
        model.getBuild().addPlugin(pluginToInject);
    }

    private static void writeModifyPomToDisk(final Model model, final File pomToModify) throws IOException {
        final MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
        try (FileWriter out = new FileWriter(pomToModify)) {
            mavenXpp3Writer.write(out, model);
        }
    }

    private static void addReportingElementToPom(final Model model) {
        if (model.getReporting() == null) {
            model.setReporting(new Reporting());
        }
    }

    private static void addPluginToReportingElement(final Model model, final Plugin pluginToInject) {
        if (model.getReporting().getReportPluginsAsMap().get(pluginToInject.getGroupId() + ":" + pluginToInject.getArtifactId()) == null) {
            model.getReporting().addPlugin(makePlugin(pluginToInject));
        }
    }

    private static ReportPlugin makePlugin(final Plugin pluginToInject) {
        final ReportPlugin reportPlugin = new ReportPlugin();
        reportPlugin.setGroupId(pluginToInject.getGroupId());
        reportPlugin.setArtifactId(pluginToInject.getArtifactId());
        reportPlugin.setVersion(pluginToInject.getVersion());
        return reportPlugin;
    }
}
