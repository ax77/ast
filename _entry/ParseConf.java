package ast._entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ast.errors.ParseException;
import jscan.Tokenlist;
import jscan.fio.FileWrapper;
import jscan.hashed.Hash_all;
import jscan.hashed.Hash_stream;
import jscan.preprocess.Scan;
import jscan.sourceloc.SourceLocation;
import jscan.tokenize.Stream;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class ParseConf {
  //@formatter:off
  public static final int APPLY_STR_CONCAT             = 1<<0;
  public static final int PREPROCESS_STRING_INPUT      = 1<<1;
  public static final int PREPROCESS_FILE_INPUT        = 1<<2;
  public static final int PREPEND_PREDEFINED_BUFFER    = 1<<3;
  //@formatter:on

  public static final String UNIT_TEST_FILENAME = "<unit-test>";

  private final int flag;
  private final String filename;
  private final String source;

  public ParseConf(int flag, String filename, String source) {
    this.flag = flag;
    this.filename = filename;
    this.source = source;

    // XXX: file or string
    checkFlags();

    // XXX: it's important to clear all hashed entries before preprocess each translation-unit.
    Hash_all.clearAll();
  }

  public boolean isFileInput() {
    return isFlag(PREPROCESS_FILE_INPUT);
  }

  public boolean isStringInput() {
    return isFlag(PREPROCESS_STRING_INPUT);
  }

  public Tokenlist preprocess() throws IOException {

    if (isFlag(PREPROCESS_FILE_INPUT)) {
      return ppFile();
    }

    return ppString();
  }

  private void checkFlags() {
    final boolean isfile = isFlag(PREPROCESS_FILE_INPUT);
    final boolean isstring = isFlag(PREPROCESS_STRING_INPUT);
    boolean ok = isfile || isstring;
    if (!ok) {
      throw new ParseException("need specify string|file input");
    }
    if (isfile && isstring) {
      throw new ParseException("need specify string|file input");
    }
  }

  private Tokenlist ppString() throws IOException {
    List<Token> input = getInput();
    return new Tokenlist(input);
  }

  private Tokenlist ppFile() throws IOException {
    FileWrapper fileWrapper = new FileWrapper(filename);
    fileWrapper.assertIsExists();
    fileWrapper.assertIsFile();

    List<Token> input = getInput();
    return new Tokenlist(input);
  }

  private List<Token> getInput() throws IOException {

    if (isFlag(APPLY_STR_CONCAT)) {
      return new StrConcat().preprocessInput(getInputInternal());
    }

    return preprocessNoStrConcat(getInputInternal());
  }

  private List<Token> preprocessNoStrConcat(List<Token> input) throws IOException {
    List<Token> clean = new ArrayList<Token>(0);
    Scan s = new Scan(input);
    for (;;) {
      Token t = s.get();
      clean.add(t);
      if (t.ofType(T.TOKEN_EOF)) {
        break;
      }
    }
    return clean;
  }

  /// predefined? + (string | file)
  private List<Token> getInputInternal() throws IOException {
    List<Token> result = new ArrayList<Token>();

    if (isFlag(PREPEND_PREDEFINED_BUFFER)) {
      result.addAll(tokenizePredefined());
    }
    if (isFlag(PREPROCESS_STRING_INPUT)) {
      List<Token> stringInputList = new Stream(filename, source).getTokenlist();
      result.addAll(stringInputList);
    }
    if (isFlag(PREPROCESS_FILE_INPUT)) {
      List<Token> fileInputList = Hash_stream.getHashedStream(filename).getTokenlist();
      result.addAll(fileInputList);
    }

    return result;
  }

  private boolean isFlag(int f) {
    return (flag & f) == f;
  }

  private List<Token> tokenizePredefined() {

    final String builtinFname = "<built-in>";
    final SourceLocation location = new SourceLocation(builtinFname, 0, 0);

    List<Token> predefined = new Stream(builtinFname, ParseSettings.getPredefinedBuffer()).getTokenlist();
    List<Token> clean = new ArrayList<Token>();

    // TODO: move this logic to Stream.class?
    // special stream-markers, and EOF doesn't need here.
    // but location new and the same for all stream
    for (Token t : predefined) {
      if (t.typeIsSpecialStreamMarks()) {
        continue;
      }
      t.setLocation(location);
      clean.add(t);
    }

    return clean;
  }

}
