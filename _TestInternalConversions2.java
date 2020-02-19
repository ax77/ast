package ast;

import java.io.IOException;

import jscan.Tokenlist;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class _TestInternalConversions2 {

  @Test
  public void testUsualArithmeticConvOnExpressions_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                                                                         \n");
    sb.append(" /*002*/      int a[7];                                                                        \n");
    sb.append(" /*003*/      int b[1][3];                                                                     \n");
    sb.append(" /*004*/      void c(void);                                                                    \n");
    sb.append(" /*005*/      void (*d)(void);                                                                 \n");
    sb.append(" /*006*/      //                                                                               \n");
    sb.append(" /*007*/      _Static_assert( _Generic(a  , int*            : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*008*/      _Static_assert( _Generic(&a , int(*)[7]       : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*009*/      _Static_assert( _Generic(*a , int             : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*010*/      //                                                                               \n");
    sb.append(" /*011*/      _Static_assert( _Generic(a+1     , int*       : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*012*/      _Static_assert( _Generic(*(a+1)  , int        : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*013*/      _Static_assert( _Generic(&*(a+1) , int*       : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*014*/      //                                                                               \n");
    sb.append(" /*015*/      _Static_assert( _Generic(b  , int(*)[3]       : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*016*/      _Static_assert( _Generic(&b , int(*)[1][3]    : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*017*/      _Static_assert( _Generic(*b , int*            : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*018*/      //                                                                               \n");
    sb.append(" /*019*/      _Static_assert( _Generic(c  , void (*)(void)  : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*020*/      _Static_assert( _Generic(&c , void (*)(void)  : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*021*/      _Static_assert( _Generic(*c , void (*)(void)  : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*022*/      //                                                                               \n");
    sb.append(" /*023*/      _Static_assert( _Generic(d  , void (*)(void)  : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*024*/      _Static_assert( _Generic(&d , void (**)(void) : 1, default: 2) == 1, \"NO\" );   \n");
//    sb.append(" /*025*/      _Static_assert( _Generic(*d , void (*)(void)  : 1, default: 2) == 1, \"NO\" );   \n");
    sb.append(" /*026*/      //                                                                               \n");
    sb.append(" /*027*/      return 0;                                                                        \n");
    sb.append(" /*028*/  }                                                                                    \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();

    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
