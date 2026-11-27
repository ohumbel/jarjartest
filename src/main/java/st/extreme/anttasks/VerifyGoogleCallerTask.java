package st.extreme.anttasks;

public class VerifyGoogleCallerTask extends VerifyCallerTask {

    public VerifyGoogleCallerTask() {
        setClassName("st.extreme.calling.Google");
        setExpectedPackage("st.extreme.google");
        setForbiddenPackage("com.google");
    }
}
