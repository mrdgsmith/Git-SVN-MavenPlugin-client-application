package codecoverage.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class ProjectInformation {

    @JsonProperty
    private final ArrayList<Project> projects;

    @JsonCreator
    public ProjectInformation(@JsonProperty("projects") ArrayList<Project> projectsList) {
        this.projects = projectsList;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public final int hashCode() {
        return reflectionHashCode(this);
    }

    public final boolean equals(Object obj) {
        return reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
