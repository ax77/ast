package ast;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.parser.ParseExternal;

public class Test_DiscoverTheFunction {

  private boolean isFunc(Parse parser) {
    return new ParseExternal(parser).isFunc();
  }

  @Test
  public void testIsNotAFunction() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add("  ; \n");
    tests.add("int a               ; \n");
    tests.add("int a, b, c         ; \n");
    tests.add("int a = 1           ; \n");
    tests.add("int a()             ; \n");
    tests.add("struct s { int x; } ; \n");
    tests.add("int a, b(), c[1]    ; \n");
    tests.add("int (a), (b)        ; \n");
    tests.add("int (a)(void)       ; \n");

    for (String s : tests) {

      Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(s, false)).pp();
      Parse p = new Parse(it);

      assertFalse(isFunc(p));
    }

  }

  @Test
  public void testIsFunction() throws IOException {

    //@formatter:off
    List<String> tests = new ArrayList<String>();
    tests.add("   struct s f() {}                  ; \n");
    tests.add(" ; int f() {}                       ; \n");
    tests.add("   int KnR(a,b,c) int a,b,c; {}       \n");
    tests.add("   int main(int argc, char **argv) {} \n");
    tests.add("  __attribute__((always_inline)) inline int prefix()  { return 1; } \n");
    //@formatter:on

    for (String s : tests) {

      Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(s, false)).pp();
      Parse p = new Parse(it);

      assertTrue(isFunc(p));
    }

  }

}
