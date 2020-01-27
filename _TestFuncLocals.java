package ast;

import java.io.IOException;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.ExternalDeclaration;
import ast.unit.FunctionDefinition;
import ast.unit.TranslationUnit;
import jscan.Tokenlist;

public class _TestFuncLocals {

  @Test
  public void test() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {             \n");
    sb.append(" /*002*/    int x = 0;             \n");
    sb.append(" /*003*/    {                      \n");
    sb.append(" /*004*/      long x = 1;          \n");
    sb.append(" /*005*/      {                    \n");
    sb.append(" /*006*/        long long x = 2;   \n");
    sb.append(" /*007*/      }                    \n");
    sb.append(" /*008*/    }                      \n");
    sb.append(" /*009*/    return x;              \n");
    sb.append(" /*010*/  }                        \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    processLocals(unit);
  }

  private void processLocals(TranslationUnit unit) {
    for (ExternalDeclaration ed : unit.getExternalDeclarations()) {
      if (!ed.isFunctionDefinition()) {
        continue;
      }
      FunctionDefinition fd = ed.getFunctionDefinition();
      for (CSymbol local : fd.getLocals()) {
        //System.out.println(local.toString());
      }
    }
  }

}
