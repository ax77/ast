package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jscan.tokenize.Stream;
import jscan.tokenize.Token;

import org.junit.Test;

import ast.parse.Parse;
import ast.stmt.main.CStatement;
import ast.stmt.main.CStatementBase;
import ast.stmt.parser.ParseStatement;
import ast.unit.TranslationUnit;

public class TestSimpleStmt {

  private static Stream getHashedStream(String source) throws IOException {
    return new Stream("<utest>", source);
  }

  @Test
  public void test() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                                              \n");
    sb.append(" /*002*/    for(int i = 0; i < 10; ++i) { }                         \n");
    sb.append(" /*003*/    for( ; ; ) { break; }                                   \n");
    sb.append(" /*004*/    for(int i = 0; ; ) { break; }                           \n");
    sb.append(" /*005*/    for(int i = 0, a = i; i < 10, a; ++i, --a) { break; }   \n");
    sb.append(" /*006*/    for( ; 1; ) { break; }                                  \n");
    sb.append(" /*007*/    for( ; ; 1) { break; }                                  \n");
    sb.append(" /*008*/    do { ; } while(0);                                      \n");
    sb.append(" /*009*/    do ; while(0);                                          \n");
    sb.append(" /*010*/    while(0) { break; }                                     \n");
    sb.append(" /*011*/    while(0) break;                                         \n");
    sb.append(" /*012*/    if(0) {;} else if(1) {;} else if(2) {;} else {;}        \n");
    sb.append(" /*013*/    if(1 || 0) {} else {}                                   \n");
    sb.append(" /*014*/    int x = 1;                                              \n");
    sb.append(" /*015*/    switch(x) {                                             \n");
    sb.append(" /*016*/      default : {                                           \n");
    sb.append(" /*017*/        x = 0;                                              \n");
    sb.append(" /*018*/      }                                                     \n");
    sb.append(" /*019*/      case 0:                                               \n");
    sb.append(" /*020*/        break;                                              \n");
    sb.append(" /*021*/      case 1: {                                             \n");
    sb.append(" /*022*/        goto out1;                                          \n");
    sb.append(" /*023*/      }                                                     \n");
    sb.append(" /*024*/          case 2:                                           \n");
    sb.append(" /*025*/          case 3:                                           \n");
    sb.append(" /*026*/              break;                                        \n");
    sb.append(" /*027*/          case 11: {                                        \n");
    sb.append(" /*028*/              case 12: {                                    \n");
    sb.append(" /*029*/                  break;                                    \n");
    sb.append(" /*030*/              }                                             \n");
    sb.append(" /*031*/          }                                                 \n");
    sb.append(" /*032*/    }                                                       \n");
    sb.append(" /*033*/    out1:                                                   \n");
    sb.append(" /*034*/    for(;;) {                                               \n");
    sb.append(" /*035*/      for(;;) {                                             \n");
    sb.append(" /*036*/        for(;;) {                                           \n");
    sb.append(" /*037*/          while(1) {                                        \n");
    sb.append(" /*038*/            do {                                            \n");
    sb.append(" /*039*/              goto out;                                     \n");
    sb.append(" /*040*/            } while(1);                                     \n");
    sb.append(" /*041*/          }                                                 \n");
    sb.append(" /*042*/        }                                                   \n");
    sb.append(" /*043*/      }                                                     \n");
    sb.append(" /*044*/    }                                                       \n");
    sb.append(" /*045*/    out:                                                    \n");
    sb.append(" /*046*/    return x;                                               \n");
    sb.append(" /*047*/  }                                                         \n");

    List<Token> tokenlist = getHashedStream(sb.toString()).getTokenlist();
    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();

    assertEquals(1, unit.getExternalDeclarations().size());
    assertEquals(1, unit.countOfFunctionDefinitions(unit));
    assertEquals(0, unit.countOfDeclarations(unit));
  }

  @Test
  public void testForLoop1() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add(" for(int i = 0; i < 10; ++i) { }                         \n");

    for (String e : tests) {

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CStatement stmt = new ParseStatement(p).parse_statement();
      assertEquals(CStatementBase.SFOR, stmt.getBase());
    }
  }

  @Test
  public void testForLoop2() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add(" for( ; ; ) { break; }                                   \n");

    for (String e : tests) {

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CStatement stmt = new ParseStatement(p).parse_statement();
      assertEquals(CStatementBase.SFOR, stmt.getBase());
    }
  }

  @Test
  public void testForLoop3() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add(" for(int i = 0; ; ) { break; }                           \n");

    for (String e : tests) {

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CStatement stmt = new ParseStatement(p).parse_statement();
      assertEquals(CStatementBase.SFOR, stmt.getBase());
    }
  }

  @Test
  public void testForLoop4() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add(" for(int i = 0, a = i; i < 10, a; ++i, --a) { break; }   \n");

    for (String e : tests) {

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CStatement stmt = new ParseStatement(p).parse_statement();
      assertEquals(CStatementBase.SFOR, stmt.getBase());
    }
  }

  @Test
  public void testForLoop5() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add(" for( ; 1; ) { break; }                                  \n");

    for (String e : tests) {

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CStatement stmt = new ParseStatement(p).parse_statement();
      assertEquals(CStatementBase.SFOR, stmt.getBase());
    }
  }

  @Test
  public void testForLoop6() throws IOException {

    List<String> tests = new ArrayList<String>();
    tests.add(" for( ; ; 1) { break; }                                  \n");

    for (String e : tests) {

      List<Token> tokenlist = getHashedStream(e).getTokenlist();
      Parse p = new Parse(tokenlist);
      CStatement stmt = new ParseStatement(p).parse_statement();
      assertEquals(CStatementBase.SFOR, stmt.getBase());
    }
  }

}
