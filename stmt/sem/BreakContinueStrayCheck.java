package ast.stmt.sem;

import jscan.tokenize.Token;
import ast.parse.Parse;
import ast.stmt.main.CStatement;
import ast.stmt.main.CStatementBase;

public class BreakContinueStrayCheck {
  private final Parse parser;

  public BreakContinueStrayCheck(Parse parser) {
    this.parser = parser;
  }

  private boolean canBreak() {
    boolean hasSWitch = !parser.getSwitches().isEmpty();
    boolean hasLoop = !parser.getLoops().isEmpty();
    return hasSWitch || hasLoop;
  }

  private boolean canContinue() {
    return !parser.getLoops().isEmpty();
  }

  private void checkBreak() {
    if (!canBreak()) {
      parser.perror("stray `break`");
    }
  }

  private void checkContinue() {
    if (!canContinue()) {
      parser.perror("stray `continue`");
    }
  }

  public CStatement breakStatement() {
    checkBreak();

    Token from = parser.expectIdentifier();
    parser.semicolon();
    return new CStatement(from, CStatementBase.SBREAK);
  }

  public CStatement continueStatement() {
    checkContinue();

    Token from = parser.expectIdentifier();
    parser.semicolon();
    return new CStatement(from, CStatementBase.SCONTINUE);
  }

}
