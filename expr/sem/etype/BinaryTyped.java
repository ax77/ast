package ast.expr.sem.etype;

import static ast._typesnew.CTypeImpl.TYPE_INT;
import static ast._typesnew.CTypeImpl.TYPE_LONG_LONG;
import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_AND_AND;
import static jscan.tokenize.T.T_ASSIGN;
import static jscan.tokenize.T.T_DIVIDE;
import static jscan.tokenize.T.T_EQ;
import static jscan.tokenize.T.T_GE;
import static jscan.tokenize.T.T_GT;
import static jscan.tokenize.T.T_LE;
import static jscan.tokenize.T.T_LSHIFT;
import static jscan.tokenize.T.T_LT;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_NE;
import static jscan.tokenize.T.T_OR;
import static jscan.tokenize.T.T_OR_OR;
import static jscan.tokenize.T.T_PERCENT;
import static jscan.tokenize.T.T_PLUS;
import static jscan.tokenize.T.T_RSHIFT;
import static jscan.tokenize.T.T_TIMES;
import static jscan.tokenize.T.T_XOR;

import ast._typesnew.CType;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.NullChecker;
import jscan.tokenize.Token;

public abstract class BinaryTyped {

  public static CExpression sBinary(Token operator, CExpression lhs, CExpression rhs) {

    lhs = ImplicitCast.genPointer(lhs);
    rhs = ImplicitCast.genPointer(rhs);

    CType Ltype = lhs.getResultType();
    CType Rtype = rhs.getResultType();
    CType tpOfResult = null;

    // T_PLUS
    //
    if (operator.ofType(T_PLUS)) {
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        tpOfResult = bb.getBalancedResult();
      } else if (Ltype.isPointerToObject() && Rtype.isInteger()) {
        tpOfResult = Ltype;
      } else if (Ltype.isInteger() && Rtype.isPointerToObject()) {
        tpOfResult = Rtype;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // T_MINUS
    //
    else if (operator.ofType(T_MINUS)) {
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        tpOfResult = bb.getBalancedResult();
      } else if (Ltype.isPointerToObject() && Rtype.isInteger()) {
        tpOfResult = Ltype;
      } else if (Ltype.isPointerToObject() && Rtype.isPointerToCompat(Ltype)) {
        tpOfResult = TYPE_LONG_LONG;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // * /
    //
    else if (operator.ofType(T_DIVIDE) || operator.ofType(T_TIMES)) {
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        tpOfResult = bb.getBalancedResult();
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // % & | ^
    //
    else if (operator.ofType(T_PERCENT) || operator.ofType(T_AND) || operator.ofType(T_OR) || operator.ofType(T_XOR)) {
      if (Ltype.isInteger() && Rtype.isInteger()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        tpOfResult = bb.getBalancedResult();
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // && ||
    //
    else if (operator.ofType(T_AND_AND) || operator.ofType(T_OR_OR)) {
      if (Ltype.isScalar() && Rtype.isScalar()) {
        tpOfResult = TYPE_INT;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // <  <=  >  >=
    //
    else if (operator.ofType(T_LT) || operator.ofType(T_LE) || operator.ofType(T_GT) || operator.ofType(T_GE)) {
      tpOfResult = TYPE_INT;
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
      } else if (Ltype.isPointer() && Rtype.isPointerToCompat(Ltype)) {
      } else {
        errorExpr("Equality binary expression error: ", operator, lhs, rhs);
      }
    }

    // ==  !=
    //
    else if (operator.ofType(T_EQ) || operator.ofType(T_NE)) {
      tpOfResult = TYPE_INT;
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
      } else if (Ltype.isPointer() && rhs.isIntegerZero()) {
      } else if (lhs.isIntegerZero() && Rtype.isPointer()) {
      } else if (Ltype.isPointer() && Rtype.isPointerToCompat(Ltype)) {
      } else if (Ltype.isPointerToVoid() && Rtype.isPointerToObject()) {
      } else if (Ltype.isPointerToVoid() && Rtype.isPointerToIncomplete()) {
      } else if (Ltype.isPointerToObject() && Rtype.isPointerToVoid()) {
      } else if (Ltype.isPointerToIncomplete() && Rtype.isPointerToVoid()) {
      } else {
        errorExpr("Equality binary expression error: ", operator, lhs, rhs);
      }
    }

    // T_LSHIFT
    //
    else if (operator.ofType(T_LSHIFT)) {

      if (Ltype.isInteger() && Rtype.isInteger()) {
        tpOfResult = Ltype;
      } else {
        errorExpr("Shift binary expression error: ", operator, lhs, rhs);
      }

    }

    // T_RSHIFT
    //
    else if (operator.ofType(T_RSHIFT)) {

      if (Ltype.isInteger() && Rtype.isInteger()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        tpOfResult = bb.getBalancedResult();
      } else {
        errorExpr("Shift binary expression error: ", operator, lhs, rhs);
      }

    }

    else {
      errorUnknownBinaryOperator(operator);
    }

    checkResultType(tpOfResult, operator, lhs, rhs);
    CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);

    resultExpression.setResultType(tpOfResult);
    return resultExpression;
  }

  public static CExpression sAssign(Token operator, CExpression lhs, CExpression rhs) {

    if (lhs.getResultType() == null || rhs.getResultType() == null) {
      System.out.println();
    }

    NullChecker.check(operator, lhs, rhs, lhs.getResultType(), rhs.getResultType());

    // TODO: modLvalue and etc...
    // TODO: cast
    // TODO: convert lhs to pointer ONLY for function-designator

    checkModLvalue(lhs);

    // T_ASSIGN
    //
    if (operator.ofType(T_ASSIGN)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = lhsRT;
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
        /* OK */;
      } else if (lhsRT.isPointer() && rhs.isIntegerZero()) {
        /* OK */;
      } else if (lhsRT.isPointer() && rhsRT.isEqualTo(lhsRT)) {
        /* OK */;
      } else if (lhsRT.isStruct() && rhsRT.isEqualTo(lhsRT)) {
        /* OK */;
      } else if (lhsRT.isUnion() && rhsRT.isEqualTo(lhsRT)) {
        /* OK */;
      } else if (lhsRT.isPointerToVoid() && rhsRT.isPointerToObject()) {
        /* OK */;
      } else if (lhsRT.isPointerToVoid() && rhsRT.isPointerToIncomplete()) {
        /* OK */;
      } else if (lhsRT.isPointerToObject() && rhsRT.isPointerToVoid()) {
        /* OK */;
      } else if (lhsRT.isPointerToIncomplete() && rhsRT.isPointerToVoid()) {
        /* OK */;
      } else {
        errorExpr("Assign binary expression error: ", operator, lhs, rhs);
      }
      checkResultType(resRT, operator, lhs, rhs);

      CExpression castExpr = new CExpression(lhsRT, rhs, operator);
      castExpr.setResultType(lhsRT);

      CExpression resultExpression = new CExpression(CExpressionBase.EASSIGN, lhs, castExpr, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    else {
      errorUnknownBinaryOperator(operator);
    }

    errorUnknownBinaryOperator(operator);
    return null;
  }

  private static void checkModLvalue(CExpression lhs) {
    if (!lhs.isModifiableLvalue()) {
      throw new ParseException("not an Lvalue: " + lhs.toString());
    }

  }

  private static void errorUnknownBinaryOperator(Token operator) {
    throw new ParseException("errorUnknownBinaryOperator: " + operator.toString());
  }

  private static void errorExpr(String string, Token operator, CExpression lHS, CExpression rHS) {
    throw new ParseException("errorExpr: " + lHS.toString() + " " + operator.getValue() + " " + rHS.toString());
  }

  private static void checkResultType(CType resultType, Token operator, CExpression lHS, CExpression rHS) {
    if (resultType == null) {
      throw new ParseException("checkResultType: " + lHS.toString() + " " + operator.getValue() + " " + rHS.toString());
    }
  }

}
