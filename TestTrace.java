package ast;

import java.io.IOException;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.declarations.Initializer;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class TestTrace {

  @Test
  public void testInits8cc() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {              \n");
    sb.append(" /*002*/      struct s {            \n");
    sb.append(" /*003*/          int a;            \n");
    sb.append(" /*004*/          int b[2];         \n");
    sb.append(" /*005*/          long long c;      \n");
    sb.append(" /*006*/      } varname[1] = {      \n");
    sb.append(" /*007*/        {                   \n");
    sb.append(" /*008*/            1, { 2,3 }, 4   \n");
    sb.append(" /*009*/        }                   \n");
    sb.append(" /*010*/      };                    \n");
    //
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
    //
    sb.append(" /*002*/      int arr_00[1][2][3] = { {{1,2,3} ,  {4,5,6}} };  \n");
    sb.append(" /*002*/      int arr_01[1][2][3] = {  {1,2,3  ,   4,5,6}  };  \n");
    sb.append(" /*002*/      int arr_02[1][2][3] = {   1,2,3  ,   4,5,6   };  \n");
    sb.append(" /*002*/      int arr_03[1][2][3] = { {{1    } ,  {4,5,6}} };  \n");
    sb.append(" /*002*/      int arr_04[1][2][3] = {  };                      \n");
    sb.append(" /*002*/      int arr_05[1][2][3] = { 1 };                     \n");
    sb.append(" /*002*/      int arr_06[1][2][3] = { {1,2,3 ,  {4,5,6}} };    \n");
    //
    sb.append(" /*002*/      int arr_10[2][2][2] = { {{1,2},3,4},{5} };              \n");
    sb.append(" /*002*/      int arr_11[2][2][2] = { {{1,2},{3,4}},{{5,6},{7,8}} };  \n");
    sb.append(" /*002*/      int arr_12[2][2][2] = { {{1},{3}},{{5},{7}} };          \n");
    //
    sb.append(" /*011*/      return 0;             \n");
    sb.append(" /*012*/  }                         \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    boolean print = false;
    if (!print) {
      return;
    }

    for (CSymbol sym : unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals()) {
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
