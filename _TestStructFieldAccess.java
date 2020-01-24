package ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class _TestStructFieldAccess {

  @Test
  public void testFields_0() throws IOException {
    //@formatter:off
    StringBuilder sb0 = new StringBuilder();
    sb0.append(" /*001*/  int main() {                                                          \n");
    sb0.append(" /*002*/      struct s {                                                        \n");
    sb0.append(" /*003*/          int i;                                                        \n");
    sb0.append(" /*004*/          // error: incomplete                                          \n");
    sb0.append(" /*005*/          // struct r r;                                                \n");
    sb0.append(" /*006*/          //                                                            \n");
    sb0.append(" /*007*/          // ok: pointer to incomplete                                  \n");
    sb0.append(" /*008*/          struct r *r;                                                  \n");
    sb0.append(" /*009*/      };                                                                \n");
    sb0.append(" /*010*/      struct s str, *sp = &str;                                         \n");
    sb0.append(" /*011*/      // ok: assign pointer integer zero                                \n");
    sb0.append(" /*012*/      str.r = 0;                                                        \n");
    sb0.append(" /*013*/      sp->r = 0;                                                        \n");
    sb0.append(" /*014*/      // warn: make pointer from integer without a cast...              \n");
    sb0.append(" /*015*/      // str.r = 1;                                                     \n");
    sb0.append(" /*016*/      // sp->r = 1;                                                     \n");
    sb0.append(" /*017*/      // error: dereferencing pointer to incomplete type \'struct r\'   \n");
    sb0.append(" /*018*/      // str.r->z = 0;                                                  \n");
    sb0.append(" /*019*/      // error: dereferencing pointer to incomplete type \'struct r\'   \n");
    sb0.append(" /*020*/      // sp->r->z = 0;                                                  \n");
    sb0.append(" /*021*/      return 0;                                                         \n");
    sb0.append(" /*022*/  }                                                                     \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb1 = new StringBuilder();
    sb1.append(" /*001*/  struct struct_tag_ {   \n");
    sb1.append(" /*002*/    int a,b,c;           \n");
    sb1.append(" /*003*/    int :1;              \n");
    sb1.append(" /*004*/    int d:2;             \n");
    sb1.append(" /*005*/    int e;               \n");
    sb1.append(" /*006*/  };                     \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb2 = new StringBuilder();
    sb2.append(" /*001*/  int main() {    \n");
    sb2.append(" /*002*/    struct s {    \n");
    sb2.append(" /*003*/      char c;     \n");
    sb2.append(" /*004*/      struct {    \n");
    sb2.append(" /*005*/        int i;    \n");
    sb2.append(" /*006*/      };          \n");
    sb2.append(" /*007*/    };            \n");
    sb2.append(" /*008*/      return 0;   \n");
    sb2.append(" /*009*/  }               \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb3 = new StringBuilder();
    sb3.append(" /*001*/  struct test_anon {                                                                                \n");
    sb3.append(" /*002*/    int a;                                                                                          \n");
    sb3.append(" /*003*/    union {                                                                                         \n");
    sb3.append(" /*004*/      int b;                                                                                        \n");
    sb3.append(" /*005*/      char c;                                                                                       \n");
    sb3.append(" /*006*/      float d;                                                                                      \n");
    sb3.append(" /*007*/      double e;                                                                                     \n");
    sb3.append(" /*008*/    };                                                                                              \n");
    sb3.append(" /*009*/    struct tag1 { // has a tag, but no name, is not ANONYMOUS... is NOT change size...              \n");
    sb3.append(" /*010*/      int f;    // and members is not a members of parent struct... is just a struct-declaration.   \n");
    sb3.append(" /*011*/      int g;                                                                                        \n");
    sb3.append(" /*012*/      int a;    // is not redeclarations. is nested struct field.                                   \n");
    sb3.append(" /*013*/    };                                                                                              \n");
    sb3.append(" /*014*/    struct {   // has no tag, but has name... is a normal field...                                  \n");
    sb3.append(" /*015*/      int a; // is not redeclarations. is nested struct field.                                      \n");
    sb3.append(" /*016*/      int i;                                                                                        \n");
    sb3.append(" /*017*/      int j;                                                                                        \n");
    sb3.append(" /*018*/    } h;                                                                                            \n");
    sb3.append(" /*019*/    struct {      // has no tag and no name... is ANONYM.                                           \n");
    sb3.append(" /*020*/      // int a; // error: member of anonymous struct redeclares 'a'                                 \n");
    sb3.append(" /*021*/      int k;                                                                                        \n");
    sb3.append(" /*022*/      int l;                                                                                        \n");
    sb3.append(" /*023*/    };                                                                                              \n");
    sb3.append(" /*024*/  };                                                                                                \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb4 = new StringBuilder();
    sb4.append(" /*001*/  int main() {          \n");
    sb4.append(" /*002*/    struct s {          \n");
    sb4.append(" /*003*/      char c;           \n");
    sb4.append(" /*004*/    };                  \n");
    sb4.append(" /*005*/    struct s varname;   \n");
    sb4.append(" /*006*/    varname.c = 0;      \n");
    sb4.append(" /*007*/    return 0;           \n");
    sb4.append(" /*008*/  }                     \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb5 = new StringBuilder();
    sb5.append(" /*001*/  int main() {         \n");
    sb5.append(" /*002*/      struct {         \n");
    sb5.append(" /*003*/          char c;      \n");
    sb5.append(" /*004*/      } varname;       \n");
    sb5.append(" /*005*/      varname.c = 0;   \n");
    sb5.append(" /*006*/      return 0;        \n");
    sb5.append(" /*007*/  }                    \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb6 = new StringBuilder();
    sb6.append(" /*001*/  int main() {              \n");
    sb6.append(" /*002*/      struct str {          \n");
    sb6.append(" /*003*/          char c;           \n");
    sb6.append(" /*004*/          struct {          \n");
    sb6.append(" /*005*/              int i;        \n");
    sb6.append(" /*006*/          };                \n");
    sb6.append(" /*007*/      };                    \n");
    sb6.append(" /*008*/      struct str varname;   \n");
    sb6.append(" /*009*/      varname.c = 0;        \n");
    sb6.append(" /*010*/      varname.i = 0;        \n");
    sb6.append(" /*011*/      return 0;             \n");
    sb6.append(" /*012*/  }                         \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb7 = new StringBuilder();
    sb7.append(" /*001*/  int main() {                       \n");
    sb7.append(" /*002*/      struct str {                   \n");
    sb7.append(" /*003*/          char c;                    \n");
    sb7.append(" /*004*/          struct {                   \n");
    sb7.append(" /*005*/              int i;                 \n");
    sb7.append(" /*006*/          } nested_no_tag;           \n");
    sb7.append(" /*007*/      };                             \n");
    sb7.append(" /*008*/      struct str varname;            \n");
    sb7.append(" /*009*/      varname.c = 0;                 \n");
    sb7.append(" /*010*/      varname.nested_no_tag.i = 0;   \n");
    sb7.append(" /*011*/      return 0;                      \n");
    sb7.append(" /*012*/  }                                  \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb8 = new StringBuilder();
    sb8.append(" /*001*/  int main() {                                                       \n");
    sb8.append(" /*002*/      struct s {                                                     \n");
    sb8.append(" /*003*/          struct { char c1; };                                       \n");
    sb8.append(" /*004*/          struct { struct { char c2; }; };                           \n");
    sb8.append(" /*005*/          struct { struct { struct { char c3; }; }; };               \n");
    sb8.append(" /*006*/          struct { struct { struct { struct { char c4; }; }; }; };   \n");
    sb8.append(" /*007*/      };                                                             \n");
    sb8.append(" /*008*/      struct s varname;                                              \n");
    sb8.append(" /*009*/      varname.c1 = 0;                                                \n");
    sb8.append(" /*010*/      varname.c2 = 0;                                                \n");
    sb8.append(" /*011*/      varname.c3 = varname.c4 = 0;                                   \n");
    sb8.append(" /*012*/      return 0;                                                      \n");
    sb8.append(" /*013*/  }                                                                  \n");
    //@formatter:on

    //@formatter:off
    StringBuilder sb9 = new StringBuilder();
    sb9.append(" /*001*/  struct empty;         \n");
    sb9.append(" /*002*/  struct empty;         \n");
    sb9.append(" /*003*/  struct empty {        \n");
    sb9.append(" /*004*/      char c;           \n");
    sb9.append(" /*005*/  };                    \n");
    sb9.append(" /*006*/  int main() {          \n");
    sb9.append(" /*007*/      struct empty e;   \n");
    sb9.append(" /*008*/      e.c = 32;         \n");
    sb9.append(" /*009*/      return 0;         \n");
    sb9.append(" /*010*/  }                     \n");
    //@formatter:on

    List<StringBuilder> tests = new ArrayList<StringBuilder>();
    tests.add(sb0);
    tests.add(sb1);
    tests.add(sb2);
    tests.add(sb3);
    tests.add(sb4);
    tests.add(sb5);
    tests.add(sb6);
    tests.add(sb7);
    tests.add(sb8);
    tests.add(sb9);

    for (StringBuilder sb : tests) {
      Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
      Parse p = new Parse(it);
      TranslationUnit unit = p.parse_unit();
    }

  }

}
