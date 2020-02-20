package ast.expr.sem;

import static ast._typesnew.CTypeImpl.TYPE_DOUBLE;
import static ast._typesnew.CTypeImpl.TYPE_FLOAT;
import static ast._typesnew.CTypeImpl.TYPE_INT;
import static ast._typesnew.CTypeImpl.TYPE_LONG_DOUBLE;
import static ast._typesnew.CTypeImpl.TYPE_LONG_LONG;
import static ast._typesnew.CTypeImpl.TYPE_VOID;
import static ast.expr.main.CExpressionBase.EASSIGN;
import static ast.expr.main.CExpressionBase.EBINARY;
import static ast.expr.main.CExpressionBase.ECOMMA;
import static ast.expr.main.CExpressionBase.ECOMPSEL;
import static ast.expr.main.CExpressionBase.EFCALL;
import static ast.expr.main.CExpressionBase.EPOSTINCDEC;
import static ast.expr.main.CExpressionBase.EPREINCDEC;
import static ast.expr.main.CExpressionBase.EPRIMARY_IDENT;
import static ast.expr.main.CExpressionBase.EPRIMARY_NUMBER;
import static ast.expr.main.CExpressionBase.ETERNARY;
import static ast.expr.main.CExpressionBase.EUNARY;
import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_AND_AND;
import static jscan.tokenize.T.T_ASSIGN;
import static jscan.tokenize.T.T_DIVIDE;
import static jscan.tokenize.T.T_EQ;
import static jscan.tokenize.T.T_EXCLAMATION;
import static jscan.tokenize.T.T_GE;
import static jscan.tokenize.T.T_GT;
import static jscan.tokenize.T.T_LE;
import static jscan.tokenize.T.T_LSHIFT;
import static jscan.tokenize.T.T_LT;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_NE;
import static jscan.tokenize.T.T_OR;
import static jscan.tokenize.T.T_OR_OR;
import static jscan.tokenize.T.T_PERCENT;
import static jscan.tokenize.T.T_PLUS;
import static jscan.tokenize.T.T_RSHIFT;
import static jscan.tokenize.T.T_TILDE;
import static jscan.tokenize.T.T_TIMES;
import static jscan.tokenize.T.T_XOR;
import jscan.cstrtox.NumType;
import jscan.tokenize.Token;
import ast._typesnew.CPointerType;
import ast._typesnew.CType;
import ast._typesnew.CTypeImpl;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.NullChecker;

public abstract class TypeApplier {

  private static void assertType(CExpression e) {
    if (e.getResultType() == null) {
      throw new ParseException(e.getLocationToString() + " error: type not added. " + e.toString());
    }
  }

  public static void applytype(CExpression e, TaStage stage) {

    final CExpressionBase base = e.getBase();
    if (e.getResultType() != null) {
      return;
    }

    if (base == EASSIGN) {
      applytype(e.getLhs(), TaStage.assign_lhs);
      applytype(e.getRhs(), TaStage.assign_rhs);

      assertType(e.getLhs());
      assertType(e.getRhs());

      applyAssign(e);
    }

    else if (base == EBINARY) {

      applytype(e.getLhs(), TaStage.binary_lhs);
      applytype(e.getRhs(), TaStage.binary_rhs);

      assertType(e.getLhs());
      assertType(e.getRhs());

      applyBinary(e);

    }

    else if (base == ECOMMA) {
      applytype(e.getLhs(), TaStage.comma_lhs);
      applytype(e.getRhs(), TaStage.comma_rhs);

      assertType(e.getLhs());
      assertType(e.getRhs());

      e.setResultType(e.getRhs().getResultType());
    }

    else if (base == ETERNARY) {
      applytype(e.getCnd(), TaStage.tern_cnd);
      applytype(e.getLhs(), TaStage.tern_true);
      applytype(e.getRhs(), TaStage.tern_false);

      assertType(e.getCnd());
      assertType(e.getLhs());
      assertType(e.getRhs());

      // TODO: more checks
      e.setResultType(e.getRhs().getResultType());
    }

    else if (base == EUNARY) {
      applytype(e.getLhs(), TaStage.unary_operand);

      assertType(e.getLhs());
      applyUnary(e);
    }

    else if (base == EPRIMARY_IDENT) {
      //System.out.printf("stage=%-16s, ident=%s\n", stage.toString(), e.getSymbol().getName().getName());

      final CType symtype = e.getSymbol().getType();
      e.setResultType(symtype);

      if (stage == TaStage.generic_control_expr) {
        genPointer(e);
      }
    }

    else if (base == EPRIMARY_NUMBER) {
      //System.out.printf("stage=%-16s, number=%d\n", stage.toString(), e.getCnumber().getClong());

      final NumType numtype = e.getCnumber().getNumtype();
      e.setResultType(CTypeImpl.bindings.get(numtype));
    }

    else if (base == ECOMPSEL) {
      applytype(e.getLhs(), TaStage.compsel_postfix);

      assertType(e.getLhs());
      e.setResultType(e.getField().getType());
    }

    else if (base == EFCALL) {
      applytype(e.getLhs(), TaStage.fcall_function);

      assertType(e.getLhs());
      applyFcall(e);
    }

    else if (base == EPREINCDEC) {
      applytype(e.getLhs(), TaStage.preincdec_operand);

      assertType(e.getLhs());

      // TODO: more checks
      e.setResultType(e.getLhs().getResultType());
    }

    else if (base == EPOSTINCDEC) {
      applytype(e.getLhs(), TaStage.postincdec_operand);

      assertType(e.getLhs());

      // TODO: more checks
      e.setResultType(e.getLhs().getResultType());
    }

    else {
      throw new ParseException("unimpl. base: " + base.toString());
    }

  }

  private static void applyAssign(CExpression e) {
    final Token operator = e.getToken();
    final CExpression lhs = e.getLhs();
    final CExpression rhs = e.getRhs();

    checkModLvalue(lhs);

    // allow pointer to function, but NOT an array
    genPointerFn(lhs);
    // allow pointer to function, AND array
    genPointer(rhs);

    final CType lhsRT = lhs.getResultType();
    final CType rhsRT = rhs.getResultType();
    CType resRT = lhsRT;

    if (operator.ofType(T_ASSIGN)) {
      if (lhsRT.isArithmetic() && rhsRT.isArithmetic()) {
      } else if (lhsRT.isPointer() && rhs.isIntegerZero()) {
      } else if (lhsRT.isPointer() && rhsRT.isEqualTo(lhsRT)) {
      } else if (lhsRT.isStruct() && rhsRT.isEqualTo(lhsRT)) {
      } else if (lhsRT.isUnion() && rhsRT.isEqualTo(lhsRT)) {
      } else if (lhsRT.isPointerToVoid() && rhsRT.isPointerToObject()) {
      } else if (lhsRT.isPointerToVoid() && rhsRT.isPointerToIncomplete()) {
      } else if (lhsRT.isPointerToObject() && rhsRT.isPointerToVoid()) {
      } else if (lhsRT.isPointerToIncomplete() && rhsRT.isPointerToVoid()) {
      } else {
        errorExpr("Assign binary expression error: ", operator, lhs, rhs);
      }
    }

    checkResultType(resRT, operator, lhs, rhs);
    e.setResultType(resRT);
  }

  private static void applyFcall(CExpression e) {
    final CExpression function = e.getLhs();
    final CType resultType = function.getResultType();
    final boolean isFunction = resultType.isFunction();

    if (!(isFunction || resultType.isPointerToFunction())) {
      throw new ParseException("expect function: " + resultType.toString());
    }

    if (isFunction) {
      e.setResultType(resultType.getTpFunction().getReturnType());
    } else {
      e.setResultType(resultType.getTpPointer().getPointerTo().getTpFunction().getReturnType());
    }
  }

  private static void applyUnary(CExpression e) {
    final Token operator = e.getToken();
    final CExpression operand = e.getLhs();

    if (!operator.ofType(T_AND)) {
      genPointer(operand);
    }

    // !
    //
    if (operator.ofType(T_EXCLAMATION)) {
      CType lhsRT = ipromote(operand.getResultType());
      CType resRT = null;
      if (lhsRT.isScalar()) {
        resRT = TYPE_INT;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }
      checkResultType(resRT, operator, operand);
      e.setResultType(resRT);
    }

    // [- + ~]
    //
    else if (operator.ofType(T_MINUS) || operator.ofType(T_PLUS) || operator.ofType(T_TILDE)) {
      CType lhsRT = ipromote(operand.getResultType());
      CType resRT = null;
      if (lhsRT.isArithmetic()) {
        resRT = lhsRT;
      } else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }
      checkResultType(resRT, operator, operand);
      e.setResultType(resRT);
    }

    // address-of
    //
    else if (operator.ofType(T_AND)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;

      if (lhsRT.isAnObjectExceptBitField()) {
        resRT = genPtrTo(lhsRT);
      }

      else if (lhsRT.isIncomplete()) {
        resRT = genPtrTo(lhsRT);
      }

      else if (lhsRT.isFunction()) {
        resRT = genPtrTo(lhsRT);
      }

      else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }
      checkResultType(resRT, operator, operand);
      e.setResultType(resRT);
    }

    // dereference
    //
    else if (operator.ofType(T_TIMES)) {
      CType lhsRT = operand.getResultType();
      CType resRT = null;

      if (lhsRT.isPointerToObject()) {
        resRT = lhsRT.getTpPointer().getPointerTo(); // XXX:
      }

      // result is function-designator.

      // A function-designator subcontext designates a function. 
      // Hence, its expression has a function type. 
      // You create a function-designator subcontext wherever 
      // you need to call a function or determine its address.

      else if (lhsRT.isPointerToFunction()) {
        resRT = lhsRT.getTpPointer().getPointerTo();
      }

      else if (lhsRT.isPointerToVoid()) {
        resRT = TYPE_VOID;
      }

      else {
        errorUnaryExpr("Unary expression error: ", operator, operand);
      }

      if (resRT == null) {
        //System.out.println();
      }

      checkResultType(resRT, operator, operand);
      e.setResultType(resRT);
    }

    else {
      errorUnknownUnaryOperator(operator);
    }
  }

  private static void applyBinary(CExpression e) {
    final Token operator = e.getToken();
    final CExpression lhs = e.getLhs();
    final CExpression rhs = e.getRhs();

    genPointer(lhs);
    genPointer(rhs);

    final CType Ltype = lhs.getResultType();
    final CType Rtype = rhs.getResultType();
    CType tpOfResult = null;

    // T_PLUS
    //
    if (operator.ofType(T_PLUS)) {
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
        tpOfResult = balanced(lhs, rhs);
      } else if (Ltype.isPointerToObject() && Rtype.isInteger()) {
        tpOfResult = Ltype;
      } else if (Ltype.isInteger() && Rtype.isPointerToObject()) {
        tpOfResult = Rtype;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // T_MINUS
    //
    else if (operator.ofType(T_MINUS)) {
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
        tpOfResult = balanced(lhs, rhs);
      } else if (Ltype.isPointerToObject() && Rtype.isInteger()) {
        tpOfResult = Ltype;
      } else if (Ltype.isPointerToObject() && Rtype.isPointerToCompat(Ltype)) {
        tpOfResult = TYPE_LONG_LONG;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // * /
    //
    else if (operator.ofType(T_DIVIDE) || operator.ofType(T_TIMES)) {
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
        tpOfResult = balanced(lhs, rhs);
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // % & | ^
    //
    else if (operator.ofType(T_PERCENT) || operator.ofType(T_AND) || operator.ofType(T_OR) || operator.ofType(T_XOR)) {
      if (Ltype.isInteger() && Rtype.isInteger()) {
        tpOfResult = balanced(lhs, rhs);
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // && ||
    //
    else if (operator.ofType(T_AND_AND) || operator.ofType(T_OR_OR)) {
      if (Ltype.isScalar() && Rtype.isScalar()) {
        tpOfResult = TYPE_INT;
      } else {
        errorExpr("Binary expression error: ", operator, lhs, rhs);
      }
    }

    // <  <=  >  >=
    //
    else if (operator.ofType(T_LT) || operator.ofType(T_LE) || operator.ofType(T_GT) || operator.ofType(T_GE)) {
      tpOfResult = TYPE_INT;
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
      } else if (Ltype.isPointer() && Rtype.isPointerToCompat(Ltype)) {
      } else {
        errorExpr("Equality binary expression error: ", operator, lhs, rhs);
      }
    }

    // ==  !=
    //
    else if (operator.ofType(T_EQ) || operator.ofType(T_NE)) {
      tpOfResult = TYPE_INT;
      if (Ltype.isArithmetic() && Rtype.isArithmetic()) {
      } else if (Ltype.isPointer() && rhs.isIntegerZero()) {
      } else if (lhs.isIntegerZero() && Rtype.isPointer()) {
      } else if (Ltype.isPointer() && Rtype.isPointerToCompat(Ltype)) {
      } else if (Ltype.isPointerToVoid() && Rtype.isPointerToObject()) {
      } else if (Ltype.isPointerToVoid() && Rtype.isPointerToIncomplete()) {
      } else if (Ltype.isPointerToObject() && Rtype.isPointerToVoid()) {
      } else if (Ltype.isPointerToIncomplete() && Rtype.isPointerToVoid()) {
      } else {
        errorExpr("Equality binary expression error: ", operator, lhs, rhs);
      }
    }

    // T_LSHIFT
    //
    else if (operator.ofType(T_LSHIFT)) {
      if (Ltype.isInteger() && Rtype.isInteger()) {
        tpOfResult = Ltype;
      } else {
        errorExpr("Shift binary expression error: ", operator, lhs, rhs);
      }
    }

    // T_RSHIFT
    //
    else if (operator.ofType(T_RSHIFT)) {
      if (Ltype.isInteger() && Rtype.isInteger()) {
        tpOfResult = balanced(lhs, rhs);
      } else {
        errorExpr("Shift binary expression error: ", operator, lhs, rhs);
      }
    }

    else {
      errorUnknownBinaryOperator(operator);
    }

    checkResultType(tpOfResult, operator, lhs, rhs);
    e.setResultType(tpOfResult);
  }

  private static void checkModLvalue(CExpression lhs) {
    // TODO Auto-generated method stub

  }

  private static void errorUnknownBinaryOperator(Token operator) {
    throw new ParseException("errorUnknownBinaryOperator: " + operator.toString());
  }

  private static void errorExpr(String string, Token operator, CExpression lHS, CExpression rHS) {
    throw new ParseException("errorExpr: " + lHS.toString() + " " + operator.getValue() + " " + rHS.toString());
  }

  private static void checkResultType(CType resultType, Token operator, CExpression lHS, CExpression rHS) {
    if (resultType == null) {
      throw new ParseException("checkResultType: " + lHS.toString() + " " + operator.getValue() + " " + rHS.toString());
    }
  }

  private static void genPointer(CExpression inputExpr) {

    NullChecker.check(inputExpr);
    final CType origType = inputExpr.getResultType();

    if (origType.isArray()) {
      CType arrtype = origType.getTpArray().getArrayOf();
      CType ptrtype = new CType(new CPointerType(arrtype, false));
      inputExpr.setResultType(ptrtype);
    }

    if (origType.isFunction()) {
      CType ptrtype = new CType(new CPointerType(origType, false));
      inputExpr.setResultType(ptrtype);
    }

  }

  private static void genPointerFn(CExpression inputExpr) {

    NullChecker.check(inputExpr);
    final CType typeOfNode = inputExpr.getResultType();

    if (typeOfNode.isFunction()) {
      CType ptrtype = new CType(new CPointerType(typeOfNode, false));
      inputExpr.setResultType(ptrtype);
    }

  }

  private static void errorUnaryExpr(String string, Token operator, CExpression lhs) {
    throw new ParseException("errorUnaryExpr: " + operator.toString() + " " + lhs.toString());

  }

  private static CType genPtrTo(CType lhsRT) {
    return new CType(new CPointerType(lhsRT, false));
  }

  private static void errorUnknownUnaryOperator(Token operator) {
    throw new ParseException("errorUnknownUnaryOperator: " + operator.toString());

  }

  private static void checkResultType(CType resRT, Token operator, CExpression lhs) {
    if (resRT == null) {
      throw new ParseException("checkResultType: " + operator.toString() + " " + lhs.toString());
    }
  }

  private static CType balanced(CExpression lhs, CExpression rhs) {
    final CType lhsRt = lhs.getResultType();
    final CType rhsRt = rhs.getResultType();

    if (lhsRt.isLongDouble() || rhsRt.isLongDouble()) {
      return TYPE_LONG_DOUBLE;
    } else if (lhsRt.isDouble() || rhsRt.isDouble()) {
      return TYPE_DOUBLE;
    } else if (lhsRt.isFloat() || rhsRt.isFloat()) {
      return TYPE_FLOAT;
    } else {
      CType prom_1 = ipromote(lhsRt);
      CType prom_2 = ipromote(rhsRt);
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

  public static CType ipromote(CType res) {
    if (res.isBool()) {
      return TYPE_INT;
    }
    if (res.isUchar() || res.isChar()) {
      return TYPE_INT;
    }
    if (res.isUshort() || res.isShort()) {
      return TYPE_INT;
    }
    return res;
  }

}
