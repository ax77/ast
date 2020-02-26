package ast;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;
import jscan.tokenize.Stream;
import jscan.tokenize.Token;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.errors.ParseException;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class TestTrace {

  @Test(expected = ParseException.class)
  public void testErrorTrace_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {    \n");
    sb.append(" /*002*/    int a;        \n");
    sb.append(" /*003*/    int b;        \n");
    sb.append(" /*004*/    for(;;) {     \n");
    sb.append(" /*005*/      if(a) {     \n");
    sb.append(" /*006*/        b += 1;   \n");
    sb.append(" /*007*/      }           \n");
    sb.append(" /*008*/    }             \n");
    sb.append(" /*009*/    return 0 !    \n");
    sb.append(" /*010*/  }               \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
