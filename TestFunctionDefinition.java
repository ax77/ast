package ast;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import jscan.tokenize.Stream;
import jscan.tokenize.Token;

import org.junit.Test;

import ast.parse.Parse;
import ast.parse.Pfunction;

public class TestFunctionDefinition {

  private static Stream getHashedStream(String source, String fname) throws IOException {
    return new Stream(fname, source);
  }

  @Test
  public void testIsNextFunctionDefinitionTrue() throws IOException {
    Map<String, String> tests = new TreeMap<String, String>();

    tests.put("int f(void) {}", "");
    tests.put("int main() {}", "");
    tests.put("int main(int argc, char **argv) {}", "");
    tests.put("int f(a,b,c) int a,b; int c; {}", "");
    tests.put("static inline const int f(char* fmt, ...) { return 0; }", "");

    for (Entry<String, String> entry : tests.entrySet()) {
      String e = entry.getKey();

      List<Token> tokenlist = getHashedStream(e, entry.getKey().toString()).getTokenlist();
      Parse p = new Parse(tokenlist);
      //assertTrue(new Pfunction(p).isNextFunctionDefinition() != null);

    }
  }

  @Test
  public void testIsNextFunctionDefinitionFalse() throws IOException {
    Map<String, String> tests = new TreeMap<String, String>();

    tests.put("int f(void), *fip(), (*pfi)(), *ap[3];", "");
    tests.put("int a;", "");
    tests.put("int a,b,c;", "");
    tests.put("int a=0,b=a;", "");
    tests.put("int main() ;", "");
    tests.put("int main(int argc, char **argv) ;", "");
    tests.put("static inline const int f(char* fmt, ...) ;", "");
    tests.put("int (*foo(const void *p))[3];", "");
    tests.put("enum reg {eax, ecx, edx};", "");
    tests.put("struct cstring { char *buffer; int nr, alloc; };", "");
    tests.put("union { int i; char c; };", "");

    for (Entry<String, String> entry : tests.entrySet()) {
      String e = entry.getKey();

      List<Token> tokenlist = getHashedStream(e, entry.getKey().toString()).getTokenlist();
      Parse p = new Parse(tokenlist);
      p.pushscope();
      assertTrue(new Pfunction(p).isNextFunctionDefinition() == null);

    }
  }

  @Test
  public void testIsNextFunctionDefinitionTrue_1() throws IOException {
    Map<String, String> tests = new TreeMap<String, String>();

    tests.put("int f(void*a, int (*fp)(int,char,char**), int x[]) {}", "");
    tests.put("struct t *fn(){}", "");
    tests.put("int ***ff(int a,int (b), int ((c))){}", "");

    for (Entry<String, String> entry : tests.entrySet()) {
      String e = entry.getKey();

      List<Token> tokenlist = getHashedStream(e, entry.getKey().toString()).getTokenlist();
      Parse p = new Parse(tokenlist);
      p.pushscope();
      assertTrue(new Pfunction(p).isNextFunctionDefinition() != null);

    }
  }

}
