package codecoverage.environment;

import codecoverage.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class EnvironmentTest extends TestCase {

    @Test
    public void whenCallThenDirectoryIsCreated() throws Exception {
        Environment.createEnvironment("TestElff");
        File actualPath = new File(System.getProperty("user.home") + "/TestElff");
        Assert.assertThat(actualPath.isDirectory(), is(equalTo(true)));
    }
}