package ast;

import static ast._entry.ParseConf.APPLY_STR_CONCAT;
import static ast._entry.ParseConf.PREPROCESS_FILE_INPUT;
import static ast._entry.ParseConf.PREPROCESS_STRING_INPUT;

import java.io.IOException;

import jscan.Tokenlist;
import ast._entry.ParseConf;
import ast.parse.NullChecker;

public class PreprocessSourceForParser {

  private final PreprocessSourceForParserVariant variant;

  public PreprocessSourceForParser(PreprocessSourceForParserVariant variant) {
    NullChecker.check(variant);
    this.variant = variant;
  }

  public Tokenlist pp() throws IOException {

    if (variant.isFromFile()) {
      ParseConf conf = new ParseConf(PREPROCESS_FILE_INPUT | APPLY_STR_CONCAT, variant.getFilenameOrText());
      return conf.preprocess();
    }

    ParseConf conf = new ParseConf(PREPROCESS_STRING_INPUT | APPLY_STR_CONCAT, new StringBuilder(
        variant.getFilenameOrText()));
    return conf.preprocess();

  }

}
