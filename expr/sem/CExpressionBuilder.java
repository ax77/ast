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
import static jscan.tokenize.T.T_PLUS;
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

    else {
      errorUnknownBinaryOperator(operator);
    }

    errorUnknownBinaryOperator(operator);
    return null;
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
    return new CExpression(CExpressionBase.EASSIGN, lvalue, rvalue, tok);
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
