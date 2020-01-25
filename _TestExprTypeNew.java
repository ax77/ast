package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.expr.main.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.Parse;
import jscan.Tokenlist;

public class _TestExprTypeNew {

  @Test
  public void testSizeofEvaluation() throws IOException {
    Map<String, Integer> s = new HashMap<String, Integer>();
    //@formatter:off
    s.put( " sizeof(_Bool)                   \n",  1);
    s.put( " sizeof((_Bool)1)                \n",  1);
    s.put( " sizeof((_Bool).2f)              \n",  1);
    s.put( " sizeof(_Bool*)                  \n",  8);
    s.put( " sizeof(char)                    \n",  1);
    s.put( " sizeof(int)                     \n",  4);
    s.put( " sizeof(void)                    \n",  1);
    s.put( " sizeof(1)                       \n",  4);
    s.put( " sizeof(1ULL)                    \n",  8);
    s.put( " sizeof(1 + 2)                   \n",  4);
    s.put( " sizeof(1 + 2ULL)                \n",  8);
    s.put( " sizeof(long double)             \n", 16);
    s.put( " sizeof(long double*)            \n",  8);
    s.put( " sizeof((char)1ULL)              \n",  1);
    s.put( " sizeof((int)1ULL)               \n",  4);
    s.put( " sizeof((long long)1ULL)         \n",  8);
    s.put( " sizeof(1.0f + 2.14)             \n",  8);
    s.put( " sizeof(1.0f + 2.14f)            \n",  4);
    s.put( " sizeof((void*)0)                \n",  8);
    s.put( " sizeof((int*)0)                 \n",  8);
    s.put( " sizeof((int)+1)                 \n",  4);
    s.put( " sizeof(int(*)())                \n",  8);
    s.put( " sizeof 1 + 3                    \n",  7);
    s.put( " sizeof 1 + 3ULL                 \n",  7);
    s.put( " sizeof 1ULL                     \n",  8);
    s.put( " sizeof +1                       \n",  4);
    s.put( " sizeof 1024                     \n",  4);
    s.put( " sizeof 1024 + 1ULL              \n",  5);
    s.put( " sizeof((1+2, 2+3))              \n",  4);
    s.put(" sizeof(((char)'1'))              \n",  1);
    s.put(" sizeof(('1', 2))                 \n",  4);
    s.put(" sizeof(('1', 2, 3ULL))           \n",  8);
    s.put(" sizeof(('1', 2, 3ULL, 4.f))      \n",  4);
    s.put(" sizeof(('1', 2, 3ULL, 4.f, 5.))  \n",  8);
    s.put("sizeof( ((_Bool)0 + (_Bool)0) )   \n",  4);
    //@formatter:on

    int x = 0;
    for (Entry<String, Integer> entry : s.entrySet()) {

      String source = entry.getKey();
      Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(source, false)).pp();

      Parse p = new Parse(it);
      CExpression expr = new ParseExpression(p).e_expression();

      long ce = new ConstexprEval(p).ce(expr);

      if (ce != entry.getValue()) {
        System.out.println(expr.toString());
      }

      assertEquals(entry.getValue().intValue(), ce);

      //System.out.println("    unsigned long x_" + String.format("%02d", x++) + " = " +  source.trim() + ";");
    }
  }

  @Test
  public void testConstexprEval_0() throws IOException {
    Map<String, Integer> s = new HashMap<String, Integer>();
    //@formatter:off
    s.put("1 + 2 * 3             ",     7);
    s.put("1 ? 2 : 3 ? 4 : 5     ",     2);
    s.put("1 ? 2 : 1 / 0         ",     2);
    s.put("0 && 1 / 0            ",     0);
    s.put("1 - 1 && 1 / 0        ",     0);
    s.put("1 || 1 / 0            ",     1);
    s.put("-1 || 1 / 0           ",     1);
    s.put("1+2*3-1+2*1024-2+1/2  ",  2052);
    s.put("1 & 2                 ",     0);
    s.put("1 | 2                 ",     3);
    s.put("1 ^ 2                 ",     3);
    s.put("1 << 1                ",     2);
    s.put("1 << 2                ",     4);
    s.put("1 << 3                ",     8);
    s.put("0 && 0 ? 1 : 2        ",     2);
    s.put("(1,2,3,4,5,6,7,8,9,0) ",     0);
    s.put("(1+2, 2+3, 3+4)       ",     7);
    //@formatter:on

    for (Entry<String, Integer> entry : s.entrySet()) {

      String source = entry.getKey();
      Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(source, false)).pp();

      Parse p = new Parse(it);
      CExpression expr = new ParseExpression(p).e_expression();

      long ce = new ConstexprEval(p).ce(expr);
      assertEquals(entry.getValue().intValue(), ce);
    }
  }

}
