package codecoverage.credential;


import org.apache.commons.lang3.StringUtils;

public final class CredentialManager {

    public static boolean IsValidateCredential(final String credential) {
        return !StringUtils.isEmpty(credential);
    }
}
