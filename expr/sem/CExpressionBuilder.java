package ast.expr.sem;

import static ast._typesnew.CType.TYPE_INT;
import static ast._typesnew.CType.TYPE_LONG_LONG;
import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_AND_AND;
import static jscan.tokenize.T.T_DIVIDE;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_OR;
import static jscan.tokenize.T.T_OR_OR;
import static jscan.tokenize.T.T_PERCENT;
import static jscan.tokenize.T.*;
import static jscan.tokenize.T.T_TIMES;
import static jscan.tokenize.T.T_XOR;
import jscan.cstrtox.C_strtox;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.NullChecker;
import ast.parse.Parse;
import ast.parse.ParseException;
import ast.symtabg.elements.CSymbol;

public abstract class CExpressionBuilder {

  private static CExpression sBinary(Token operator, CExpression lhs, CExpression rhs) {

    lhs = ImplicitCast.genPointer(lhs);
    rhs = ImplicitCast.genPointer(rhs);

    // T_PLUS
    //
    if (operator.ofType(T_PLUS)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        resRT = bb.getBalancedResult();
      } else if (lhsRT.isPointerToObject() && rhsRT.isInteger()) {
        resRT = lhsRT;
      } else if (lhsRT.isInteger() && rhsRT.isPointerToObject()) {
        resRT = rhsRT;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_MINUS
    //
    else if (operator.ofType(T_MINUS)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        resRT = bb.getBalancedResult();
      } else if (lhsRT.isPointerToObject() && rhsRT.isInteger()) {
        resRT = lhsRT;
      } else if (lhsRT.isPointerToObject() && rhsRT.isPointerToCompat(lhsRT)) {
        resRT = TYPE_LONG_LONG;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // * /
    //
    else if (operator.ofType(T_DIVIDE) || operator.ofType(T_TIMES)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        resRT = bb.getBalancedResult();
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // % & | ^
    //
    else if (operator.ofType(T_PERCENT) || operator.ofType(T_AND) || operator.ofType(T_OR) || operator.ofType(T_XOR)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isInteger() && rhsRT.isInteger()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        resRT = bb.getBalancedResult();
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // && ||
    //
    else if (operator.ofType(T_AND_AND) || operator.ofType(T_OR_OR)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isScalar() && rhsRT.isScalar()) {
        resRT = TYPE_INT;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // <  <=  >  >=
    //
    else if (operator.ofType(T_LT) || operator.ofType(T_LE) || operator.ofType(T_GT) || operator.ofType(T_GE)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = TYPE_INT;
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
        /* OK */;
      } else if (lhsRT.isPointer() && rhsRT.isPointerToCompat(lhsRT)) {
        /* OK */;
      } else {
        errorExpr("Equality binary expression error: ", operator, lhs, rhs);
      }

      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // ==  !=
    //
    else if (operator.ofType(T_EQ) || operator.ofType(T_NE)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = TYPE_INT;
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
        /* OK */;
      } else if (lhsRT.isPointer() && rhs.isIntegerZero()) {
        /* OK */;
      } else if (lhs.isIntegerZero() && rhsRT.isPointer()) {
        /* OK */;
      } else if (lhsRT.isPointer() && rhsRT.isPointerToCompat(lhsRT)) {
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
        errorExpr("Equality binary expression error: ", operator, lhs, rhs);
      }

      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_LSHIFT
    //
    else if (operator.ofType(T_LSHIFT)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isInteger() && rhsRT.isInteger()) {
        resRT = lhsRT;
      } else {
        errorExpr("Shift binary expression error: ", operator, lhs, rhs);
      }

      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    // T_RSHIFT
    //
    else if (operator.ofType(T_RSHIFT)) {
      CType lhsRT = lhs.getResultType();
      CType rhsRT = rhs.getResultType();
      CType resRT = null;
      if (lhsRT.isInteger() && rhsRT.isInteger()) {
        BinaryBalancing bb = new BinaryBalancing(lhs, rhs);
        lhs = bb.getCastedLhs();
        rhs = bb.getCastedRhs();
        resRT = bb.getBalancedResult();
      } else {
        errorExpr("Shift binary expression error: ", operator, lhs, rhs);
      }

      checkResultType(resRT, operator, lhs, rhs);
      CExpression resultExpression = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      resultExpression.setResultType(resRT);
      return resultExpression;
    }

    else {
      errorUnknownBinaryOperator(operator);
    }

    errorUnknownBinaryOperator(operator);
    return null;
  }

  private static CExpression sAssign(Token operator, CExpression lhs, CExpression rhs) {

    NullChecker.check(operator, lhs, rhs, lhs.getResultType(), rhs.getResultType());

    // TODO: modLvalue and etc...

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

      CExpression castExpr = new CExpression(lhsRT, rhs, operator, false);
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
    throw new ParseException("errorExpr: " + operator.toString() + " " + lHS.toString() + " " + rHS.toString());
  }

  private static void checkResultType(CType resultType, Token operator, CExpression lHS, CExpression rHS) {
    if (resultType == null) {
      throw new ParseException("checkResultType: " + operator.toString() + " " + lHS.toString() + " " + rHS.toString());
    }
  }

  //public CExpression(Token op, CExpression lhs, boolean isParameterStubToDestroyConstructorUsage) 
  public static CExpression unary(Token op, CExpression operand, boolean isParameterStubToDestroyConstructorUsage) {
    CExpression res = new CExpression(op, operand, isParameterStubToDestroyConstructorUsage);
    return res;
  }

  public static CExpression binary(Token operator, Parse parser, CExpression lhs, CExpression rhs) {
    return sBinary(operator, lhs, rhs);
  }

  public static CExpression assign(Token tok, CExpression lvalue, CExpression rvalue) {
    return sAssign(tok, lvalue, rvalue);
  }

  public static CExpression comma(Token tok, T op, CExpression lhs, CExpression rhs) {
    return new CExpression(CExpressionBase.ECOMMA, lhs, rhs, tok);
  }

  // public CExpression(C_strtox e, Token token, boolean isParameterStubToDestroyConstructorUsage)
  public static CExpression number(C_strtox e, Token token, Parse parser,
      boolean isParameterStubToDestroyConstructorUsage) {

    CExpression ret = new CExpression(e, token, isParameterStubToDestroyConstructorUsage);

    final NumType numtype = ret.getCnumber().getNumtype();
    ret.setResultType(CType.bindings.get(numtype));

    return ret;
  }

  //public CExpression(CType typename, CExpression tocast, Token token, boolean isParameterStubToDestroyConstructorUsage)
  public static CExpression doCast(Parse parser, CType typename, CExpression tocast, Token token,
      boolean isParameterStubToDestroyConstructorUsage) {
    CExpression ret = new CExpression(typename, tocast, token, isParameterStubToDestroyConstructorUsage);
    ret.setResultType(typename);
    return ret;

  }

  //e.setResultType(e.getSymbol().getType());
  // public CExpression(CSymbol e, Token token, boolean isParameterStubToDestroyConstructorUsage)
  public static CExpression esymbol(Parse parser, CSymbol e, Token token,
      boolean isParameterStubToDestroyConstructorUsage) {
    CExpression ret = new CExpression(e, token, isParameterStubToDestroyConstructorUsage);
    if (!parser.isSemanticEnable()) {
      ret.setResultType(new CType(TypeKind.TP_INT, StorageKind.ST_NONE));
    } else {
      ret.setResultType(e.getType());
    }
    return ret;
  }

}
