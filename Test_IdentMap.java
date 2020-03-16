package ast;

import static org.junit.Assert.*;

import java.io.IOException;

import jscan.Tokenlist;
import jscan.tokenize.T;
import jscan.tokenize.Token;

import org.junit.Test;

import ast._entry.IdentMap;
import ast.parse.Pcheckers;

public class Test_IdentMap {

  @Test
  public void testIdents() throws IOException {
    String source = "int int int int int int int";
    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(source, false)).pp();
    for (;;) {
      Token t = it.next();
      if (t.ofType(T.TOKEN_STREAMBEGIN) || t.ofType(T.TOKEN_STREAMEND)) {
        continue;
      }
      if (t.ofType(T.TOKEN_EOF)) {
        break;
      }
      assertEquals(IdentMap.int_ident, t.getIdent());
      assertTrue(t.isIdent(IdentMap.int_ident));
      assertTrue(Pcheckers.isTypeSpec(t));
    }
  }

}
