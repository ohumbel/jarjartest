package  st.extreme.calling;

import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;

public class Antlr {

   public Token createToken(){
       return new CommonToken(Token.EOF);
   }

}
