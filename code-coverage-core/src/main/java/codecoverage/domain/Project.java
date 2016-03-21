package codecoverage.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Project {

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String url;

    @JsonProperty
    private final String version;

    @JsonProperty("repository type")
    private final String repositoryType;

    @JsonCreator
    public Project(@JsonProperty("name") final String name, @JsonProperty("url") final String url, @JsonProperty("version") final String version, @JsonProperty("repository type") final String repositoryType) {
        this.name = name;
        this.url = url;
        this.version = version;
        this.repositoryType = repositoryType;
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

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public String getRepositoryType() {
        return repositoryType;
    }
}
