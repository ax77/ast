package ast.stmt;

import ast.unit.FunctionDefinition;
import jscan.symtab.Ident;

public class Sgoto {
  private final FunctionDefinition function;
  private final Ident label;

  public Sgoto(FunctionDefinition function, Ident label) {
    super();
    this.function = function;
    this.label = label;
  }

  public FunctionDefinition getFunction() {
    return function;
  }

  public Ident getLabel() {
    return label;
  }

}
