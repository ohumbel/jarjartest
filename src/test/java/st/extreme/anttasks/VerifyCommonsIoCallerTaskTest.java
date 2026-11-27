package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifyCommonsIoCallerTaskTest {

    @Test
    void testRepackagedResultJarStaticallyReferencesRepackagedCommonsIo() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        Set<String> referencedClasses = VerifyCommonsIoCallerTask.extractReferencedClasses(
                resultJar,
                "st/extreme/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.class"
        );

        assertThat(referencedClasses)
                .contains("st.extreme.apache.commons.io.input.CloseShieldInputStream")
                .doesNotContain("org.apache.commons.io.input.CloseShieldInputStream");

        VerifyCommonsIoCallerTask task = new VerifyCommonsIoCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream");
        task.setExpectedPackage("st.extreme.apache.commons.io");
        task.setForbiddenPackage("org.apache.commons.io");

        task.execute();
    }

    @Test
    void testUnrepackagedJarFailsVerificationDueToForbiddenPackage() {
        File commonsCompressJar = new File("lib/commons-compress-1.28.0.jar");
        assertThat(commonsCompressJar).exists();

        VerifyCommonsIoCallerTask task = new VerifyCommonsIoCallerTask();
        task.setProject(new Project());
        task.setJar(commonsCompressJar);
        task.setClassName("org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream");
        task.setExpectedPackage("st.extreme.apache.commons.io");
        task.setForbiddenPackage("org.apache.commons.io");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("forbidden package 'org.apache.commons.io'");
    }

    @Test
    void testMissingClassFailsVerification() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyCommonsIoCallerTask task = new VerifyCommonsIoCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.NonExistent");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("not found");
    }
}
