package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Ignore;
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

public class _TestArrayInitializers {

  //I)
  /////////////////////////////////////////////////////////////////////
  //
  //int arr[][2][2] = { {{1,2},3,4},{5},6 }; // x?==3
  //arr.2.0:
  //.long   1
  //.long   2
  //.long   3
  //.long   4
  //---
  //.long   5
  //.long   0
  //.long   0
  //.long   0
  //---
  //.long   6
  //.long   0
  //.long   0
  //.long   0

  @Test
  public void test_1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                    \n");
    sb.append(" /*002*/     int arr[][2][2] = { {{1,2},3,4},{5},6 };   \n");
    sb.append(" /*035*/  }                                             \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(3, blocks.getInitsCount());

      final String tocheck = "1,2,3,4,5,0,0,0,6,0,0,0";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }
  }

  //II)
  /////////////////////////////////////////////////////////////////////
  //
  //int arr[][2] = { {1}, {2}, 3,4,5,6,7 }; // x?==5
  //arr.2.0:
  //.long   1
  //.long   0
  //---
  //.long   2
  //.long   0
  //---
  //.long   3
  //.long   4
  //---
  //.long   5
  //.long   6
  //---
  //.long   7
  //.long   0

  @Test
  public void test_2() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                    \n");
    sb.append(" /*002*/     int arr[][2] = { {1}, {2}, 3,4,5,6,7 };    \n");
    sb.append(" /*035*/  }                                             \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(5, blocks.getInitsCount());

      final String tocheck = "1,0,2,0,3,4,5,6,7,0";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }
  }

  //III)
  /////////////////////////////////////////////////////////////////////
  //
  //int arr[][2][3] = { {{1}}, {{2, 3}}, 4,5,6,7, };
  //arr.2.0:
  //.long   1
  //.long   0
  //.long   0
  //.long   0
  //.long   0
  //.long   0
  //
  //.long   2
  //.long   3
  //.long   0
  //.long   0
  //.long   0
  //.long   0
  //
  //.long   4
  //.long   5
  //.long   6
  //.long   7
  //.long   0
  //.long   0

  @Test
  public void test_3() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                            \n");
    sb.append(" /*002*/     int arr[][2][3] = { {{1}}, {{2, 3}}, 4,5,6,7, };   \n");
    sb.append(" /*035*/  }                                                     \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(3, blocks.getInitsCount());

      final String tocheck = "1,0,0,0,0,0,2,3,0,0,0,0,4,5,6,7,0,0";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }
  }

  //  int arr[][2] = { 1,2,3,4,5 };
  //  arr.2.0:
  //    .long   1
  //    .long   2
  //    .long   3
  //    .long   4
  //    .long   5
  //    .long   0

  @Test
  public void test_4() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                            \n");
    sb.append(" /*002*/     int arr[][2] = { 1,2,3,4,5 };   \n");
    sb.append(" /*035*/  }                                                     \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(3, blocks.getInitsCount());

      final String tocheck = "1,2,3,4,5,0";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }

  }

  @Test
  public void test_5() throws IOException {
    //    arr.2.0:
    //      .long   1
    //      .long   2
    //      .long   0
    //      .long   0
    //      .long   0
    //      .long   6
    //      .long   7
    //      .long   8
    //      .long   0
    //      .long   0
    //      .long   11
    //      .long   12
    //      .long   13
    //      .long   14
    //      .long   0

    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                               \n");
    sb.append(" /*002*/      int arr[3][5] =                                      \n");
    sb.append(" /*003*/      {                                                    \n");
    sb.append(" /*004*/        { 1, 2 },           // row 0 = 1, 2, 0, 0, 0       \n");
    sb.append(" /*005*/        { 6, 7, 8 },        // row 1 = 6, 7, 8, 0, 0       \n");
    sb.append(" /*006*/        { 11, 12, 13, 14 }  // row 2 = 11, 12, 13, 14, 0   \n");
    sb.append(" /*007*/      };                                                   \n");
    sb.append(" /*008*/  }                                                        \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(3, blocks.getInitsCount());

      final String tocheck = "1,2,0,0,0,6,7,8,0,0,11,12,13,14,0";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }

  }

  // int arr[] = { 1,2,3,4,5 };
  //  arr.2.0:
  //    .long   1
  //    .long   2
  //    .long   3
  //    .long   4
  //    .long   5

  @Test
  public void testOneDimensionalWithUnknownLen_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                  \n");
    sb.append(" /*002*/     int arr[] = { 1,2,3,4,5 };               \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(5, blocks.getInitsCount());

      final String tocheck = "1,2,3,4,5";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }

  }

  @Test
  public void testJustParse() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                                               \n");
    sb.append(" /*002*/      int a1[5] = {1, 1, 1, 1, 1};                                         \n");
    sb.append(" /*003*/      int a2[ ] = {1, 1, 1, 1, 1};                                         \n");
    sb.append(" /*004*/      int a3[5] = { };                                                     \n");
    sb.append(" /*005*/      int a4[5] = { 0 };                                                   \n");
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
    sb.append(" /*027*/      int c2[][3] = {{1, 3, 0}, {-1, 5, 9}};                               \n");
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
      for (InitNew e : mergingResult) {
        //System.out.println(e);
      }

    }

  }

  // int arr[6] = { [4] = 29, [2] = 15 };
  //  arr.2.0:
  //    .long   0
  //    .long   0
  //    .long   15
  //    .long   0
  //    .long   29
  //    .long   0

  @Ignore
  @Test
  public void testArrayDesignators_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                  \n");
    sb.append(" /*002*/     int arr[6] = { [4] = 29, [2] = 15 };     \n");
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

      Blocks blocks = BlocksBuilder.build(sym, initializer);
      final List<InitNew> mergingResult = blocks.getMergingResult();

      assertEquals(3, blocks.getInitsCount());

      final String tocheck = "1,2,3,4,5,0";
      final String[] split = tocheck.split(",");

      assertEquals(split.length, mergingResult.size());
      checkOneByOne(mergingResult, split);
    }

  }

  private void checkOneByOne(final List<InitNew> mergingResult, final String[] split) {
    for (int x = 0; x < mergingResult.size(); x++) {
      Integer expected = Integer.parseInt(split[x]);
      int actual = (int) mergingResult.get(x).getExpression().getCnumber().getClong();
      assertEquals(expected.intValue(), actual);
    }
  }

}
