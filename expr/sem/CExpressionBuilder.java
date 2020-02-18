package ast.expr.sem;

import java.util.List;

import jscan.cstrtox.C_strtox;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CStructField;
import ast._typesnew.CType;
import ast._typesnew.CTypeImpl;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;

public abstract class CExpressionBuilder {

  public static CExpression unary(Token op, CExpression operand) {
    return new CExpression(CExpressionBase.EUNARY, op, operand);
  }

  public static CExpression binary(Token operator, CExpression lhs, CExpression rhs) {
    return new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
  }

  public static CExpression assign(Token tok, CExpression lvalue, CExpression rvalue) {
    return new CExpression(CExpressionBase.EASSIGN, lvalue, rvalue, tok);
  }

  public static CExpression comma(Token tok, T op, CExpression lhs, CExpression rhs) {
    final CExpression res = new CExpression(CExpressionBase.ECOMMA, lhs, rhs, tok);
    res.setResultType(rhs.getResultType());
    return res;
  }

  public static CExpression number(C_strtox e, Token token) {

    CExpression ret = new CExpression(e, token);

    final NumType numtype = ret.getCnumber().getNumtype();
    ret.setResultType(CTypeImpl.bindings.get(numtype));

    return ret;
  }

  public static CExpression doCast(Parse parser, CType typename, CExpression tocast, Token token) {
    CExpression ret = new CExpression(typename, tocast, token);
    ret.setResultType(typename);
    return ret;
  }

  public static CExpression esymbol(CSymbol e, Token token) {
    CExpression ret = new CExpression(e, token);
    ret.setResultType(e.getType());
    return ret;
  }

  public static CExpression eStructFieldAccess(CExpression postfis, Token operator, CStructField fieldName) {
    CExpression ret = new CExpression(postfis, operator, fieldName);
    ret.setResultType(fieldName.getType());
    return ret;
  }

  // TODO: FuncTyped

  public static CExpression efcall(CExpression function, List<CExpression> arguments, Token token) {
    CExpression fcall = new CExpression(function, arguments, token);
    final CType resultType = function.getResultType();

    final boolean isFunction = resultType.isFunction();
    final boolean isPointerToFunction = resultType.isPointerToFunction();
    final boolean isFuncOk = isFunction || isPointerToFunction;
    if (!isFuncOk) {
      throw new ParseException("expect function: " + resultType.toString());
    }

    if (isFunction) {
      fcall.setResultType(resultType.getTpFunction().getReturnType());
    } else {
      fcall.setResultType(resultType.getTpPointer().getPointerTo().getTpFunction().getReturnType());
    }
    return fcall;

  }

  public static CExpression incdec(CExpressionBase base, Token op, CExpression lhs) {
    CExpression ret = new CExpression(base, op, lhs);
    //TODO: scalar's
    //TODO: mod lvalue's
    ret.setResultType(lhs.getResultType());
    return ret;
  }

}
