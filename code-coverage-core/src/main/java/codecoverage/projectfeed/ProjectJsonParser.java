package codecoverage.projectfeed;

import codecoverage.domain.ProjectInformation;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public final class ProjectJsonParser {

    private final ObjectMapper objectMapper;

    public ProjectJsonParser(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProjectInformation parseJsonFile(final File jsonFile) throws IOException {
        return objectMapper.readValue(jsonFile, ProjectInformation.class);
    }
}
