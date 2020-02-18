package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.newinits.JustOut;
import ast.newinits.TemplateJo;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class _TestInitsTemplatesInits {

  @Test
  public void testTemplatesGenerators() throws IOException {

    Map<String, String> m = new HashMap<String, String>();
    m.put("arr0", "{ { 1 2 3 } { 4 5 6 } }");
    m.put("arr1", "{ { 1 2 3 } { 4 5 6 } }");
    m.put("arr2", "{ { 1 2 3 } { 4 5 6 } }");
    m.put("arr3", "{ { 1 0 0 } { 4 5 6 } }");
    m.put("arr4", "{ { 0 0 0 } { 0 0 0 } }");
    m.put("arr5", "{ { 1 0 0 } { 0 0 0 } }");
    m.put("arr6", "{ { 1 2 3 } { 4 5 6 } }");

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

      List<JustOut> outlist = new TemplateJo(sym).getOutlist();
      String actual = "";
      for (JustOut jo : outlist) {
        actual += String.format("%s ", jo);
      }
      assertEquals(m.get(name).trim(), actual.trim());

    }

  }

  @Test
  public void testTemplates2() throws IOException {

    Map<String, String> m = new HashMap<String, String>();
    m.put("arr0", "{ { 1 2 } { 3 4 } } { { 5 0 } { 0 0 } }");
    m.put("arr1", "{ { 1 2 } { 3 4 } } { { 5 6 } { 7 8 } } ");
    m.put("arr2", "{ { 1 0 } { 3 0 } } { { 5 0 } { 7 0 } } ");
    m.put("arr3", "{ { 1 2 } { 3 4 } } { { 5 6 } { 7 8 } } ");

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

      final TemplateJo templateJo = new TemplateJo(sym);
      List<JustOut> outlist = templateJo.getOutlist();
      String actual = "";
      for (JustOut jo : outlist) {
        actual += String.format("%s ", jo);
      }
      assertEquals(m.get(name).trim(), actual.trim());

    }

  }

  @Test
  public void testTemplates3() throws IOException {

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

      final TemplateJo templateJo = new TemplateJo(sym);

    }

  }

}
