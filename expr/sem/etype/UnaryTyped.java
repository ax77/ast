package ast.expr.sem.etype;

import static ast._typesnew.CType.FUNC_DESIGNATOR_TODO_STUB;
import static ast._typesnew.CType.TYPE_INT;
import static ast._typesnew.CType.VOID_TYPE;
import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_EXCLAMATION;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_PLUS;
import static jscan.tokenize.T.T_TILDE;
import static jscan.tokenize.T.T_TIMES;
import jscan.tokenize.Token;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast.expr.main.CExpression;
import ast.parse.NullChecker;
import ast.parse.ParseException;

public abstract class UnaryTyped {
  public static CExpression sUnary(Token operator, CExpression operand) {

    if (operand.getResultType() == null) {
      System.out.println(operand.toString());
    }

    NullChecker.check(operator, operand, operand.getResultType());

    // T_EXCLAMATION
    //
    if (operator.ofType(T_EXCLAMATION)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isScalar()) {
        resRT = TYPE_INT;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_UMINUS
    //
    else if (operator.ofType(T_MINUS)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isArithmetic()) {
        resRT = lhsRT;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_UPLUS
    //
    else if (operator.ofType(T_PLUS)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isArithmetic()) {
        resRT = lhsRT;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_UTILDE
    //
    else if (operator.ofType(T_TILDE)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isInteger()) {
        resRT = lhsRT;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_UADDRESS
    //
    else if (operator.ofType(T_AND)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isAnObjectExceptBitField()) {
        resRT = genPtrTo(lhsRT);
      } else if (lhsRT.isIncomplete()) {
        resRT = genPtrTo(lhsRT);
      } else if (lhsRT.isFunction()) {
        resRT = genPtrTo(lhsRT);
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_UTIMES
    //
    else if (operator.ofType(T_TIMES)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isPointerToObject()) {
        resRT = lhsRT;
      } else if (lhsRT.isPointerToFunction()) {
        resRT = FUNC_DESIGNATOR_TODO_STUB;
      } else if (lhsRT.isPointerToVoid()) {
        resRT = VOID_TYPE;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    else {
      errorUnknownUnaryOperator(operator);
    }

    errorUnknownUnaryOperator(operator);
    return null;
  }

  private static void checkResultType(CType resRT, Token operator, CExpression lhs) {
    if (resRT == null) {
      throw new ParseException("checkResultType: " + operator.toString() + " " + lhs.toString());
    }

  }

  private static void errorUnaryExpr(String string, Token operator, CExpression lhs) {
    throw new ParseException("errorUnaryExpr: " + operator.toString() + " " + lhs.toString());

  }

  private static CType genPtrTo(CType lhsRT) {
    return new CType(lhsRT, StorageKind.ST_NONE);
  }

  private static void errorUnknownUnaryOperator(Token operator) {
    throw new ParseException("errorUnknownUnaryOperator: " + operator.toString());

  }

}
