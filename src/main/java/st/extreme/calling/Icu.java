package st.extreme.calling;

import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;

public class Icu {

    public String prepare(String label) throws StringPrepParseException {
        return StringPrep.getInstance(StringPrep.RFC3491_NAMEPREP).prepare(label, StringPrep.ALLOW_UNASSIGNED);
    }

}
