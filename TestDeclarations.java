package ast;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.decls.Initializer;
import ast.parse.Parse;
import ast.symtab.elements.CSymbol;
import ast.unit.TranslationUnit;

public class TestDeclarations {

  @Test
  public void testDeclarations1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                     \n");
    sb.append(" /*002*/      int a[2] = {1,2}, b, c, d;   \n");
    sb.append(" /*003*/      return 0;                    \n");
    sb.append(" /*004*/  }                                \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    boolean print = false;
    if (!print) {
      return;
    }

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getInitializer() != null) {
        System.out.printf("name=%s, type=%s\n", sym.getName().getName(), sym.getType());
        for (Initializer init : sym.getInitializer()) {
          System.out.println(init);
        }
        System.out.println();
      }
    }
  }

}
