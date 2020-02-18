package ast.expr.sem;

import static jscan.tokenize.T.TOKEN_NUMBER;
import jscan.cstrtox.C_strtox;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CTypeImpl;
import ast.expr.main.CExpression;
import ast.parse.NullChecker;
import ast.symtabg.elements.NumericConstant;

public abstract class CExpressionBuilderHelper {

  public static Token copyTokenAddNewType(Token from, T newtype, String newvalue) {
    NullChecker.check(from, newtype, newvalue);

    Token ntoken = new Token(from);
    ntoken.setType(newtype);
    ntoken.setValue(newvalue);
    return ntoken;
  }

  public static Token plusOperator(Token from) {
    return copyTokenAddNewType(from, T.T_PLUS, "+");
  }

  public static Token derefOperator(Token from) {
    return copyTokenAddNewType(from, T.T_TIMES, "*");
  }

  public static Token assignOperator(Token from) {
    return copyTokenAddNewType(from, T.T_ASSIGN, "=");
  }

  public static CExpression createNumericConst(Token from, Integer intValue) {
    C_strtox e = new C_strtox(intValue.toString());
    return new CExpression(e, from);
  }

  public static CExpression digitZero(Token from) {
    NullChecker.check(from);

    NumericConstant number = new NumericConstant(0, NumType.N_INT);
    CExpression ret = new CExpression(number, copyTokenAddNewType(from, TOKEN_NUMBER, "0"));

    ret.setResultType(CTypeImpl.TYPE_INT);
    return ret;
  }

  public static CExpression digitOne(Token from) {
    NullChecker.check(from);

    NumericConstant number = new NumericConstant(1, NumType.N_INT);
    CExpression ret = new CExpression(number, copyTokenAddNewType(from, TOKEN_NUMBER, "1"));

    ret.setResultType(CTypeImpl.TYPE_INT);
    return ret;
  }

}