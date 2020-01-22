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
import ast.expr.sem.etype.BinaryTyped;
import ast.expr.sem.etype.UnaryTyped;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;

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
    ret.setResultType(CType.bindings.get(numtype));

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
      ret.setResultType(new CType(TypeKind.TP_INT, StorageKind.ST_NONE));
    } else {
      ret.setResultType(e.getType());
    }
    return ret;
  }

}
