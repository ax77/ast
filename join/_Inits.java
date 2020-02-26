package ast.join;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast._typesnew.CType;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class _Inits {

  @Test
  public void testArrayInitializers_0() throws IOException {
    //    int arr_07[3][2][3] = { {{1}}, {{2, 3}}, 4,5,6,7, };
    //    arr_07.2.0:
    //      .long   1
    //      .long   0
    //      .long   0
    //      .long   0
    //      .long   0
    //      .long   0
    //      .long   2
    //      .long   3
    //      .long   0
    //      .long   0
    //      .long   0
    //      .long   0
    //      .long   4
    //      .long   5
    //      .long   6
    //      .long   7
    //      .long   0
    //      .long   0

    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append("void f() {                                                 \n");
    sb.append("    int arr_00[1][2][3] = { {{1,2,3} ,  {4,5,6}} };        \n");
    sb.append("    int arr_01[1][2][3] = {  {1,2,3  ,   4,5,6}  };        \n");
    sb.append("    int arr_02[1][2][3] = {   1,2,3  ,   4,5,6   };        \n");
    sb.append("    int arr_03[1][2][3] = { {{1    } ,  {4,5,6,}} };       \n");
    sb.append("    int arr_04[3][2][2] = { {{1,2},3,4},{5},6 }; //3       \n");
    sb.append("    int arr_05[1][2][3] = { 1 };                           \n");
    sb.append("    int arr_06[1][2][3] = { {1,2,3 ,  {4,5,6}} };          \n");
    sb.append("    int arr_07[3][2][3] = { {{1}}, {{2, 3}}, 4,5,6,7, };   \n");
    sb.append("    int arr_08[5] = {1,2,3,4,5}; //5                       \n");
    sb.append("    int arr_09[ ] = {1,2,3,4,5}; //5                       \n");
    sb.append("    int arr_10[5][2] = { {1}, {2}, 3,4,5,6,7 }; //5        \n");
    //
    sb.append("    int arr_11[2][2][2] = { {{1,2},3,4},{5} };             \n");
    sb.append("    int arr_12[2][2][2] = { {{1,2},{3,4}},{{5,6},{7,8}} }; \n");
    sb.append("    int arr_13[2][2][2] = { {{1},{3}},{{5},{7}} };         \n");
    sb.append("    int arr_14[ ][2][2] = { 1,2,3,4,5,6,7,8 };             \n");
    sb.append("}                                                          \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      final String name = sym.getName().getName();
      if (!name.startsWith("arr")) {
        continue;
      }

      List<InitNew> inits = new MIR_init(sym.getType(), sym.getInitializer()).getInits();
      final CType type = sym.getType();

      //      System.out.printf("name=%s, arrlen=%d, typesize=%d\n", name, type.getTpArray().getArrayLen(), type.getSize());
      //      for (InitNew init : inits) {
      //        System.out.println(init);
      //      }
      //      System.out.println();

    }

  }

  @Test
  public void testArrayInitializersSizes_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                                               \n");
    sb.append(" /*002*/      int a1[5] = {1, 1, 1, 1, 1};                                         \n");
    sb.append(" /*003*/      int a2[ ] = {1, 1, 1, 1, 1}; //5                                     \n");
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
    sb.append(" /*027*/      int c2[][3] = {{1, 3, 0}, {-1, 5, 9}}; //2                           \n");
    sb.append(" /*028*/      int c3[2][3] = {1, 3, 0, -1, 5, 9};                                  \n");
    sb.append(" /*029*/      int arr1[][2][2] = { {{1,2},3,4},{5},6 }; //3                        \n");
    sb.append(" /*030*/      int arr2[][2] = { {1}, {2}, 3,4,5,6,7 }; //5                         \n");
    sb.append(" /*031*/      int arr3[][2][3] = { {{1}}, {{2, 3}}, 4,5,6,7, }; //3                \n");
    sb.append(" /*032*/  }                                                                        \n");

    Map<String, Integer> sizes=new HashMap<String, Integer>();
    sizes.put("a1"    , 5);
    sizes.put("a2"    , 5);
    sizes.put("a3"    , 5);
    sizes.put("a4"    , 5);
    sizes.put("q1"    , 4);
    sizes.put("q2"    , 4);
    sizes.put("q3"    , 4);
    sizes.put("q4"    , 4);
    sizes.put("c0"    , 3);
    sizes.put("c1"    , 2);
    sizes.put("c2"    , 2);
    sizes.put("c3"    , 2);
    sizes.put("arr1"  , 3);
    sizes.put("arr2"  , 5);
    sizes.put("arr3"  , 3);
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();

    for (CSymbol sym : locals) {
      final String name = sym.getName().getName();
      if (!name.startsWith("arr")) {
        continue;
      }

      List<InitNew> inits = new MIR_init(sym.getType(), sym.getInitializer()).getInits();
      final CType type = sym.getType();

      final int lenExpected = sizes.get(name).intValue();
      final int lenActual = type.getTpArray().getArrayLen();

      if (lenExpected != lenActual) {
        System.out.println(name);
      }

      assertEquals(lenExpected, lenActual);

    }

  }

  @Test
  public void testArrayInitializersSizes_1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     \n");
    sb.append(" /*002*/      int arr_00[] = {3,} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                      \n");
    sb.append(" /*003*/      int arr_01[][4][4] = {{{25,84,57,33,},{25,84,57,33,},{25,84,57,33,},{25,84,57,33,},},} ;                                                                                                                                                                                                                                                                                                                                                                                                   \n");
    sb.append(" /*004*/      int arr_02[][4][4] = {{{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},},{{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},},{{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},},{{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},{64,36,4,21,},},} ;                                                                                                                                                                                                                      \n");
    sb.append(" /*005*/      int arr_03[][4][1][2] = {{{{74,14,},},{{74,14,},},{{74,14,},},{{74,14,},},},} ;                                                                                                                                                                                                                                                                                                                                                                                                            \n");
    sb.append(" /*006*/      int arr_04[][3][1][3] = {{{{12,50,108,},},{{12,50,108,},},{{12,50,108,},},},{{{12,50,108,},},{{12,50,108,},},{{12,50,108,},},},{{{12,50,108,},},{{12,50,108,},},{{12,50,108,},},},} ;                                                                                                                                                                                                                                                                                                      \n");
    sb.append(" /*007*/      int arr_05[][1][4][4] = {{{{21,31,58,58,},{21,31,58,58,},{21,31,58,58,},{21,31,58,58,},},},{{{21,31,58,58,},{21,31,58,58,},{21,31,58,58,},{21,31,58,58,},},},{{{21,31,58,58,},{21,31,58,58,},{21,31,58,58,},{21,31,58,58,},},},} ;                                                                                                                                                                                                                                                         \n");
    sb.append(" /*008*/      int arr_06[] = {22,39,73,} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                               \n");
    sb.append(" /*009*/      int arr_07[][1] = {{66,},{66,},{66,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                   \n");
    sb.append(" /*010*/      int arr_08[][2][3] = {{{0,88,31,},{0,88,31,},},{{0,88,31,},{0,88,31,},},{{0,88,31,},{0,88,31,},},} ;                                                                                                                                                                                                                                                                                                                                                                                       \n");
    sb.append(" /*011*/      int arr_09[] = {70,45,0,} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                \n");
    sb.append(" /*012*/      int arr_10[][2][3] = {{{1,0,1,},{1,0,1,},},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                             \n");
    sb.append(" /*013*/      int arr_11[][4] = {{12,3,93,34,},{12,3,93,34,},{12,3,93,34,},{12,3,93,34,},} ;                                                                                                                                                                                                                                                                                                                                                                                                             \n");
    sb.append(" /*014*/      int arr_12[][2] = {{11,10,},{11,10,},{11,10,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                          \n");
    sb.append(" /*015*/      int arr_13[][3][1][4] = {{{{33,43,1,12,},},{{33,43,1,12,},},{{33,43,1,12,},},},{{{33,43,1,12,},},{{33,43,1,12,},},{{33,43,1,12,},},},{{{33,43,1,12,},},{{33,43,1,12,},},{{33,43,1,12,},},},} ;                                                                                                                                                                                                                                                                                             \n");
    sb.append(" /*016*/      int arr_14[][3][3] = {{{91,1,85,},{91,1,85,},{91,1,85,},},{{91,1,85,},{91,1,85,},{91,1,85,},},} ;                                                                                                                                                                                                                                                                                                                                                                                          \n");
    sb.append(" /*017*/      int arr_15[][1][3][2][1] = {{{{{32,},{32,},},{{32,},{32,},},{{32,},{32,},},},},{{{{32,},{32,},},{{32,},{32,},},{{32,},{32,},},},},} ;                                                                                                                                                                                                                                                                                                                                                      \n");
    sb.append(" /*018*/      int arr_16[][2][4][2] = {{{{5,20,},{5,20,},{5,20,},{5,20,},},{{5,20,},{5,20,},{5,20,},{5,20,},},},{{{5,20,},{5,20,},{5,20,},{5,20,},},{{5,20,},{5,20,},{5,20,},{5,20,},},},{{{5,20,},{5,20,},{5,20,},{5,20,},},{{5,20,},{5,20,},{5,20,},{5,20,},},},{{{5,20,},{5,20,},{5,20,},{5,20,},},{{5,20,},{5,20,},{5,20,},{5,20,},},},} ;                                                                                                                                                           \n");
    sb.append(" /*019*/      int arr_17[][1][3][2] = {{{{60,72,},{60,72,},{60,72,},},},{{{60,72,},{60,72,},{60,72,},},},} ;                                                                                                                                                                                                                                                                                                                                                                                             \n");
    sb.append(" /*020*/      int arr_18[][2] = {{26,13,},{26,13,},{26,13,},{26,13,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                 \n");
    sb.append(" /*021*/      int arr_19[][1][3][1] = {{{{23,},{23,},{23,},},},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                       \n");
    sb.append(" /*022*/      int arr_20[][4][2] = {{{9,40,},{9,40,},{9,40,},{9,40,},},} ;                                                                                                                                                                                                                                                                                                                                                                                                                               \n");
    sb.append(" /*023*/      int arr_21[][3][1] = {{{10,},{10,},{10,},},{{10,},{10,},{10,},},} ;                                                                                                                                                                                                                                                                                                                                                                                                                        \n");
    sb.append(" /*024*/      int arr_22[] = {11,2,} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                   \n");
    sb.append(" /*025*/      int arr_23[][1][1] = {{{2,},},{{2,},},{{2,},},{{2,},},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                  \n");
    sb.append(" /*026*/      int arr_24[][3] = {{16,6,15,},{16,6,15,},{16,6,15,},{16,6,15,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                         \n");
    sb.append(" /*027*/      int arr_25[][4][2] = {{{16,1,},{16,1,},{16,1,},{16,1,},},{{16,1,},{16,1,},{16,1,},{16,1,},},{{16,1,},{16,1,},{16,1,},{16,1,},},{{16,1,},{16,1,},{16,1,},{16,1,},},} ;                                                                                                                                                                                                                                                                                                                      \n");
    sb.append(" /*028*/      int arr_26[][3][3][2] = {{{{44,1,},{44,1,},{44,1,},},{{44,1,},{44,1,},{44,1,},},{{44,1,},{44,1,},{44,1,},},},{{{44,1,},{44,1,},{44,1,},},{{44,1,},{44,1,},{44,1,},},{{44,1,},{44,1,},{44,1,},},},} ;                                                                                                                                                                                                                                                                                       \n");
    sb.append(" /*029*/      int arr_27[][2] = {{81,14,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                            \n");
    sb.append(" /*030*/      int arr_28[][4] = {{32,38,32,6,},{32,38,32,6,},{32,38,32,6,},{32,38,32,6,},} ;                                                                                                                                                                                                                                                                                                                                                                                                             \n");
    sb.append(" /*031*/      int arr_29[][1] = {{15,},{15,},{15,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                   \n");
    sb.append(" /*032*/      int arr_30[][3] = {{10,11,64,},} ;                                                                                                                                                                                                                                                                                                                                                                                                                                                         \n");
    sb.append(" /*033*/      int arr_31[][3][3][3] = {{{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},},{{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},},{{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},},{{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},{{33,5,12,},{33,5,12,},{33,5,12,},},},} ;   \n");
    sb.append(" /*034*/  }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              \n");

    
    Map<String, Integer> sizes=new HashMap<String, Integer>();
    sizes.put("arr_00" , 1   );
    sizes.put("arr_01" , 1   );
    sizes.put("arr_02" , 4   );
    sizes.put("arr_03" , 1   );
    sizes.put("arr_04" , 3   );
    sizes.put("arr_05" , 3   );
    sizes.put("arr_06" , 3   );
    sizes.put("arr_07" , 3   );
    sizes.put("arr_08" , 3   );
    sizes.put("arr_09" , 3   );
    sizes.put("arr_10" , 1   );
    sizes.put("arr_11" , 4   );
    sizes.put("arr_12" , 3   );
    sizes.put("arr_13" , 3   );
    sizes.put("arr_14" , 2   );
    sizes.put("arr_15" , 2   );
    sizes.put("arr_16" , 4   );
    sizes.put("arr_17" , 2   );
    sizes.put("arr_18" , 4   );
    sizes.put("arr_19" , 1   );
    sizes.put("arr_20" , 1   );
    sizes.put("arr_21" , 2   );
    sizes.put("arr_22" , 2   );
    sizes.put("arr_23" , 4   );
    sizes.put("arr_24" , 4   );
    sizes.put("arr_25" , 4   );
    sizes.put("arr_26" , 2   );
    sizes.put("arr_27" , 1   );
    sizes.put("arr_28" , 4   );
    sizes.put("arr_29" , 3   );
    sizes.put("arr_30" , 1   );
    sizes.put("arr_31" , 4   );
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      final String name = sym.getName().getName();
      if (name.equals("__func__")) {
        continue;
      }
      if (!sym.isArray()) {
        continue;
      }

      List<InitNew> inits = new MIR_init(sym.getType(), sym.getInitializer()).getInits();
      final CType type = sym.getType();

      final int lenExpected = sizes.get(name).intValue();
      final int lenActual = type.getTpArray().getArrayLen();

      if (lenExpected != lenActual) {
        System.out.println(name);
      }

      assertEquals(lenExpected, lenActual);

    }

  }

  @Test
  public void testCountOfElements_0() throws IOException {

    Map<String, String> m = new HashMap<String, String>();
    m.put("arr0", "1 2 3 4 5 6");
    m.put("arr1", "1 2 3 4 5 6");
    m.put("arr2", "1 2 3 4 5 6");
    m.put("arr3", "1 0 0 4 5 6");
    m.put("arr4", "0 0 0 0 0 0");
    m.put("arr5", "1 0 0 0 0 0");
    m.put("arr6", "1 2 3 4 5 6");

    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                         \n");
    sb.append(" /*002*/      int arr0[1][2][3] = { {{1,2,3} ,  {4,5,6}} };  \n");
    sb.append(" /*002*/      int arr1[1][2][3] = {  {1,2,3  ,   4,5,6}  };  \n");
    sb.append(" /*002*/      int arr2[1][2][3] = {   1,2,3  ,   4,5,6   };  \n");
    sb.append(" /*002*/      int arr3[1][2][3] = { {{1    } ,  {4,5,6}} };  \n");
    sb.append(" /*002*/      int arr4[1][2][3] = {  };                      \n");
    sb.append(" /*002*/      int arr5[1][2][3] = { 1 };                     \n");
    sb.append(" /*002*/      int arr6[1][2][3] = { {1,2,3 ,  {4,5,6}} };    \n");
    sb.append(" /*035*/  }                                                  \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      final String name = sym.getName().getName();
      if (!name.startsWith("arr")) {
        continue;
      }

      List<InitNew> inits = new MIR_init(sym.getType(), sym.getInitializer()).getInits();
      String actual = "";
      for (InitNew init : inits) {
        actual += String.format("%s ", init.getInit());
      }
      assertEquals(m.get(name).trim(), actual.trim());

    }

  }

  @Test
  public void testCountOfElements_1() throws IOException {

    Map<String, String> m = new HashMap<String, String>();
    m.put("arr0", "1 2 3 4 5 0 0 0");
    m.put("arr1", "1 2 3 4 5 6 7 8");
    m.put("arr2", "1 0 3 0 5 0 7 0");
    m.put("arr3", "1 2 3 4 5 6 7 8");

    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                                                \n");
    sb.append(" /*002*/      int arr0[2][2][2] = { {{1,2},3,4},{5} };              \n");
    sb.append(" /*002*/      int arr1[2][2][2] = { {{1,2},{3,4}},{{5,6},{7,8}} };  \n");
    sb.append(" /*002*/      int arr2[2][2][2] = { {{1},{3}},{{5},{7}} };          \n");
    sb.append(" /*002*/      int arr3[2][2][2] = { 1,2,3,4,5,6,7,8 };              \n");
    sb.append(" /*035*/  }                                                         \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      final String name = sym.getName().getName();
      if (!name.startsWith("arr")) {
        continue;
      }

      List<InitNew> inits = new MIR_init(sym.getType(), sym.getInitializer()).getInits();
      String actual = "";
      for (InitNew init : inits) {
        actual += String.format("%s ", init.getInit());
      }
      assertEquals(m.get(name).trim(), actual.trim());

    }

  }

  @Test
  public void testParse_0() throws IOException {

    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {\n");
    sb.append(" /*002*/      int arr_00[2][2][2][1] = {{{{53,},{53,},},{{53,},{53,},},},{{{53,},{53,},},{{53,},{53,},},},} ;\n");
    sb.append(" /*003*/      int arr_01[2][1] = {{22,},{22,},} ;\n");
    sb.append(" /*004*/      int arr_02[2][2] = {{14,3,},{14,3,},} ;\n");
    sb.append(" /*005*/      int arr_03[2][2][1][2] = {{{{1,46,},},{{1,46,},},},{{{1,46,},},{{1,46,},},},} ;\n");
    sb.append(" /*006*/      int arr_04[2][1] = {{79,},{79,},} ;\n");
    sb.append(" /*007*/      int arr_05[1][1][1][1] = {{{{0,},},},} ;\n");
    sb.append(" /*008*/      int arr_06[1][1][1] = {{{38,},},} ;\n");
    sb.append(" /*009*/      int arr_07[2][1][1][2] = {{{{10,27,},},},{{{10,27,},},},} ;\n");
    sb.append(" /*010*/      int arr_08[2][2][1] = {{{0,},{0,},},{{0,},{0,},},} ;\n");
    sb.append(" /*011*/      int arr_09[1][2][1][1][1] = {{{{{14,},},},{{{14,},},},},} ;\n");
    sb.append(" /*012*/      int arr_10[2][1][1] = {{{36,},},{{36,},},} ;\n");
    sb.append(" /*013*/      int arr_11[1][2][1][1][1] = {{{{{10,},},},{{{10,},},},},} ;\n");
    sb.append(" /*014*/      int arr_12[2][1][1] = {{{15,},},{{15,},},} ;\n");
    sb.append(" /*015*/      int arr_13[2][1][2][1] = {{{{84,},{84,},},},{{{84,},{84,},},},} ;\n");
    sb.append(" /*016*/      int arr_14[1][2] = {{2,32,},} ;\n");
    sb.append(" /*017*/      int arr_15[1][1] = {{6,},} ;\n");
    sb.append(" /*018*/      int arr_16[2][1][2] = {{{13,20,},},{{13,20,},},} ;\n");
    sb.append(" /*019*/      int arr_17[1][1] = {{9,},} ;\n");
    sb.append(" /*020*/      int arr_18[1][1] = {{52,},} ;\n");
    sb.append(" /*021*/      int arr_19[1][2][1] = {{{35,},{35,},},} ;\n");
    sb.append(" /*022*/      int arr_20[2][1][1] = {{{15,},},{{15,},},} ;\n");
    sb.append(" /*023*/      int arr_21[2][2] = {{43,25,},{43,25,},} ;\n");
    sb.append(" /*024*/      int arr_22[2][2][1] = {{{14,},{14,},},{{14,},{14,},},} ;\n");
    sb.append(" /*025*/      int arr_23[1][1][1] = {{{100,},},} ;\n");
    sb.append(" /*026*/      int arr_24[1][1] = {{2,},} ;\n");
    sb.append(" /*027*/      int arr_25[1][1][2][1][2] = {{{{{18,39,},},{{18,39,},},},},} ;\n");
    sb.append(" /*028*/      int arr_26[2][1][2] = {{{33,47,},},{{33,47,},},} ;\n");
    sb.append(" /*029*/      int arr_27[2][2] = {{87,51,},{87,51,},} ;\n");
    sb.append(" /*030*/      int arr_28[2][2][1][1][2] = {{{{{11,86,},},},{{{11,86,},},},},{{{{11,86,},},},{{{11,86,},},},},} ;\n");
    sb.append(" /*031*/      int arr_29[2][1][1][1] = {{{{64,},},},{{{64,},},},} ;\n");
    sb.append(" /*032*/      int arr_30[1][2][1][2] = {{{{39,24,},},{{39,24,},},},} ;\n");
    sb.append(" /*033*/      int arr_31[1][1] = {{48,},} ;\n");
    sb.append(" /*034*/      int arr_32[1][2][1] = {{{26,},{26,},},} ;\n");
    sb.append(" /*035*/      int arr_33[2][1][2][1] = {{{{43,},{43,},},},{{{43,},{43,},},},} ;\n");
    sb.append(" /*036*/      int arr_34[1][2][1][2] = {{{{19,87,},},{{19,87,},},},} ;\n");
    sb.append(" /*037*/      int arr_35[2][1][2][1] = {{{{13,},{13,},},},{{{13,},{13,},},},} ;\n");
    sb.append(" /*038*/      int arr_36[1][2][1] = {{{14,},{14,},},} ;\n");
    sb.append(" /*039*/      int arr_37[1][2] = {{3,0,},} ;\n");
    sb.append(" /*040*/      int arr_38[1][2][2][2][2] = {{{{{36,8,},{36,8,},},{{36,8,},{36,8,},},},{{{36,8,},{36,8,},},{{36,8,},{36,8,},},},},} ;\n");
    sb.append(" /*041*/      int arr_39[1][1][2][1] = {{{{0,},{0,},},},} ;\n");
    sb.append(" /*042*/      int arr_40[1][2][2][1] = {{{{4,},{4,},},{{4,},{4,},},},} ;\n");
    sb.append(" /*043*/      int arr_41[2][1][2][2] = {{{{24,7,},{24,7,},},},{{{24,7,},{24,7,},},},} ;\n");
    sb.append(" /*044*/      int arr_42[2][1][1][2] = {{{{42,7,},},},{{{42,7,},},},} ;\n");
    sb.append(" /*045*/      int arr_43[1][1][2] = {{{58,20,},},} ;\n");
    sb.append(" /*046*/      int arr_44[2][1][1] = {{{40,},},{{40,},},} ;\n");
    sb.append(" /*047*/      int arr_45[2][2][2] = {{{74,9,},{74,9,},},{{74,9,},{74,9,},},} ;\n");
    sb.append(" /*048*/      int arr_46[2][2] = {{43,91,},{43,91,},} ;\n");
    sb.append(" /*049*/      int arr_47[2][1][2] = {{{2,23,},},{{2,23,},},} ;\n");
    sb.append(" /*050*/      int arr_48[2][2][2] = {{{43,35,},{43,35,},},{{43,35,},{43,35,},},} ;\n");
    sb.append(" /*051*/      int arr_49[2][1][2] = {{{63,46,},},{{63,46,},},} ;\n");
    sb.append(" /*052*/      int arr_50[1][2][2][2] = {{{{34,1,},{34,1,},},{{34,1,},{34,1,},},},} ;\n");
    sb.append(" /*053*/      int arr_51[1][2] = {{11,56,},} ;\n");
    sb.append(" /*054*/      int arr_52[1][1][1] = {{{44,},},} ;\n");
    sb.append(" /*055*/      int arr_53[1][1][2] = {{{50,70,},},} ;\n");
    sb.append(" /*056*/      int arr_54[1][2][1][2] = {{{{0,3,},},{{0,3,},},},} ;\n");
    sb.append(" /*057*/      int arr_55[1][1][2] = {{{61,48,},},} ;\n");
    sb.append(" /*058*/      int arr_56[2][1] = {{36,},{36,},} ;\n");
    sb.append(" /*059*/      int arr_57[2][2][1] = {{{28,},{28,},},{{28,},{28,},},} ;\n");
    sb.append(" /*060*/      int arr_58[2][1][2] = {{{22,36,},},{{22,36,},},} ;\n");
    sb.append(" /*061*/      int arr_59[2][2][2][1] = {{{{36,},{36,},},{{36,},{36,},},},{{{36,},{36,},},{{36,},{36,},},},} ;\n");
    sb.append(" /*062*/      int arr_60[1][1][1] = {{{9,},},} ;\n");
    sb.append(" /*063*/      int arr_61[2][2] = {{13,74,},{13,74,},} ;\n");
    sb.append(" /*064*/      int arr_62[1][1] = {{63,},} ;\n");
    sb.append(" /*065*/      int arr_63[2][2] = {{63,0,},{63,0,},} ;\n");
    sb.append(" /*066*/      int arr_64[2][1] = {{104,},{104,},} ;\n");
    sb.append(" /*067*/      int arr_65[2][1][1][1][2] = {{{{{15,67,},},},},{{{{15,67,},},},},} ;\n");
    sb.append(" /*068*/      int arr_66[2][1][1][1] = {{{{7,},},},{{{7,},},},} ;\n");
    sb.append(" /*069*/      int arr_67[1][2] = {{2,42,},} ;\n");
    sb.append(" /*070*/      int arr_68[2][1][2] = {{{14,39,},},{{14,39,},},} ;\n");
    sb.append(" /*071*/      int arr_69[1][1][2][1] = {{{{17,},{17,},},},} ;\n");
    sb.append(" /*072*/  }\n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      final String name = sym.getName().getName();
      if (!name.startsWith("arr")) {
        continue;
      }

      List<InitNew> inits = new MIR_init(sym.getType(), sym.getInitializer()).getInits();
      CType type = sym.getType();

      //      System.out.printf("name=%s, arrlen=%d, typesize=%d\n", name, type.getTpArray().getArrayLen(), type.getSize());
      //      for (InitNew init : inits) {
      //        System.out.println(init);
      //      }
      //      System.out.println();

    }

  }

}
