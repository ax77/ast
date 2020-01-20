package ast.stmt;

import jscan.symtab.Ident;
import ast.stmt.main.CStatement;
import ast.unit.FunctionDefinition;

public class Slabel {
  private final FunctionDefinition function;
  private final Ident label;
  private final CStatement stmt;

  public Slabel(FunctionDefinition function, Ident label, CStatement stmt) {
    this.function = function;
    this.label = label;
    this.stmt = stmt;
  }

  public FunctionDefinition getFunction() {
    return function;
  }

  public Ident getLabel() {
    return label;
  }

  public CStatement getStmt() {
    return stmt;
  }

}
