package ast;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import jscan.Tokenlist;
import jscan.tokenize.Token;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class _TestSyms {

  @Test
  public void test() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main(int argc, char **argv)                             \n");
    sb.append(" /*002*/  {                                                           \n");
    sb.append(" /*003*/    char *s = \"test-string\";                                \n");
    sb.append(" /*014*/  }                                                           \n");

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();

    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();
  }

}
