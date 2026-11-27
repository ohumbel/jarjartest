package st.extreme.calling;

import com.google.common.base.CharMatcher;

public class Google {

    public boolean matches(String keyword) {
        return CharMatcher.ascii().matchesAllOf(keyword);
    }

}