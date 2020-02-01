package ast;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.declarations.Initializer;
import ast.initarr.BlocksBuilder;
import ast.initarr.InitNew;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;
import jscan.Tokenlist;

public class _TestArrayInitializers3 {

  //  int arr[2][2] = { [0] = {1,2}, [1] = {3,4}, };
  //  arr.2.0:
  //    .long   1
  //    .long   2
  //    .long   3
  //    .long   4

  @Test
  public void testArrayDesignators_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                  \n");
    sb.append(" /*002*/     int arr[2][2] = { [0] = {1,2}, [1] = {3,4}, };     \n");
    sb.append(" /*035*/  }                                           \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (!sym.getName().getName().equals("arr")) {
        continue;
      }
      final Initializer initializer = sym.getInitializer();
      final List<InitNew> mergingResult = BlocksBuilder.build(sym, initializer);

    }

  }

}
