package ast.stmt.main;

import jscan.sourceloc.SourceLocation;
import jscan.tokenize.Token;
import ast.parse.ILocation;
import ast.stmt.Sasm;
import ast.stmt.Scase;
import ast.stmt.Scompound;
import ast.stmt.Sdefault;
import ast.stmt.Sdowhile;
import ast.stmt.Sexpr;
import ast.stmt.Sfor;
import ast.stmt.Sgoto;
import ast.stmt.Sif;
import ast.stmt.Slabel;
import ast.stmt.Sreturn;
import ast.stmt.Sswitch;
import ast.stmt.Swhile;

public class CStatement implements ILocation {
  private final CStatementBase base;
  private final SourceLocation location;

  private Sif sif;
  private Sdowhile sdowhile;
  private Scompound scompound;
  private Sexpr sexpr;
  private Sswitch sswitch;
  private Scase scase;
  private Sfor sfor;
  private Sreturn sreturn;
  private Sgoto sgoto;
  private Slabel slabel;
  private Sdefault sdefault;
  private Sasm sasm;
  private Swhile swhile;

  public CStatement(Token from, Swhile swhile) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SWHILE;
    this.swhile = swhile;
  }

  public CStatement(Token from, Sasm asm_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SASM;
    this.sasm = asm_stmt;
  }

  public CStatement(Token from, Sdefault default_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SDEFAULT;
    this.sdefault = default_stmt;
  }

  public CStatement(Token from, Slabel label_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SLABEL;
    this.slabel = label_stmt;
  }

  public CStatement(Token from, Sgoto goto_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SGOTO;
    this.sgoto = goto_stmt;
  }

  public CStatement(Token from, Sreturn return_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SRETURN;
    this.sreturn = return_stmt;
  }

  // break, continue
  public CStatement(Token from, CStatementBase base) {
    this.location = new SourceLocation(from);
    this.base = base;
  }

  public CStatement(Token from, Sfor for_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SFOR;
    this.sfor = for_stmt;
  }

  public CStatement(Token from, Sswitch switch_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SSWITCH;
    this.sswitch = switch_stmt;
  }

  public CStatement(Token from, Scase case_stmt) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SCASE;
    this.scase = case_stmt;
  }

  public CStatement(Token from, Sexpr expressionStatement) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SEXPR;
    this.sexpr = expressionStatement;
  }

  public CStatement(Scompound stmtCompound) {
    this.location = stmtCompound.getBeginPos();
    this.base = CStatementBase.SCOMPOUND;
    this.scompound = stmtCompound;
  }

  public CStatement(Token from, Sif stmtIf) {
    this.location = new SourceLocation(from);
    this.base = CStatementBase.SIF;
    this.sif = stmtIf;
  }

  public CStatement(Token from, CStatementBase base, Sdowhile stmtWhile) {
    this.location = new SourceLocation(from);
    this.base = base;
    this.sdowhile = stmtWhile;
  }

  public Sif getSif() {
    return sif;
  }

  public void setSif(Sif sif) {
    this.sif = sif;
  }

  public Sdowhile getSdowhile() {
    return sdowhile;
  }

  public void setSdowhile(Sdowhile sdowhile) {
    this.sdowhile = sdowhile;
  }

  public Scompound getScompound() {
    return scompound;
  }

  public void setScompound(Scompound scompound) {
    this.scompound = scompound;
  }

  public Sexpr getSexpr() {
    return sexpr;
  }

  public void setSexpr(Sexpr sexpr) {
    this.sexpr = sexpr;
  }

  public Sswitch getSswitch() {
    return sswitch;
  }

  public void setSswitch(Sswitch sswitch) {
    this.sswitch = sswitch;
  }

  public Scase getScase() {
    return scase;
  }

  public void setScase(Scase scase) {
    this.scase = scase;
  }

  public Sfor getSfor() {
    return sfor;
  }

  public void setSfor(Sfor sfor) {
    this.sfor = sfor;
  }

  public Sreturn getSreturn() {
    return sreturn;
  }

  public void setSreturn(Sreturn sreturn) {
    this.sreturn = sreturn;
  }

  public Sgoto getSgoto() {
    return sgoto;
  }

  public void setSgoto(Sgoto sgoto) {
    this.sgoto = sgoto;
  }

  public Slabel getSlabel() {
    return slabel;
  }

  public void setSlabel(Slabel slabel) {
    this.slabel = slabel;
  }

  public Sdefault getSdefault() {
    return sdefault;
  }

  public void setSdefault(Sdefault sdefault) {
    this.sdefault = sdefault;
  }

  public Sasm getSasm() {
    return sasm;
  }

  public void setSasm(Sasm sasm) {
    this.sasm = sasm;
  }

  public CStatementBase getBase() {
    return base;
  }

  public Swhile getSwhile() {
    return swhile;
  }

  public void setSwhile(Swhile swhile) {
    this.swhile = swhile;
  }

  @Override
  public SourceLocation getLocation() {
    return location;
  }

  @Override
  public String getLocationToString() {
    return location.toString();
  }

  @Override
  public int getLocationLine() {
    return location.getLine();
  }

  @Override
  public int getLocationColumn() {
    return location.getColumn();
  }

  @Override
  public String getLocationFile() {
    return location.getFilename();
  }

}
