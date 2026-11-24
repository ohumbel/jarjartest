package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifyCommonsCompressCallerTaskTest {

    @Test
    void testRepackagedResultJarStaticallyReferencesRepackagedCommonsCompress() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        Set<String> referencedClasses = VerifyCommonsCompressCallerTask.extractReferencedClasses(
                resultJar,
                "st/extreme/calling/CommonsCompress.class"
        );

        assertThat(referencedClasses)
                .contains("st.extreme.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream")
                .doesNotContain("org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream");

        VerifyCommonsCompressCallerTask task = new VerifyCommonsCompressCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.CommonsCompress");
        task.setExpectedPackage("st.extreme.apache.commons.compress");
        task.setForbiddenPackage("org.apache.commons.compress");

        task.execute();
    }

    @Test
    void testUnrepackagedJarFailsVerificationDueToForbiddenPackage() {
        File stextremeJar = new File("deployment/stextreme.jar");
        assertThat(stextremeJar).exists();

        VerifyCommonsCompressCallerTask task = new VerifyCommonsCompressCallerTask();
        task.setProject(new Project());
        task.setJar(stextremeJar);
        task.setClassName("st.extreme.calling.CommonsCompress");
        task.setExpectedPackage("st.extreme.apache.commons.compress");
        task.setForbiddenPackage("org.apache.commons.compress");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("forbidden package 'org.apache.commons.compress'");
    }

    @Test
    void testMissingClassFailsVerification() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyCommonsCompressCallerTask task = new VerifyCommonsCompressCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.NonExistent");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("not found");
    }
}
