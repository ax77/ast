package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ast.parse.Parse;
import ast.unit.TranslationUnit;
import jscan.tokenize.Stream;
import jscan.tokenize.Token;

public class TestSimple2 {

  private static Stream getHashedStream(String fname, String source) throws IOException {
    return new Stream(fname, source);
  }

  @Test
  public void test() throws IOException {
    //@formatter:off
    StringBuilder sb_000 = new StringBuilder();
    sb_000.append(" /*001*/  static const int unsigned GL0 = 255;                                            \n");
    sb_000.append(" /*002*/  static char A1[2] = { \'\\0\' };                                                \n");
    sb_000.append(" /*003*/  enum { coord_x, coord_y, coord_z = -1 };                                        \n");
    sb_000.append(" /*004*/  enum reg_used { eax, ecx, edx };                                                \n");
    sb_000.append(" /*005*/  int t00() {                                                                     \n");
    sb_000.append(" /*006*/    int r;                                                                        \n");
    sb_000.append(" /*007*/    r = 0;                                                                        \n");
    sb_000.append(" /*008*/    r += eax;                                                                     \n");
    sb_000.append(" /*009*/    r += ecx;                                                                     \n");
    sb_000.append(" /*010*/    r += edx;                                                                     \n");
    sb_000.append(" /*011*/    return r == 3;                                                                \n");
    sb_000.append(" /*012*/  }                                                                               \n");
    sb_000.append(" /*013*/  struct numinfo {                                                                \n");
    sb_000.append(" /*014*/    int t;                                                                        \n");
    sb_000.append(" /*015*/    union {                                                                       \n");
    sb_000.append(" /*016*/      int si;                                                                     \n");
    sb_000.append(" /*017*/      unsigned int ui;                                                            \n");
    sb_000.append(" /*018*/      long sl;                                                                    \n");
    sb_000.append(" /*019*/      unsigned long ul;                                                           \n");
    sb_000.append(" /*020*/      float f32;                                                                  \n");
    sb_000.append(" /*021*/      double f64;                                                                 \n");
    sb_000.append(" /*022*/    } u;                                                                          \n");
    sb_000.append(" /*023*/  };                                                                              \n");
    sb_000.append(" /*024*/  struct list {                                                                   \n");
    sb_000.append(" /*025*/    char c;                                                                       \n");
    sb_000.append(" /*026*/    struct list *next;                                                            \n");
    sb_000.append(" /*027*/  };                                                                              \n");
    sb_000.append(" /*028*/  struct cmdopt {                                                                 \n");
    sb_000.append(" /*029*/    const char *o;                                                                \n");
    sb_000.append(" /*030*/  }                                                                               \n");
    sb_000.append(" /*031*/  cmdoptab1[] = {                                                                 \n");
    sb_000.append(" /*032*/    { .o = \"-a\" },                                                              \n");
    sb_000.append(" /*033*/    { .o = \"-b\" },                                                              \n");
    sb_000.append(" /*034*/    { .o = \"-c\" },                                                              \n");
    sb_000.append(" /*035*/  },                                                                              \n");
    sb_000.append(" /*036*/  cmdoptab2[] = {                                                                 \n");
    sb_000.append(" /*037*/    { .o = \"-a\" },                                                              \n");
    sb_000.append(" /*038*/    { .o = \"-b\" },                                                              \n");
    sb_000.append(" /*039*/    { .o = \"-c\" },                                                              \n");
    sb_000.append(" /*040*/  };                                                                              \n");
    sb_000.append(" /*041*/  typedef int (*add_fp)(int, int);                                                \n");
    sb_000.append(" /*042*/  int f_add(int a, int b) { return a + b; }                                       \n");
    sb_000.append(" /*043*/  int t01() {                                                                     \n");
    sb_000.append(" /*044*/    add_fp fp;                                                                    \n");
    sb_000.append(" /*045*/    fp = &f_add;                                                                  \n");
    sb_000.append(" /*046*/    return fp(1, 4) == 5;                                                         \n");
    sb_000.append(" /*047*/  }                                                                               \n");
    sb_000.append(" /*048*/  int t02() {                                                                     \n");
    sb_000.append(" /*049*/    int i;                                                                        \n");
    sb_000.append(" /*050*/    int *pi;                                                                      \n");
    sb_000.append(" /*051*/    int **ppi;                                                                    \n");
    sb_000.append(" /*052*/    int ***pppi;                                                                  \n");
    sb_000.append(" /*053*/    i = 32;                                                                       \n");
    sb_000.append(" /*054*/    pi = &i;                                                                      \n");
    sb_000.append(" /*055*/    ppi = &pi;                                                                    \n");
    sb_000.append(" /*056*/    pppi = &ppi;                                                                  \n");
    sb_000.append(" /*057*/    i -= 15;                                                                      \n");
    sb_000.append(" /*058*/    (*pi) += 5;                                                                   \n");
    sb_000.append(" /*059*/    *(*ppi) += 5;                                                                 \n");
    sb_000.append(" /*060*/    *(*(*pppi)) += 5;                                                             \n");
    sb_000.append(" /*061*/    return i == 32;                                                               \n");
    sb_000.append(" /*062*/  }                                                                               \n");
    sb_000.append(" /*063*/  int t03() {                                                                     \n");
    sb_000.append(" /*064*/    int signed a,b,c,d,e=0;                                                       \n");
    sb_000.append(" /*065*/    return (e + (a=b=c=d=16)) == 16;                                              \n");
    sb_000.append(" /*066*/  }                                                                               \n");
    sb_000.append(" /*067*/  int t04() {                                                                     \n");
    sb_000.append(" /*068*/    if( 1 << 7 ) {                                                                \n");
    sb_000.append(" /*069*/      goto out1;                                                                  \n");
    sb_000.append(" /*070*/    }                                                                             \n");
    sb_000.append(" /*071*/    else {                                                                        \n");
    sb_000.append(" /*072*/      goto out2;                                                                  \n");
    sb_000.append(" /*073*/    }                                                                             \n");
    sb_000.append(" /*074*/   out1:                                                                          \n");
    sb_000.append(" /*075*/    goto outbranch;                                                               \n");
    sb_000.append(" /*076*/      for(;;) { goto out2; }                                                      \n");
    sb_000.append(" /*077*/      while(1) { goto out2; }                                                     \n");
    sb_000.append(" /*078*/      do { goto out2; } while(1);                                                 \n");
    sb_000.append(" /*079*/    outbranch:                                                                    \n");
    sb_000.append(" /*080*/    return 1;                                                                     \n");
    sb_000.append(" /*081*/   out2:                                                                          \n");
    sb_000.append(" /*082*/    return 0;                                                                     \n");
    sb_000.append(" /*083*/  }                                                                               \n");
    sb_000.append(" /*084*/  int t05() {                                                                     \n");
    sb_000.append(" /*085*/    int a,b,c=0;                                                                  \n");
    sb_000.append(" /*086*/    {                                                                             \n");
    sb_000.append(" /*087*/      int a = 1<<1;                                                               \n");
    sb_000.append(" /*088*/      int b = 1<<2;                                                               \n");
    sb_000.append(" /*089*/      int c = 1<<3;                                                               \n");
    sb_000.append(" /*090*/      {                                                                           \n");
    sb_000.append(" /*091*/        int a = 1<<4;                                                             \n");
    sb_000.append(" /*092*/        int b = 1<<5;                                                             \n");
    sb_000.append(" /*093*/        int c = 1<<6;                                                             \n");
    sb_000.append(" /*094*/      }                                                                           \n");
    sb_000.append(" /*095*/    }                                                                             \n");
    sb_000.append(" /*096*/    return c == 0;                                                                \n");
    sb_000.append(" /*097*/  }                                                                               \n");
    sb_000.append(" /*098*/  int t06() {                                                                     \n");
    sb_000.append(" /*099*/    int c[2][3] = {{1, 3, 0}, {-1, 5, 9},};                                       \n");
    sb_000.append(" /*100*/    return (c[0][0]+c[1][0]==0) && c[1][2]==9;                                    \n");
    sb_000.append(" /*101*/  }                                                                               \n");
    sb_000.append(" /*102*/  int t07() {                                                                     \n");
    sb_000.append(" /*103*/    int num[3][4] = {                                                             \n");
    sb_000.append(" /*104*/      {1, 2,  3,  4},                                                             \n");
    sb_000.append(" /*105*/      {5, 6,  7,  8},                                                             \n");
    sb_000.append(" /*106*/      {9, 10, 11, 12}                                                             \n");
    sb_000.append(" /*107*/    };                                                                            \n");
    sb_000.append(" /*108*/    int *ptr = &num[1][2];                                                        \n");
    sb_000.append(" /*109*/    return *ptr == 7;                                                             \n");
    sb_000.append(" /*110*/  }                                                                               \n");
    sb_000.append(" /*111*/  int t08() {                                                                     \n");
    sb_000.append(" /*112*/    return ( \"\\xe\\xf\"[0] == 14 ) && ( \"\\xe\\xf\"[1] == 15 );                \n");
    sb_000.append(" /*113*/  }                                                                               \n");
    sb_000.append(" /*114*/  int t09() {                                                                     \n");
    sb_000.append(" /*115*/    int c[8] = {1,2,3,4,5,};                                                      \n");
    sb_000.append(" /*116*/    return (                                                                      \n");
    sb_000.append(" /*117*/      (short unsigned const)                                                      \n");
    sb_000.append(" /*118*/        (1[c] + c[1] + 1) + (L\"\\xa\"[0] - L\"\\012\"[0] - u\'\\0\') == 5U       \n");
    sb_000.append(" /*119*/    );                                                                            \n");
    sb_000.append(" /*120*/  }                                                                               \n");
    sb_000.append(" /*121*/  int main() {                                                                    \n");
    sb_000.append(" /*122*/    return 0;                                                                     \n");
    sb_000.append(" /*123*/  }                                                                               \n");

    List<UtilTestDto> tests = new ArrayList<UtilTestDto>();
    tests.add(new UtilTestDto(false, "000", sb_000));

    int others = 0;
    for (UtilTestDto dto : tests) {

      if (dto.isIgnoge()) {
        continue;
      }

      List<Token> tokenlist = getHashedStream(dto.getName(), dto.getSource()).getTokenlist();
      Parse p = new Parse(tokenlist);
      TranslationUnit unit = p.parse_unit();
      others++;
    }

    assertEquals(tests.size(),  others);

  }

}
