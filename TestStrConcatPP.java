package ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import jscan.Tokenlist;
import jscan.tokenize.Token;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;

public class TestStrConcatPP {

  @Test
  public void testStringsConcat1() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                               \n");
    sb.append(" /*002*/    char *str1 = \"1\"                       \n");
    sb.append(" /*003*/      \"2\"                                  \n");
    sb.append(" /*004*/      \"3\";                                 \n");
    sb.append(" /*005*/    int a;                                   \n");
    sb.append(" /*006*/    char str2[] = u8\"1\" u8\"2\" u8\"3\";   \n");
    sb.append(" /*007*/    int b;                                   \n");
    sb.append(" /*008*/    int *str3 = L\"1\"                       \n");
    sb.append(" /*009*/      \"2\"                                  \n");
    sb.append(" /*010*/      \"3\";                                 \n");
    sb.append(" /*011*/    int c;                                   \n");
    sb.append(" /*012*/    int *str4 = L\"1\" L\"2\" L\"3\";        \n");
    sb.append(" /*013*/    int d;                                   \n");
    sb.append(" /*014*/    return 0;                                \n");
    sb.append(" /*015*/  }                                          \n");

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    for (Token t = it.next(); it.hasNext(); t = it.next()) {
      //System.out.println(t.getValue());
    }
  }

  @Test
  public void testStringsConcat2() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  \"1\" \"2\" \"3\"        ; \n");
    sb.append(" /*002*/  u8\"1\" u8\"2\" u8\"3\"  ; \n");
    sb.append(" /*003*/  L\"1\" L\"2\" L\"3\"     ; \n");
    sb.append(" /*004*/  \"1\"                      \n");
    sb.append(" /*005*/  \"2\"                      \n");
    sb.append(" /*006*/  \"3\"                      \n");

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    assertEquals(7 + 1, it.getSize()); // 7 real tokens + 1 EOF

    for (Token t = it.next(); it.hasNext(); t = it.next()) {
      assertTrue(t.getValue().equals("\"123\"") || t.getValue().equals(";"));
    }
  }

}
