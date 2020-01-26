package ast;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.parser.ParseBase;
import ast._typesnew.parser.ParseDecl;
import ast._typesnew.util.TypeMerger;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;
import jscan.Tokenlist;

public class __TestCTypeNewNew {

  private CDecl parseDecl(Parse p) {
    return new ParseDecl(p).parseDecl();
  }

  private CType build(CType type, CDecl decl) {
    return TypeMerger.build(type, decl);
  }

  @Ignore
  @Test
  public void test0() throws IOException {

    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append("const typedef struct x tdname;");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    p.pushscope();

    CType base = new ParseBase(p).findTypeAgain();
    CDecl decl = parseDecl(p);
    CType type = build(base, decl);

  }

  @Test
  public void test1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int f() {                              \n");
    sb.append(" /*002*/      const typedef struct s1 tdname1;   \n");
    sb.append(" /*003*/      typedef const struct s1 tdname1;   \n");
    sb.append(" /*004*/      typedef const int i32;             \n");
    sb.append(" /*005*/      typedef i32 i32;                   \n");
    sb.append(" /*006*/      i32 typedef i32;                   \n");
    sb.append(" /*007*/      int typedef const i32;             \n");
    sb.append(" /*008*/      i32 const varname1;                \n");
    sb.append(" /*009*/      static i32 varname2;               \n");
    sb.append(" /*010*/  }                                      \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    for (CSymbol sym : unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals()) {
      System.out.println(sym.toString());
    }
  }

  @Test
  public void test2() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                      \n");
    sb.append(" /*002*/      typedef int A, B(A);          \n");
    sb.append(" /*003*/      struct S {                    \n");
    sb.append(" /*004*/        int (*zerofunc)();          \n");
    sb.append(" /*005*/      } s;                          \n");
    sb.append(" /*006*/      typedef struct S* (*fty)();   \n");
    sb.append(" /*007*/      auto const fty go();          \n");
    sb.append(" /*008*/  }                                 \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    for (CSymbol sym : unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals()) {
      System.out.println(sym.toString());
    }
  }

  @Test
  public void test3() throws IOException {
    //@formatter:off
    StringBuilder sb19 = new StringBuilder();
    sb19.append(" /*001*/  int main() {                      \n");
    sb19.append(" /*002*/      typedef enum e tde, *ptde;    \n");
    sb19.append(" /*003*/      enum e;                       \n");
    sb19.append(" /*004*/      enum e { e = 1 };             \n");
    sb19.append(" /*005*/      struct s {                    \n");
    sb19.append(" /*006*/          // enum e incomplete;     \n");
    sb19.append(" /*007*/          enum e e;                 \n");
    sb19.append(" /*008*/          tde tde;                  \n");
    sb19.append(" /*009*/      };                            \n");
    sb19.append(" /*010*/      {                             \n");
    sb19.append(" /*011*/          typedef enum e tde;       \n");
    sb19.append(" /*012*/          enum e;                   \n");
    sb19.append(" /*013*/          enum e { e = 2 };         \n");
    sb19.append(" /*014*/          {                         \n");
    sb19.append(" /*015*/              int e = e;            \n");
    sb19.append(" /*016*/              ptde x =  (tde*)&e;   \n");
    sb19.append(" /*017*/          }                         \n");
    sb19.append(" /*018*/      }                             \n");
    sb19.append(" /*019*/      return e;                     \n");
    sb19.append(" /*020*/  }                                 \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb19.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    for (CSymbol sym : unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals()) {
      System.out.println(sym.toString());
    }
  }

  @Test
  public void test4() throws IOException {
    StringBuilder sb = new StringBuilder();
    //@formatter:off
    sb.append(" /*016*/  typedef struct token TOKEN, *PTOKEN; \n");
    sb.append(" /*017*/  struct token { char *sval; };        \n");
    sb.append(" /*021*/  int main()                      {    \n");
    sb.append(" /*024*/      TOKEN *token;                    \n");
    sb.append(" /*049*/  }                                    \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    for (CSymbol sym : unit.getExternalDeclarations().get(2).getFunctionDefinition().getLocals()) {
      System.out.println(sym.toString());
    }
  }

}
