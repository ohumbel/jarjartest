package st.extreme.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifyCallerTaskTest {

    @Test
    void testVerifyCallerTaskWithCommonsCompress() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyCallerTask task = new VerifyCallerTask("st.extreme.calling.CommonsCompress", "st.extreme.apache.commons.compress", "org.apache.commons.compress");
        task.setProject(new Project());
        task.setJar(resultJar);

        task.execute();
    }

    @Test
    void testVerifyCallerTaskWithAntlr() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyCallerTask task = new VerifyCallerTask("st.extreme.calling.Antlr", "st.extreme.antlr.runtime", "org.antlr.runtime");
        task.setProject(new Project());
        task.setJar(resultJar);

        task.execute();
    }

    @Test
    void testMissingExpectedPackageFails() {
        File resultJar = new File("deployment/result.jar");
        assertThat(resultJar).exists();

        VerifyCallerTask task = new VerifyCallerTask();
        task.setProject(new Project());
        task.setJar(resultJar);
        task.setClassName("st.extreme.calling.Antlr");
        task.setExpectedPackage("st.extreme.nonexistent.package");

        assertThatThrownBy(task::execute)
                .isInstanceOf(BuildException.class)
                .hasMessageContaining("does not statically reference expected package");
    }
}
