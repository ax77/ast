package ast.expr.sem;

import java.util.List;

import ast._typesnew.CStructField;
import ast._typesnew.CType;
import ast._typesnew.CTypeImpl;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.expr.sem.etype.BinaryTyped;
import ast.expr.sem.etype.UnaryTyped;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import jscan.cstrtox.C_strtox;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public abstract class CExpressionBuilder {

  public static CExpression unary(Token op, CExpression operand) {
    CExpression res = UnaryTyped.sUnary(op, operand);
    return res;
  }

  public static CExpression binary(Token operator, Parse parser, CExpression lhs, CExpression rhs) {
    return BinaryTyped.sBinary(operator, lhs, rhs);
  }

  public static CExpression assign(Token tok, CExpression lvalue, CExpression rvalue) {
    return BinaryTyped.sAssign(tok, lvalue, rvalue);
  }

  public static CExpression comma(Token tok, T op, CExpression lhs, CExpression rhs) {
    final CExpression res = new CExpression(CExpressionBase.ECOMMA, lhs, rhs, tok);
    res.setResultType(rhs.getResultType());
    return res;
  }

  public static CExpression number(C_strtox e, Token token, Parse parser) {

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

  public static CExpression esymbol(Parse parser, CSymbol e, Token token) {
    CExpression ret = new CExpression(e, token);
    if (!parser.isSemanticEnable()) {
      ret.setResultType(CTypeImpl.TYPE_INT);
    } else {
      ret.setResultType(e.getType());
    }
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
