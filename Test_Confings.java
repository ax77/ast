package ast;

import java.io.IOException;

import jscan.Tokenlist;
import jscan.tokenize.T;
import jscan.tokenize.Token;

import org.junit.Test;

import ast._entry.ParseConf;
import static ast._entry.ParseConf.*;

public class Test_Confings {

  @Test
  public void testConfigString() throws IOException {
    String source = "int main() { return u8\"123\" u8\"456\"; }";
    ParseConf conf = new ParseConf(PREPROCESS_STRING_INPUT | APPLY_STR_CONCAT | PREPEND_PREDEFINED_BUFFER,
        UNIT_TEST_FILENAME, source);
    Tokenlist list = conf.preprocess();
    //    for (;;) {
    //      Token t = list.next();
    //      if (t.ofType(T.TOKEN_EOF)) {
    //        break;
    //      }
    //      if (t.hasLeadingWhitespace()) {
    //        System.out.printf("%s", " ");
    //      }
    //      System.out.printf("%s", t.getValue());
    //      if (t.isNewLine()) {
    //        System.out.printf("%s", "\n");
    //      }
    //    }
  }

}
