package ast.expr.sem;

import jscan.cstrtox.C_strtox;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;

public abstract class CExpressionBuilder {

  private static CExpression ecast(CType to, CExpression what, Token where) {
    return new CExpression(to, what, where, false);
  }

  //public CExpression(Token op, CExpression lhs, boolean isParameterStubToDestroyConstructorUsage) 
  public static CExpression unary(Token op, CExpression operand, boolean isParameterStubToDestroyConstructorUsage) {
    CExpression res = new CExpression(op, operand, isParameterStubToDestroyConstructorUsage);
    return res;
  }

  public static CExpression binary(Token operator, Parse parser, CExpression lhs, CExpression rhs) {
    TypeApplier.applyType(lhs, parser);
    TypeApplier.applyType(rhs, parser);

    CType lhstype = lhs.getResultType();
    CType rhstype = rhs.getResultType();

    if (lhstype.isEqualTo(rhstype)) {
      // integer promotion
      CType resultType = TypeApplier.applyBinaryUsualArithConv(parser, operator, lhs, rhs);
      lhs.setResultType(resultType);
      rhs.setResultType(resultType);

      final CExpression res = new CExpression(CExpressionBase.EBINARY, lhs, rhs, operator);
      res.setResultType(resultType);

      //System.out.println("r1::" + res.toString());
      return res;
    }

    CType resultType = TypeApplier.applyBinaryUsualArithConv(parser, operator, lhs, rhs);
    CExpression lhsnew = ecast(resultType, lhs, operator);
    CExpression rhsnew = ecast(resultType, rhs, operator);

    final CExpression res = new CExpression(CExpressionBase.EBINARY, lhsnew, rhsnew, operator);
    res.setResultType(resultType);

    //System.out.println("r2::" + res.toString());
    return res;

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
    ret.setResultType(TypeApplier.bindings.get(numtype));

    return ret;
  }

  //public CExpression(CType typename, CExpression tocast, Token token, boolean isParameterStubToDestroyConstructorUsage)
  public static CExpression doCast(Parse parser, CType typename, CExpression tocast, Token token,
      boolean isParameterStubToDestroyConstructorUsage) {
    TypeApplier.applyType(tocast, parser);
    CExpression ret = new CExpression(typename, tocast, token, isParameterStubToDestroyConstructorUsage);
    ret.setResultType(typename);
    return ret;

  }

  //e.setResultType(e.getSymbol().getType());
  // public CExpression(CSymbol e, Token token, boolean isParameterStubToDestroyConstructorUsage)
  public static CExpression esymbol(Parse parser, CSymbol e, Token token, boolean isParameterStubToDestroyConstructorUsage) {
    CExpression ret = new CExpression(e, token, isParameterStubToDestroyConstructorUsage);
    if(!parser.isSemanticEnable()) {
      ret.setResultType(new CType(TypeKind.TP_INT, StorageKind.ST_NONE));
    } else {
      ret.setResultType(e.getType());
    }
    return ret;
  }

}
