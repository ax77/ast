package ast.stmt;

import ast.stmt.main.CStatement;
import ast.stmt.main.TempLabel;

public class Sdefault {

  private final String labelout;
  private final Sswitch parent;
  private final CStatement stmt;

  public Sdefault(Sswitch parent, CStatement stmt) {
    this.labelout = TempLabel.getdefault();
    this.parent = parent;
    this.stmt = stmt;
  }

  public CStatement getStmt() {
    return stmt;
  }

  public Sswitch getParent() {
    return parent;
  }

  public String getLabelout() {
    return labelout;
  }

}
