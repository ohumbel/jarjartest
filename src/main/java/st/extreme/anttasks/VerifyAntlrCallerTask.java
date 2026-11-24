package st.extreme.anttasks;

public class VerifyAntlrCallerTask extends VerifyCallerTask {

    public VerifyAntlrCallerTask() {
        setClassName("st.extreme.calling.Antlr");
        setExpectedPackage("st.extreme.antlr.runtime");
        setForbiddenPackage("org.antlr.runtime");
    }
}
