package ast;

public class UtilTestDto {
  private final boolean ignoge;
  private final String name;
  private final String source;

  public UtilTestDto(boolean ignoge, String name, StringBuilder source) {
    this.ignoge = ignoge;
    this.name = name;
    this.source = source.toString();
  }

  public boolean isIgnoge() {
    return ignoge;
  }

  public String getName() {
    return name;
  }

  public String getSource() {
    return source;
  }

}