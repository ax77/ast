package ast.expr.sem;

import ast._typesnew.CType;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.Parse;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public abstract class CExpressionBuilder {

  private static CExpression ecast(CType to, CExpression what, Token where) {
    return new CExpression(to, what, where);
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

    System.out.println("r2::" + res.toString());
    return res;

  }

  public static CExpression assign(Token tok, CExpression lvalue, CExpression rvalue) {
    return new CExpression(CExpressionBase.EASSIGN, lvalue, rvalue, tok);
  }

  public static CExpression comma(Token tok, T op, CExpression lhs, CExpression rhs) {
    return new CExpression(CExpressionBase.ECOMMA, lhs, rhs, tok);
  }

}
