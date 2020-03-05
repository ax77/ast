package ast._entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jscan.Tokenlist;
import jscan.hashed.HashStreamBufferVariant;
import jscan.hashed.Hash_all;
import jscan.hashed.Hash_stream;
import jscan.preprocess.Scan;
import jscan.tokenize.Stream;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.errors.ParseException;
import ast.parse.NullChecker;

public class PreprocessSourceForParser {

  private final PreprocessSourceForParserVariant variant;

  public PreprocessSourceForParser(PreprocessSourceForParserVariant variant) {
    NullChecker.check(variant);
    this.variant = variant;
  }

  public Tokenlist pp() throws IOException {

    Hash_all.clearAll(); // XXX: it's important to clear all hashed entries before preprocess each translation-unit.
    List<Token> input = null;

    if (variant.isFromFile()) {
      input = Hash_stream.getHashedStream(variant.getFilenameOrText(), HashStreamBufferVariant.WITH_PREDEFINED)
          .getTokenlist();
    } else {
      input = getHashedStream(variant.getFilenameOrText());
    }

    if (input == null) {
      throw new ParseException("no variant... you may select file or source...");
    }

    List<Token> clean = preprocessInput(input);
    return new Tokenlist(clean);

  }

  // if we preprocess some string as is.
  //
  private List<Token> getHashedStream(String source) throws IOException {
    int mlen = Integer.min(32, source.length());
    String fname = source.substring(0, mlen - 1);
    return new Stream("[" + fname + "]", source).getTokenlist();
  }

  private Token concatStrings(List<Token> strings) {
    if (strings.isEmpty()) {
      throw new ParseException("empty strings list");
    }

    StringBuilder sb = new StringBuilder();
    Token head = null;

    for (Token t : strings) {
      if (head == null) {
        head = t;
      }

      final String strvalue = t.getValue();
      boolean isStrOk = strvalue.startsWith("\"") && strvalue.endsWith("\"");
      if (!isStrOk) {
        throw new ParseException("error str-concat");
      }

      final String res = strvalue.substring(1, strvalue.length() - 1);
      sb.append(res);
    }

    // paranoia.
    if (head == null) {
      throw new ParseException("something wrong...");
    }

    Token r = new Token(head);
    r.setType(T.TOKEN_STRING);
    r.setValue("\"" + sb.toString() + "\"");
    return r;
  }

  private List<Token> preprocessInput(List<Token> input) throws IOException {
    List<Token> clean = new ArrayList<Token>(0);
    Scan s = new Scan(input);

    List<Token> strings = new ArrayList<Token>(0);
    for (;;) {
      Token t = s.get();
      if (t.ofType(T.TOKEN_EOF)) {

        // maybe strings are before EOF
        if (!strings.isEmpty()) {
          clean.add(concatStrings(strings));
          strings = new ArrayList<Token>(0);
        }

        // add EOF marker, and go out.
        clean.add(t);
        break;
      }

      // collect all strings, this, and all after this...
      if (t.ofType(T.TOKEN_STRING)) {
        strings.add(t);
        continue;
      } else {

        // merge string-list, add new string token, and add current token
        if (!strings.isEmpty()) {
          clean.add(concatStrings(strings));
          strings = new ArrayList<Token>(0);

          clean.add(t);
          continue;
        }

        // there is no strings, just push current token
        clean.add(t);

      }
    }
    return clean;
  }

}
