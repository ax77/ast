package ast.unit;

import ast.stmt.main.CStatement;
import ast.symtabg.elements.CSymbol;

public class FunctionDefinition {

  private final CSymbol symbol;
  private CStatement compoundStatement; //TODO:final

  public FunctionDefinition(CSymbol symbol) {
    this.symbol = symbol;
  }

  public CStatement getCompoundStatement() {
    return compoundStatement;
  }

  public void setCompoundStatement(CStatement compoundStatement) {
    this.compoundStatement = compoundStatement;
  }

  public CSymbol getSignature() {
    return symbol;
  }

}
