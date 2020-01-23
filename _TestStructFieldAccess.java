package ast;

import java.io.IOException;

import jscan.Tokenlist;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class _TestStructFieldAccess {

  @Test
  public void testSymbolsEasy() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                                                          \n");
    sb.append(" /*002*/      struct s {                                                        \n");
    sb.append(" /*003*/          int i;                                                        \n");
    sb.append(" /*004*/          // error: incomplete                                          \n");
    sb.append(" /*005*/          // struct r r;                                                \n");
    sb.append(" /*006*/          //                                                            \n");
    sb.append(" /*007*/          // ok: pointer to incomplete                                  \n");
    sb.append(" /*008*/          struct r *r;                                                  \n");
    sb.append(" /*009*/      };                                                                \n");
    sb.append(" /*010*/      struct s str, *sp = &str;                                         \n");
    sb.append(" /*011*/      // ok: assign pointer integer zero                                \n");
    sb.append(" /*012*/      str.r = 0;                                                        \n");
    sb.append(" /*013*/      sp->r = 0;                                                        \n");
    sb.append(" /*014*/      // warn: make pointer from integer without a cast...              \n");
    sb.append(" /*015*/      // str.r = 1;                                                     \n");
    sb.append(" /*016*/      // sp->r = 1;                                                     \n");
    sb.append(" /*017*/      // error: dereferencing pointer to incomplete type \'struct r\'   \n");
    sb.append(" /*018*/      // str.r->z = 0;                                                  \n");
    sb.append(" /*019*/      // error: dereferencing pointer to incomplete type \'struct r\'   \n");
    sb.append(" /*020*/      // sp->r->z = 0;                                                  \n");
    sb.append(" /*021*/      return 0;                                                         \n");
    sb.append(" /*022*/  }                                                                     \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();

    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
