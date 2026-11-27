package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifyGoogleCallerTaskTest {

    @Test
    void testRepackagedResultJarStaticallyReferencesRepackagedGoogle() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        Set<String> referencedClasses = VerifyGoogleCallerTask.extractReferencedClasses(
                resultJar,
                "st/extreme/calling/Google.class"
        );

        assertThat(referencedClasses)
                .contains("st.extreme.google.common.base.CharMatcher")
                .doesNotContain("com.google.common.base.CharMatcher");

        VerifyGoogleCallerTask task = new VerifyGoogleCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.Google");
        task.setExpectedPackage("st.extreme.google");
        task.setForbiddenPackage("com.google");

        task.execute();
    }

    @Test
    void testUnrepackagedJarFailsVerificationDueToForbiddenPackage() {
        File stextremeJar = new File("deployment/stextreme.jar");
        assertThat(stextremeJar).exists();

        VerifyGoogleCallerTask task = new VerifyGoogleCallerTask();
        task.setProject(new Project());
        task.setJar(stextremeJar);
        task.setClassName("st.extreme.calling.Google");
        task.setExpectedPackage("st.extreme.google");
        task.setForbiddenPackage("com.google");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("forbidden package 'com.google'");
    }

    @Test
    void testMissingClassFailsVerification() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyGoogleCallerTask task = new VerifyGoogleCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.NonExistent");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("not found");
    }
}
