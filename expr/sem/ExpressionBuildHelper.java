package ast.expr.sem;

import static jscan.tokenize.T.TOKEN_NUMBER;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CType;
import ast.expr.main.CExpression;
import ast.parse.NullChecker;
import ast.symtabg.elements.NumericConstant;

public abstract class ExpressionBuildHelper {

  public static Token copyTokenAddNewType(Token from, T newtype, String newvalue) {
    NullChecker.check(from, newtype, newvalue);

    Token ntoken = new Token(from);
    ntoken.setType(newtype);
    ntoken.setValue(newvalue);
    return ntoken;
  }

  public static CExpression digitZero(Token from) {
    NullChecker.check(from);

    NumericConstant number = new NumericConstant(0, NumType.N_INT);
    CExpression ret = new CExpression(number, copyTokenAddNewType(from, TOKEN_NUMBER, "0"));

    ret.setResultType(CType.TYPE_INT);
    return ret;
  }

  public static CExpression digitOne(Token from) {
    NullChecker.check(from);

    NumericConstant number = new NumericConstant(1, NumType.N_INT);
    CExpression ret = new CExpression(number, copyTokenAddNewType(from, TOKEN_NUMBER, "1"));

    ret.setResultType(CType.TYPE_INT);
    return ret;
  }
}