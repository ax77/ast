package ast;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.decls.Initializer;
import ast.parse.Parse;
import ast.symtab.elements.CSymbol;
import ast.unit.TranslationUnit;

public class TestDeclarations {

  @Test
  public void testDeclarations1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                     \n");
    sb.append(" /*002*/      int a[2] = {1,2}, b, c, d;   \n");
    sb.append(" /*003*/      return 0;                    \n");
    sb.append(" /*004*/  }                                \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    boolean print = false;
    if (!print) {
      return;
    }

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getInitializer() != null) {
        System.out.printf("name=%s, type=%s\n", sym.getName().getName(), sym.getType());
        for (Initializer init : sym.getInitializer()) {
          System.out.println(init);
        }
        System.out.println();
      }
    }
  }

  @Test
  public void testDeclarations2() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                                                        \n");
    sb.append(" /*002*/      int a[7];                                                       \n");
    sb.append(" /*003*/      int b[1][3];                                                    \n");
    sb.append(" /*004*/      void c(void);                                                   \n");
    sb.append(" /*005*/      void (*d)(void);                                                \n");
    sb.append(" /*006*/      int *func(int a, long long b, unsigned long long c, float f);   \n");
    sb.append(" /*007*/      struct numconst {                                               \n");
    sb.append(" /*008*/          signed char i8;                                             \n");
    sb.append(" /*009*/          unsigned char u8;                                           \n");
    sb.append(" /*010*/          signed short i16;                                           \n");
    sb.append(" /*011*/          unsigned short u16;                                         \n");
    sb.append(" /*012*/          signed int i32;                                             \n");
    sb.append(" /*013*/          unsigned int u32;                                           \n");
    sb.append(" /*014*/          signed long long i64;                                       \n");
    sb.append(" /*015*/          unsigned long long u64;                                     \n");
    sb.append(" /*016*/          float f32;                                                  \n");
    sb.append(" /*017*/          double f64;                                                 \n");
    sb.append(" /*018*/          long double f128;                                           \n");
    sb.append(" /*019*/      } numconst_varname;                                             \n");
    sb.append(" /*020*/      return 0;                                                       \n");
    sb.append(" /*021*/  }                                                                   \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    boolean print = false;
    if (!print) {
      return;
    }

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
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
