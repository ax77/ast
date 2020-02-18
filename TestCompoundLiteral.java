package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Test;

import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.expr.parser.ParseExpression;
import ast.parse.Parse;
import ast.unit.TranslationUnit;
import jscan.tokenize.Stream;
import jscan.tokenize.Token;

public class TestCompoundLiteral {

  private static Stream getHashedStream(String source) throws IOException {
    return new Stream("<utest>", source);
  }

  @Test
  public void testCompoundLiteral_1() throws IOException {
    Map<String, String> tests = new TreeMap<String, String>();

    tests.put("(int []){2, 4, 6}", "0");

    for (Entry<String, String> entry : tests.entrySet()) {
      String e = entry.getKey();

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CExpression expr = new ParseExpression(p).e_expression();

      assertEquals(CExpressionBase.ECOMPLITERAL, expr.getBase());
    }
  }

  @Test
  public void testCompoundLiteral_2() throws IOException {
    Map<String, String> tests = new TreeMap<String, String>();

    tests.put("&(struct POINT) {1, 1}", "0");

    for (Entry<String, String> entry : tests.entrySet()) {
      String e = entry.getKey();

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      p.pushscope();

      CExpression expr = new ParseExpression(p).e_expression();

      assertEquals(CExpressionBase.EUNARY, expr.getBase());
    }
  }

  @Test
  public void testCompoundLiteral_3() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main(int argc, char **argv)                             \n");
    sb.append(" /*002*/  {                                                           \n");
    sb.append(" /*003*/    struct num   { int i32; };                                \n");
    sb.append(" /*004*/    struct ntype { struct num *num; };                        \n");
    sb.append(" /*005*/    struct nref  { struct ntype *ntype; };                    \n");
    sb.append(" /*006*/    struct n     { struct nref *nref; };                      \n");
    sb.append(" /*007*/    struct num *num = &(struct num) { .i32 = 128, };          \n");
    sb.append(" /*008*/    struct ntype *ntype = &(struct ntype) { .num = num, };    \n");
    sb.append(" /*009*/    struct nref *nref = &(struct nref) { .ntype = ntype, };   \n");
    sb.append(" /*010*/    struct n *n = &(struct n) { .nref = nref };               \n");
    sb.append(" /*011*/    int a = n->nref->ntype->num->i32;                         \n");
    sb.append(" /*012*/    int b = (*(*(*(*n).nref).ntype).num).i32;                 \n");
    sb.append(" /*013*/    return a == 128 && b == 128;                              \n");
    sb.append(" /*014*/  }                                                           \n");

    List<Token> tokenlist = getHashedStream(sb.toString()).getTokenlist();
    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();
  }

}
