package ast;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.arrayinit.OffsetInitializer;
import ast.declarations.Initializer;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class _TestArrayInitializers {

  @Test
  public void testFirst() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                                      \n");
    sb.append(" /*002*/      int arr1[2][2]    = {{1,2}, {3,4}};                         \n");
    sb.append(" /*003*/      //                                                          \n");
    sb.append(" /*004*/      int arr2[2][2][2] = { {{1,2}, {3,4}}, {{5, 6}, {7, 8}} };   \n");
    sb.append(" /*005*/      //                                                          \n");
    sb.append(" /*006*/      int arr3[2][2][2] = {1,2,3,4,5,6,7,8};                      \n");
    sb.append(" /*007*/      //                                                          \n");
    sb.append(" /*008*/      int arr4[2][2][2] = { {1}, {{5, 6}, {7, 8}} };              \n");
    sb.append(" /*009*/      //                                                          \n");
    sb.append(" /*010*/      int arr5[2][2]    = {1,2,3,4};                              \n");
    sb.append(" /*011*/      //                                                          \n");
    sb.append(" /*012*/      int arr6[3][4] = {                                          \n");
    sb.append(" /*013*/         {0, 1, 2, 3} ,                                           \n");
    sb.append(" /*014*/         {4, 5, 6, 7} ,                                           \n");
    sb.append(" /*015*/         {8, 9, 10, 11}                                           \n");
    sb.append(" /*016*/      };                                                          \n");
    sb.append(" /*017*/      //                                                          \n");
    sb.append(" /*018*/      int arr7[2][3][4] = {                                       \n");
    sb.append(" /*019*/          {{3, 4, 2, 3}, {0, -3, 9, 11}, {23, 12, 23, 2}},        \n");
    sb.append(" /*020*/          {{13, 4, 56, 3}, {5, 9, 3, 5}, {3, 1, 4, 9}}};          \n");
    sb.append(" /*021*/      //                                                          \n");
    sb.append(" /*022*/      int arr8[3][5] =                                            \n");
    sb.append(" /*023*/      {                                                           \n");
    sb.append(" /*024*/        { 1, 2, 3, 4, 5 },                                        \n");
    sb.append(" /*025*/        { 6, 7, 8, 9, 10 },                                       \n");
    sb.append(" /*026*/        { 11, 12, 13, 14, 15 }                                    \n");
    sb.append(" /*027*/      };                                                          \n");
    sb.append(" /*028*/      //                                                          \n");
    sb.append(" /*029*/      int arr9[3][5] =                                            \n");
    sb.append(" /*030*/      {                                                           \n");
    sb.append(" /*031*/        { 1, 2 },           // row 0 = 1, 2, 0, 0, 0              \n");
    sb.append(" /*032*/        { 6, 7, 8 },        // row 1 = 6, 7, 8, 0, 0              \n");
    sb.append(" /*033*/        { 11, 12, 13, 14 }  // row 2 = 11, 12, 13, 14, 0          \n");
    sb.append(" /*034*/      };                                                          \n");
    sb.append("              int arr10[2][3][3] = {1,2,3,4,5,6,7,8,9,10};                \n");
    sb.append(" /*035*/  }                                                               \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getName().getName().equals("__func__")) {
        continue;
      }
      if (sym.getInitializer() == null) {
        continue;
      }
      final Initializer initializer = sym.getInitializer();
      System.out.println(sym.getName().getName() + ":");
      OffsetInitializer offsetBuilder = new OffsetInitializer(sym, initializer);
      //offsetBuilder.show();
    }
  }

  @Ignore
  @Test
  public void testArraysSimple() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                                                   \n");
    sb.append(" /*002*/      int a1[2][3] = {{1, 3, 0}, {-1, 5, 9}};                    \n");
    sb.append(" /*003*/      int a2[][3] = {{1, 3, 0}, {-1, 5, 9}};                     \n");
    sb.append(" /*004*/      int a3[2][3] = {1, 3, 0, -1, 5, 9};                        \n");
    sb.append(" /*005*/      int a4[][2][2] = { {{1, 2}, {3, 4}}, {{5, 6}, {7, 8}} };   \n");
    sb.append(" /*006*/      int a5[1][2][3][4][5] = { 0 };                             \n");
    sb.append(" /*007*/      return 0;                                                  \n");
    sb.append(" /*008*/  }                                                              \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
