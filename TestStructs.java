package ast;

import java.io.IOException;
import java.util.List;

import jscan.tokenize.Stream;
import jscan.tokenize.Token;

import org.junit.Test;

import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class TestStructs {

  private static Stream getHashedStream(String source) throws IOException {
    return new Stream("", source);
  }

  private static Stream getHashedStream(String fname, String source) throws IOException {
    return new Stream(fname, source);
  }

  @Test
  public void testStructsNew1() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  struct s {                               \n");
    sb.append(" /*002*/      struct _numtag {                     \n");
    sb.append(" /*003*/          int i32;                         \n");
    sb.append(" /*004*/          long i64;                        \n");
    sb.append(" /*005*/          float f32;                       \n");
    sb.append(" /*006*/          double f64;                      \n");
    sb.append(" /*007*/          long double f128;                \n");
    sb.append(" /*008*/          void *pad;                       \n");
    sb.append(" /*009*/          enum reg { eax,ecx,edx, } reg;   \n");
    sb.append(" /*010*/      } numname;                           \n");
    sb.append(" /*011*/      struct {                             \n");
    sb.append(" /*012*/          float f;                         \n");
    sb.append(" /*013*/          double d;                        \n");
    sb.append(" /*014*/      };                                   \n");
    sb.append(" /*015*/      struct {                             \n");
    sb.append(" /*016*/          int i;                           \n");
    sb.append(" /*017*/          long l;                          \n");
    sb.append(" /*018*/          long long ll;                    \n");
    sb.append(" /*019*/      };                                   \n");
    sb.append(" /*020*/      struct {                             \n");
    sb.append(" /*021*/          char c;                          \n");
    sb.append(" /*022*/          unsigned char uc;                \n");
    sb.append(" /*023*/          struct {                         \n");
    sb.append(" /*024*/              char *str;                   \n");
    sb.append(" /*025*/              int len,n;                   \n");
    sb.append(" /*026*/              union {                      \n");
    sb.append(" /*027*/                  struct {                 \n");
    sb.append(" /*028*/                     int a;                \n");
    sb.append(" /*029*/                     int b;                \n");
    sb.append(" /*030*/                     int c,d,e,f,g;        \n");
    sb.append(" /*031*/                  } b;                     \n");
    sb.append(" /*032*/              };                           \n");
    sb.append(" /*033*/          };                               \n");
    sb.append(" /*034*/      };                                   \n");
    sb.append(" /*035*/      enum rgb {r,g,b,};                   \n");
    sb.append(" /*036*/  };                                       \n");
    sb.append(" /*037*/  int main(int argc, char **argv) {        \n");
    sb.append(" /*038*/      return 0;                            \n");
    sb.append(" /*039*/  }                                        \n");

    List<Token> tokenlist = getHashedStream(sb.toString()).getTokenlist();

    Parse p = new Parse(tokenlist);
    p.parse_unit();
  }

  @Test
  public void testStructsNew2() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  enum nothing; // incomplete                                                           \n");
    sb.append(" /*002*/  struct empty; // incomplete                                                           \n");
    sb.append(" /*003*/  typedef int arr_t[]; // incomplete                                                    \n");
    sb.append(" /*004*/  struct token {                                                                        \n");
    sb.append(" /*005*/    int t;                                                                              \n");
    sb.append(" /*006*/    struct                                                                              \n");
    sb.append(" /*007*/    {                                                                                   \n");
    sb.append(" /*008*/        struct token * n;                                                               \n");
    sb.append(" /*009*/        struct    { int flag; }   ; // anonymous                                        \n");
    sb.append(" /*010*/        struct    { int flag; } s1;                                                     \n");
    sb.append(" /*011*/        struct s2 { int flag; }   ; // declaration does not declare anything            \n");
    sb.append(" /*012*/        struct s3 { int flag; } s3;                                                     \n");
    sb.append(" /*013*/        union     { int i32; long i64; }   ; // anonymous                               \n");
    sb.append(" /*014*/        union     { int i32; long i64; } u1;                                            \n");
    sb.append(" /*015*/        union  u2 { int i32; long i64; }   ; // declaration does not declare anything   \n");
    sb.append(" /*016*/         union  u3 { int i32; long i64; } u3;                                           \n");
    sb.append(" /*017*/      enum      { eax, ecx, edx } e1; // this change size                               \n");
    sb.append(" /*018*/      enum    e2t  { r, g, b }; // these two NOT change the size                           \n");
    sb.append(" /*019*/      enum      { x, y, z }; // these two NOT change the size                           \n");
    sb.append(" /*020*/    }; // anonymous (no tag, no name)                                                   \n");
    sb.append(" /*021*/    // NO: enum nothing incomplete_enum_field;                                          \n");
    sb.append(" /*022*/    enum nothing *incomplete_enum_field_p; // pointer to incomplete is ok               \n");
    sb.append(" /*023*/    struct s *sp; // pointer to incomplete is ok                                        \n");
    sb.append(" /*024*/    arr_t *arr_p; // pointer to incomplete is ok                                        \n");
    sb.append(" /*025*/  };                                                                                    \n");
    sb.append(" /*026*/  struct empty; // incomplete                                                           \n");
    sb.append(" /*027*/  struct empty {                                                                        \n");
    sb.append(" /*028*/    char c;                                                                             \n");
    sb.append(" /*029*/  }; // complete here                                                                   \n");
    sb.append(" /*030*/  enum nothing {                                                                        \n");
    sb.append(" /*031*/    l, w, h,                                                                            \n");
    sb.append(" /*032*/  }; // complete here                                                                   \n");
    sb.append(" /*033*/  int main() {                                                                          \n");
    sb.append(" /*034*/    struct token tok;                                                                   \n");
    sb.append(" /*035*/    tok.flag = 1;                                                                       \n");
    sb.append(" /*036*/    tok.s1.flag = 1;                                                                    \n");
    sb.append(" /*037*/    tok.s3.flag = 2;                                                                    \n");
    sb.append(" /*038*/    tok.i32 = 2;                                                                        \n");
    sb.append(" /*039*/    tok.u1.i32 = 4;                                                                     \n");
    sb.append(" /*040*/    tok.u3.i64 = -1;                                                                    \n");
    sb.append(" /*041*/    int f = eax + z;                                                                    \n");
    sb.append(" /*042*/    struct empty e;                                                                     \n");
    sb.append(" /*043*/    e.c = 32;                                                                           \n");
    sb.append(" /*044*/    enum nothing xxx = w;                                                               \n");
    sb.append(" /*045*/    return sizeof(struct token);                                                        \n");
    sb.append(" /*046*/  }                                                                                     \n");

    List<Token> tokenlist = getHashedStream("struct_2", sb.toString()).getTokenlist();

    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();
  }

  @Test
  public void testStructsNew3() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(
        " /*001*/  struct test_anon {                                                                                \n");
    sb.append(
        " /*002*/    int a;                                                                                          \n");
    sb.append(
        " /*003*/    union {                                                                                         \n");
    sb.append(
        " /*004*/      int b;                                                                                        \n");
    sb.append(
        " /*005*/      char c;                                                                                       \n");
    sb.append(
        " /*006*/      float d;                                                                                      \n");
    sb.append(
        " /*007*/      double e;                                                                                     \n");
    sb.append(
        " /*008*/    };                                                                                              \n");
    sb.append(
        " /*009*/    struct tag1 { // has a tag, but no name, is not ANONYMOUS... is NOT change size...              \n");
    sb.append(
        " /*010*/      int f;    // and members is not a members of parent struct... is just a struct-declaration.   \n");
    sb.append(
        " /*011*/      int g;                                                                                        \n");
    sb.append(
        " /*012*/      int a;    // is not redeclarations. is nested struct field.                                   \n");
    sb.append(
        " /*013*/    };                                                                                              \n");
    sb.append(
        " /*014*/    struct {   // has no tag, but has name... is a normal field...                                  \n");
    sb.append(
        " /*015*/      int a; // is not redeclarations. is nested struct field.                                      \n");
    sb.append(
        " /*016*/      int i;                                                                                        \n");
    sb.append(
        " /*017*/      int j;                                                                                        \n");
    sb.append(
        " /*018*/    } h;                                                                                            \n");
    sb.append(
        " /*019*/    struct {      // has no tag and no name... is ANONYM.                                           \n");
    sb.append(
        " /*020*/      // int a; // error: member of anonymous struct redeclares 'a'                                 \n");
    sb.append(
        " /*021*/      int k;                                                                                        \n");
    sb.append(
        " /*022*/      int l;                                                                                        \n");
    sb.append(
        " /*023*/    };                                                                                              \n");
    sb.append(
        " /*024*/  };                                                                                                \n");

    List<Token> tokenlist = getHashedStream("struct_2", sb.toString()).getTokenlist();

    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();
  }

}
