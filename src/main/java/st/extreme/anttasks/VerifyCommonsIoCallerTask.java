package st.extreme.anttasks;

public class VerifyCommonsIoCallerTask extends VerifyCallerTask {

    public VerifyCommonsIoCallerTask() {
        setClassName("st.extreme.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream");
        setExpectedPackage("st.extreme.apache.commons.io");
        setForbiddenPackage("org.apache.commons.io");
    }
}
