package st.extreme.initial;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class UnmodifiedTest {

    @Test
    void testDoNotModifyMe() {
        Unmodified unmodified = new Unmodified();
        assertThatNoException().isThrownBy(unmodified::doNotModifyMe);
    }
}
