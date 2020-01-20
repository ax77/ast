package ast.declarations;

import jscan.tokenize.Token;
import ast.expr.main.CExpression;

public class Designator {

  //designator
  //    : '[' constant_expression ']'
  //    | '.' IDENTIFIER
  //    ;

  private final boolean isDotDesignator;
  private final CExpression constantExpression;
  private final Token identifier;

  public Designator(CExpression constantExpression) {
    this.isDotDesignator = false;
    this.constantExpression = constantExpression;
    this.identifier = null;
  }

  public Designator(Token identifier) {
    this.isDotDesignator = true;
    this.constantExpression = null;
    this.identifier = identifier;
  }

  public boolean isDotDesignator() {
    return isDotDesignator;
  }

  public CExpression getConstantExpression() {
    return constantExpression;
  }

  public Token getIdentifier() {
    return identifier;
  }

}
