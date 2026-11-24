package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifyAntlrCallerTaskTest {

    @Test
    void testRepackagedResultJarStaticallyReferencesRepackagedAntlr() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        Set<String> referencedClasses = VerifyAntlrCallerTask.extractReferencedClasses(
                resultJar,
                "st/extreme/calling/Antlr.class"
        );

        assertThat(referencedClasses)
                .contains("st.extreme.antlr.runtime.Token")
                .contains("st.extreme.antlr.runtime.CommonToken")
                .doesNotContain("org.antlr.runtime.Token")
                .doesNotContain("org.antlr.runtime.CommonToken");

        VerifyAntlrCallerTask task = new VerifyAntlrCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.Antlr");
        task.setExpectedPackage("st.extreme.antlr.runtime");
        task.setForbiddenPackage("org.antlr.runtime");

        task.execute();
    }

    @Test
    void testUnrepackagedJarFailsVerificationDueToForbiddenPackage() {
        File stextremeJar = new File("deployment/stextreme.jar");
        assertThat(stextremeJar).exists();

        VerifyAntlrCallerTask task = new VerifyAntlrCallerTask();
        task.setProject(new Project());
        task.setJar(stextremeJar);
        task.setClassName("st.extreme.calling.Antlr");
        task.setExpectedPackage("st.extreme.antlr.runtime");
        task.setForbiddenPackage("org.antlr.runtime");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("forbidden package 'org.antlr.runtime'");
    }

    @Test
    void testMissingClassFailsVerification() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyAntlrCallerTask task = new VerifyAntlrCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.NonExistent");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("not found");
    }
}
