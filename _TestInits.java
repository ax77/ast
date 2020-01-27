package ast;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;
import jscan.preprocess.ScanExc;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.declarations.Initializer;
import ast.declarations.InitializerList;
import ast.declarations.InitializerListEntry;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

public class _TestInits {

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

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getName().getName().equals("__func__")) {
        continue;
      }
      System.out.println("init: " + sym.getName().getName());
      final Initializer initializer = sym.getInitializer();
      enumerate(initializer);
      showInitializer(initializer);
    }
  }

  @Ignore
  @Test
  public void testDesignations() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                    \n");
    sb.append(" /*002*/      struct s {                  \n");
    sb.append(" /*003*/          int type;               \n");
    sb.append(" /*004*/          struct ptr {            \n");
    sb.append(" /*005*/              int flag;           \n");
    sb.append(" /*006*/          } *ptr;                 \n");
    sb.append(" /*007*/      } opts = {                  \n");
    sb.append(" /*008*/          .type = 0,              \n");
    sb.append(" /*009*/          .ptr = &(struct ptr){   \n");
    sb.append(" /*010*/              .flag = -1,         \n");
    sb.append(" /*011*/          },                      \n");
    sb.append(" /*012*/      };                          \n");
    sb.append(" /*013*/      return 0;                   \n");
    sb.append(" /*014*/  }                               \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getName().getName().equals("__func__")) {
        continue;
      }
      System.out.println("init: " + sym.getName().getName());
      final Initializer initializer = sym.getInitializer();
      enumerate(initializer);
      showInitializer(initializer);
    }
  }

  /// PRINT

  private void enumerate(Initializer initializer) {
    if (initializer.isHasInitializerList()) {
      InitializerList initList = initializer.getInitializerList();
      List<InitializerListEntry> initializers = initList.getInitializers();
      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        if (entry.isDesignation()) {
          throw new ScanExc("unsupported now");
        }
        Initializer inittmp = entry.getInitializer();
        inittmp.pushall(initializer.getIndex());
        inittmp.pushi(j);
        enumerate(inittmp);
      }
    }
  }

  private void showInitializer(Initializer initializer) {
    if (!initializer.isHasInitializerList()) {
      System.out.printf("%s ", initializer.getIndex().toString());
    }
    if (initializer.isHasInitializerList()) {

      InitializerList initList = initializer.getInitializerList();
      List<InitializerListEntry> entries = initList.getInitializers();

      for (InitializerListEntry init : entries) {
        if (init.isDesignation()) {
          throw new ScanExc("unsupported now");
        }
        showInitializer(init.getInitializer());
      }
    } else {
      System.out.println(initializer.getAssignment().toString());
    }
  }

}
