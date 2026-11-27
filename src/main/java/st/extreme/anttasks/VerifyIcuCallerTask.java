package st.extreme.anttasks;

public class VerifyIcuCallerTask extends VerifyCallerTask {

    public VerifyIcuCallerTask() {
        setClassName("st.extreme.calling.Icu");
        setExpectedPackage("st.extreme.icu");
        setForbiddenPackage("com.ibm.icu");
    }
}
