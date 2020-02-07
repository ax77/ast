package ast.newinits;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class _TestSizeFinder {

  @Test
  public void testSizes1() throws IOException {
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
      if (name.equals("__func__")) {
        continue;
      }
      if (!sym.isArray()) {
        continue;
      }

      List<JustOut> outlist = new TemplateJo(sym).getOutlist();

      final int lenExpected = sizes.get(name).intValue();
      final int lenActual = sym.getType().getTpArray().getArrayLen();

      if (lenExpected != lenActual) {
        System.out.println(name);
        String actual = "";
        for (JustOut jo : outlist) {
          actual += String.format("%s ", jo);
        }
        System.out.println(actual);
      }

      assertEquals(lenExpected, lenActual);

    }

  }

  @Test
  public void testSizes2() throws IOException {
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

      List<JustOut> outlist = new TemplateJo(sym).getOutlist();

      final int lenExpected = sizes.get(name).intValue();
      final int lenActual = sym.getType().getTpArray().getArrayLen();

      if (lenExpected != lenActual) {
        System.out.println(name);
        String actual = "";
        for (JustOut jo : outlist) {
          actual += String.format("%s ", jo);
        }
        System.out.println(actual);
      }

      assertEquals(lenExpected, lenActual);

    }

  }

}
