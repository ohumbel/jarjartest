package st.extreme.anttasks;

public class VerifyCommonsCompressCallerTask extends VerifyCallerTask {

    public VerifyCommonsCompressCallerTask() {
        setClassName("st.extreme.calling.CommonsCompress");
        setExpectedPackage("st.extreme.apache.commons.compress");
        setForbiddenPackage("org.apache.commons.compress");
    }
}
