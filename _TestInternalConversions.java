package ast;

import java.io.IOException;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;
import jscan.Tokenlist;

public class _TestInternalConversions {

  @Test
  public void testUsualArithmeticConvOnExpressions_0() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f1(int x[4096]) {                                                                                                \n");
//    sb.append(" /*002*/      _Static_assert(sizeof(x) == sizeof(unsigned long long)                                                            \n");
//    sb.append(" /*003*/          , \"conv.arr to ptr in fparam.\"                                                                              \n");
//    sb.append(" /*004*/      );                                                                                                                \n");
    sb.append(" /*005*/  }                                                                                                                     \n");
    sb.append(" /*006*/  void f2(int a, int b) {  }                                                                                            \n");
    sb.append(" /*007*/  void f3(void (*fp)(int, int)) { }                                                                                     \n");
    sb.append(" /*008*/  int main ()                                                                                                           \n");
    sb.append(" /*009*/  {                                                                                                                     \n");
    sb.append(" /*010*/      void (*fp)(int,int);                                                                                              \n");
    sb.append(" /*011*/      char i8 = 0;                                                                                                      \n");
    sb.append(" /*012*/      short i16 = 0;                                                                                                    \n");
    sb.append(" /*013*/      int i32 = 0;                                                                                                      \n");
    sb.append(" /*014*/      long long i64 = 0;                                                                                                \n");
    sb.append(" /*015*/      float f32 = 0;                                                                                                    \n");
    sb.append(" /*016*/      double f64 = 0;                                                                                                   \n");
    sb.append(" /*017*/      long double f128 = 0;                                                                                             \n");
    sb.append(" /*018*/      _Static_assert(    4 == sizeof( ((_Bool)0 + (_Bool)0) ), \"((_Bool)0 + (_Bool)0)\" );                             \n");
    sb.append(" /*019*/      _Static_assert(    4 == sizeof( ((char)0 + (char)0) ), \"((char)0 + (char)0)\" );                                 \n");
    sb.append(" /*020*/      _Static_assert(    4 == sizeof( ((short)0 + (char)0) ), \"((short)0 + (char)0)\" );                               \n");
    sb.append(" /*021*/      _Static_assert(    4 == sizeof( ((int)0 + (char)0) ), \"((int)0 + (char)0)\" );                                   \n");
    sb.append(" /*022*/      _Static_assert(    4 == sizeof( ((float)0 + (char)0) ), \"((float)0 + (char)0)\" );                               \n");
    sb.append(" /*023*/      _Static_assert(    8 == sizeof( ((double)0 + (char)0) ), \"((double)0 + (char)0)\" );                             \n");
    sb.append(" /*024*/      _Static_assert(    sizeof(long long) == sizeof( ((long long)0 - (char)0) ), \"((long long)0 - (char)0)\" );       \n");
    sb.append(" /*025*/      _Static_assert(   16 == sizeof( ((long double)0 + (char)0) ), \"((long double)0 + (char)0)\" );                   \n");
    sb.append(" /*026*/      _Static_assert(    4 == sizeof( ((_Bool)0 || (_Bool)0) ), \"((_Bool)0 || (_Bool)0)\" );                           \n");
    sb.append(" /*027*/      _Static_assert(    4 == sizeof( ((int)0 && (char)0) ), \"((int)0 && (char)0)\" );                                 \n");
    sb.append(" /*028*/      _Static_assert(    4 == sizeof( ((long double)0 && (long double)0) ), \"((long double)0 && (long double)0)\" );   \n");
    sb.append(" /*029*/      _Static_assert(    4 == sizeof( ((_Bool)0 | (_Bool)0) ), \"((_Bool)0 | (_Bool)0)\" );                             \n");
    sb.append(" /*030*/      _Static_assert(    4 == sizeof( ((int)0 & (char)0) ), \"((int)0 & (char)0)\" );                                   \n");
    sb.append(" /*031*/      _Static_assert(    8 == sizeof( ((long long)0 & (char)0) ), \"((long long)0 & (char)0)\" );                       \n");
    sb.append(" /*032*/      _Static_assert(    4 == sizeof( (!(_Bool)0) ), \"(!(_Bool)0)\" );                                                 \n");
    sb.append(" /*033*/      _Static_assert(    4 == sizeof( (-(int)0) ), \"(-(int)0)\" );                                                     \n");
    sb.append(" /*034*/      _Static_assert(    4 == sizeof( (+(char)0) ), \"(+(char)0)\" );                                                   \n");
    sb.append(" /*035*/      _Static_assert(    8 == sizeof( (+(long long)0) ), \"(+(long long)0)\" );                                         \n");
    sb.append(" /*036*/      _Static_assert(    1 == sizeof( (i8 += i16) ), \"(i8 += i16)\" );                                                 \n");
    sb.append(" /*037*/      _Static_assert(    1 == sizeof( (i8 = f64+f128) ), \"(i8 = f64+f128)\" );                                         \n");
    sb.append(" /*038*/      _Static_assert(    2 == sizeof( (i16 = i8+i16+i32+i64) ), \"(i16 = i8+i16+i32+i64)\" );                           \n");
    sb.append(" /*039*/      _Static_assert(    2 == sizeof( (i16 = f64) ), \"(i16 = f64)\" );                                                 \n");
    sb.append(" /*040*/      _Static_assert(    8 == sizeof( (f64 = i8) ), \"(f64 = i8)\" );                                                   \n");
    sb.append(" /*041*/      _Static_assert(    8 == sizeof( (f64 = f128) ), \"(f64 = f128)\" );                                               \n");
    sb.append(" /*042*/      _Static_assert(   16 == sizeof( (f128 = i8 || i32) ), \"(f128 = i8 || i32)\" );                                   \n");
//    sb.append(" /*043*/      _Static_assert(    1 == sizeof( (i8++) ), \"(i8++)\" );                                                           \n");
//    sb.append(" /*044*/      _Static_assert(    8 == sizeof( (++i64) ), \"(++i64)\" );                                                         \n");
//    sb.append(" /*045*/      _Static_assert(    1 == sizeof( (f2(1, 2)) ), \"(f2(1, 2))\" );                                                   \n");
//    sb.append(" /*046*/      _Static_assert(    8 == sizeof( (fp = f2) ), \"(fp = f2)\" );                                                     \n");
//    sb.append(" /*047*/      _Static_assert(    8 == sizeof( (fp = &f2) ), \"(fp = &f2)\" );                                                   \n");
//    sb.append(" /*048*/      _Static_assert(    8 == sizeof( (f2 + 1) ), \"(f2 + 1)\" );                                                       \n");
//    sb.append(" /*049*/      _Static_assert(    8 == sizeof( (fp - 1) ), \"(fp - 1)\" );                                                       \n");
//    sb.append(" /*050*/      _Static_assert(    1 == sizeof( (f3(fp)) ), \"(f3(fp))\" );                                                       \n");
    sb.append(" /*051*/      return 0;                                                                                                         \n");
    sb.append(" /*052*/  }                                                                                                                     \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();

    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
