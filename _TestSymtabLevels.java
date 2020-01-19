package ast;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;
import jscan.Tokenlist;

public class _TestSymtabLevels {

  @Ignore
  @Test
  public void testSymbolsEasy() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int global_var;   int f(void){}              \n");
    sb.append(" /*002*/  int main(int argc, char **argv) {            \n");
    sb.append(" /*003*/    int a;                                     \n");
    sb.append(" /*004*/    if(a) { ++argc;}                           \n");
    sb.append(" /*005*/    {      { long a; }                         \n");
    sb.append(" /*006*/      int a;                                   \n");
    sb.append(" /*007*/      if(a) {}                                 \n");
    sb.append(" /*008*/      for(int a=0; a<10; ++a) { if(a){} }      \n");
    sb.append(" /*009*/    }                                          \n");
    sb.append(" /*010*/    return a+global_var + __func__[0];         \n");
    sb.append(" /*011*/  }                                            \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();

    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
