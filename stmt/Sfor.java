package ast.stmt;

import ast.declarations.main.Declaration;
import ast.expr.main.CExpression;
import ast.stmt.main.CStatement;

public class Sfor {
  private Declaration decl;
  private CExpression init;
  private CExpression test;
  private CExpression step;
  private CStatement loop;

  public CExpression getInit() {
    return init;
  }

  public void setInit(CExpression init) {
    this.init = init;
  }

  public CExpression getTest() {
    return test;
  }

  public void setTest(CExpression test) {
    this.test = test;
  }

  public CExpression getStep() {
    return step;
  }

  public void setStep(CExpression step) {
    this.step = step;
  }

  public CStatement getLoop() {
    return loop;
  }

  public void setLoop(CStatement loop) {
    this.loop = loop;
  }

  public Declaration getDecl() {
    return decl;
  }

  public void setDecl(Declaration decl) {
    this.decl = decl;
  }

}
