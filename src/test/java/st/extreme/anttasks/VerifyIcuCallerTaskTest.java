package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifyIcuCallerTaskTest {

    @Test
    void testRepackagedResultJarStaticallyReferencesRepackagedIcu() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        Set<String> referencedClasses = VerifyIcuCallerTask.extractReferencedClasses(
                resultJar,
                "st/extreme/calling/Icu.class"
        );

        assertThat(referencedClasses)
                .contains("st.extreme.icu.text.StringPrep")
                .doesNotContain("com.ibm.icu.text.StringPrep");

        VerifyIcuCallerTask task = new VerifyIcuCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.Icu");
        task.setExpectedPackage("st.extreme.icu");
        task.setForbiddenPackage("com.ibm.icu");

        task.execute();
    }

    @Test
    void testUnrepackagedJarFailsVerificationDueToForbiddenPackage() {
        File stextremeJar = new File("deployment/stextreme.jar");
        assertThat(stextremeJar).exists();

        VerifyIcuCallerTask task = new VerifyIcuCallerTask();
        task.setProject(new Project());
        task.setJar(stextremeJar);
        task.setClassName("st.extreme.calling.Icu");
        task.setExpectedPackage("st.extreme.icu");
        task.setForbiddenPackage("com.ibm.icu");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("forbidden package 'com.ibm.icu'");
    }

    @Test
    void testMissingClassFailsVerification() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyIcuCallerTask task = new VerifyIcuCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.NonExistent");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("not found");
    }
}
