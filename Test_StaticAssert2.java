package ast;

import java.io.IOException;
import java.util.List;

import jscan.tokenize.Stream;
import jscan.tokenize.Token;

import org.junit.Test;

import ast.errors.ParseException;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class Test_StaticAssert2 {

  private static Stream getHashedStream(String source) throws IOException {
    return new Stream("", source);
  }

  @Test
  public void testStaticAssert1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {             \n");
    sb.append(" /*002*/      _Static_assert(1);   \n");
    sb.append(" /*003*/      return 0;            \n");
    sb.append(" /*004*/  }                        \n");
    //@formatter:on

    List<Token> tokenlist = getHashedStream(sb.toString()).getTokenlist();
    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();

  }

  @Test(expected = ParseException.class)
  public void testStaticAssert2() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {             \n");
    sb.append(" /*002*/      _Static_assert(0);   \n");
    sb.append(" /*003*/      return 0;            \n");
    sb.append(" /*004*/  }                        \n");
    //@formatter:on

    List<Token> tokenlist = getHashedStream(sb.toString()).getTokenlist();
    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();

  }
}
