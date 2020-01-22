package ast.expr.sem.etype;

import static ast._typesnew.CType.TYPE_DOUBLE;
import static ast._typesnew.CType.TYPE_FLOAT;
import static ast._typesnew.CType.TYPE_LONG_DOUBLE;
import ast._typesnew.CType;
import ast.expr.main.CExpression;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.parse.NullChecker;

public class BinaryBalancing {

  // input
  private final CExpression lhs;
  private final CExpression rhs;

  // output
  private final CType balancedResult;
  private final CExpression castedLhs;
  private final CExpression castedRhs;

  public BinaryBalancing(CExpression lhs, CExpression rhs) {
    NullChecker.check(lhs, rhs, lhs.getResultType(), rhs.getResultType());

    this.lhs = lhs;
    this.rhs = rhs;

    // TODO: if type are the same, we actually need promote integers if it was here.
    // for example: (_Bool)1 + (_Bool)0 :: sizeof(e) == 4, not 1
    //

    this.balancedResult = balanced();
    this.castedLhs = cast(lhs, balancedResult);
    this.castedRhs = cast(rhs, balancedResult);
  }

  private CExpression cast(CExpression castThis, CType toThat) {
    CExpression castTo = new CExpression(toThat, castThis, castThis.getToken());
    return castTo;
  }

  private CType balanced() {
    CType lhsRt = lhs.getResultType();
    CType rhsRt = rhs.getResultType();

    if (lhsRt.isLongDouble() || rhsRt.isLongDouble()) {
      return TYPE_LONG_DOUBLE;
    } else if (lhsRt.isDouble() || rhsRt.isDouble()) {
      return TYPE_DOUBLE;
    } else if (lhsRt.isFloat() || rhsRt.isFloat()) {
      return TYPE_FLOAT;
    } else {
      CType prom_1 = CExpressionBuilderHelper.ipromote(lhsRt);
      CType prom_2 = CExpressionBuilderHelper.ipromote(rhsRt);
      if (prom_1.getSize() > prom_2.getSize()) {
        return prom_1;
      } else if (prom_2.getSize() > prom_1.getSize()) {
        return prom_2;
      } else {
        if (prom_1.isUnsigned()) {
          return prom_1;
        } else {
          return prom_2;
        }
      }
    }
  }

  public CType getBalancedResult() {
    return balancedResult;
  }

  public CExpression getCastedLhs() {
    return castedLhs;
  }

  public CExpression getCastedRhs() {
    return castedRhs;
  }

}