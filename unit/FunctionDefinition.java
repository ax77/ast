package ast.unit;

import java.util.ArrayList;
import java.util.List;

import ast.stmt.Scompound;
import ast.symtabg.elements.CSymbol;

public class FunctionDefinition {

  private final CSymbol symbol;
  private Scompound block; //TODO:final
  private List<CSymbol> locals;

  public FunctionDefinition(CSymbol symbol) {
    this.symbol = symbol;
    this.locals = new ArrayList<CSymbol>();
  }

  public void addLocal(CSymbol e) {
    locals.add(e);
  }

  public Scompound getCompoundStatement() {
    return block;
  }

  public void setCompoundStatement(Scompound compoundStatement) {
    this.block = compoundStatement;
  }

  public CSymbol getSignature() {
    return symbol;
  }

  public CSymbol getSymbol() {
    return symbol;
  }

  public List<CSymbol> getLocals() {
    return locals;
  }

}
