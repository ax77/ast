package ast.unit;

import java.util.ArrayList;
import java.util.List;

import ast.stmt.main.CStatement;
import ast.symtabg.elements.CSymbol;

public class FunctionDefinition {

  private final CSymbol symbol;
  private CStatement block; //TODO:final
  private List<CSymbol> locals;
  private int localsize;

  public FunctionDefinition(CSymbol symbol) {
    this.symbol = symbol;
    this.locals = new ArrayList<CSymbol>();
  }

  public void addLocal(CSymbol e) {
    locals.add(e);
  }

  public CStatement getCompoundStatement() {
    return block;
  }

  public void setCompoundStatement(CStatement compoundStatement) {
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

  public int getLocalsize() {
    return localsize;
  }

  public void setLocalsize(int localsize) {
    this.localsize = localsize;
  }

}
