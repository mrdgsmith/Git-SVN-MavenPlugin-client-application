package codecoverage.credential;

import org.junit.Test;

import static codecoverage.credential.CredentialManager.IsValidateCredential;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CredentialManagerTest {

    @Test
    public void whenCredentialIsSubmittedEmptyThenReturnFalse() throws Exception {
        final String credential = null;
        assertThat(IsValidateCredential(credential), is(equalTo(false)));
    }

    @Test
    public void whenCredentialIsNotEmptyThenReturnTrue() throws Exception {
        final String credential = "credential";
        assertThat(IsValidateCredential(credential), is(equalTo(true)));
    }
}