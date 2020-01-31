package ast;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.declarations.Initializer;
import ast.initarr.Blocks;
import ast.initarr.BlocksBuilder;
import ast.initarr.InitNew;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class _TestArrayInitializers2 {

  @Test
  public void testJustParse() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                                               \n");
    sb.append(" /*002*/      int a1[5] = {1, 1, 1, 1, 1};                                         \n");
    sb.append(" /*003*/      int a2[ ] = {1, 1, 1, 1, 1};                                         \n");
    sb.append(" /*004*/      int a3[5] = { };                                                     \n");
    sb.append(" /*005*/      int a4[5] = { -1 };                                                   \n");
    sb.append(" /*006*/      short q1[4][3][2] = { { { } } };                                     \n");
    sb.append(" /*007*/      short q2[4][3][2] = {                                                \n");
    sb.append(" /*008*/          { 1 },                                                           \n");
    sb.append(" /*009*/          { 2, 3 },                                                        \n");
    sb.append(" /*010*/          { 4, 5, 6 }                                                      \n");
    sb.append(" /*011*/      };                                                                   \n");
    sb.append(" /*012*/      short q3[4][3][2] = {1, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 4, 5, 6};   \n");
    sb.append(" /*013*/      short q4[4][3][2] = {                                                \n");
    sb.append(" /*014*/          {                                                                \n");
    sb.append(" /*015*/              { 1 },                                                       \n");
    sb.append(" /*016*/          },                                                               \n");
    sb.append(" /*017*/          {                                                                \n");
    sb.append(" /*018*/              { 2, 3 },                                                    \n");
    sb.append(" /*019*/          },                                                               \n");
    sb.append(" /*020*/          {                                                                \n");
    sb.append(" /*021*/              { 4, 5 },                                                    \n");
    sb.append(" /*022*/              { 6 },                                                       \n");
    sb.append(" /*023*/          }                                                                \n");
    sb.append(" /*024*/      };                                                                   \n");
    sb.append(" /*025*/      int c0[3][4] = {0,1,2,3,4,5,6,7,8,9,10,11};                          \n");
    sb.append(" /*026*/      int c1[2][3] = {{1, 3, 0}, {-1, 5, 9}};                              \n");
    sb.append(" /*027*/      int c2[][3] = {{1, 3, }, {-1, 5, 9}};                               \n");
    sb.append(" /*028*/      int c3[2][3] = {1, 3, 0, -1, 5, 9};                                  \n");
    sb.append(" /*029*/  }                                                                        \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getName().getName().equals("__func__")) {
        continue;
      }
      if (!sym.isArray()) {
        continue;
      }
      final Initializer initializer = sym.getInitializer();

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      //System.out.println(sym.getName().getName() + " :: " + sym.getType().getTpArray().getArrayLen());
      for (List<InitNew> e : blocks.getFixupBlocks()) {
        //System.out.println(e);
      }
      //System.out.println();

    }

  }

}
