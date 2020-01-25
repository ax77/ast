package ast.expr.sem.etype;

import static ast._typesnew.CTypeImpl.FUNC_DESIGNATOR_TODO_STUB;
import static ast._typesnew.CTypeImpl.TYPE_INT;
import static ast._typesnew.CTypeImpl.TYPE_VOID;
import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_EXCLAMATION;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_PLUS;
import static jscan.tokenize.T.T_TILDE;
import static jscan.tokenize.T.T_TIMES;

import ast._typesnew.CPointerType;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.parse.NullChecker;
import jscan.tokenize.Token;

public abstract class UnaryTyped {
  public static CExpression sUnary(Token operator, CExpression operand) {

    if (operand.getResultType() == null) {
      System.out.println(operand.toString());
    }

    NullChecker.check(operator, operand, operand.getResultType());

    // !
    //
    if (operator.ofType(T_EXCLAMATION)) {
      CType lhsRT = CExpressionBuilderHelper.ipromote(operand.getResultType());
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

    // [- + ~]
    //
    else if (operator.ofType(T_MINUS) || operator.ofType(T_PLUS) || operator.ofType(T_TILDE)) {
      CType lhsRT = CExpressionBuilderHelper.ipromote(operand.getResultType());
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

    // address-of
    //
    else if (operator.ofType(T_AND)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isAnObjectExceptBitField()) {
        resRT = genPtrTo(lhsRT);
      }

      else if (lhsRT.isIncomplete()) {
        resRT = genPtrTo(lhsRT);
      }

      else if (lhsRT.isFunction()) {
        resRT = genPtrTo(lhsRT);
      }

      else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      checkResultType(resRT, operator, operand);
      CExpression resultExpression = new CExpression(operator, operand);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // dereference
    //
    else if (operator.ofType(T_TIMES)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;
      if (lhsRT.isPointerToObject()) {
        resRT = lhsRT.getTpPointer().getPointerTo(); // XXX:
      }

      else if (lhsRT.isPointerToFunction()) {
        resRT = FUNC_DESIGNATOR_TODO_STUB;
      }

      else if (lhsRT.isPointerToVoid()) {
        resRT = TYPE_VOID;
      }

      else {
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
    return new CType(new CPointerType(lhsRT, false), StorageKind.ST_NONE);
  }

  private static void errorUnknownUnaryOperator(Token operator) {
    throw new ParseException("errorUnknownUnaryOperator: " + operator.toString());

  }

}
