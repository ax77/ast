package ast._entry;

public class PreprocessSourceForParserVariant {
  private final String filenameOrText;
  private final boolean isFromFile;

  public PreprocessSourceForParserVariant(String what, boolean isFromFile) {
    this.filenameOrText = what;
    this.isFromFile = isFromFile;
  }

  public String getFilenameOrText() {
    return filenameOrText;
  }

  public boolean isFromFile() {
    return isFromFile;
  }

}
