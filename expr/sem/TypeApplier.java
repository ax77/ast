package ast.expr.sem;

import java.util.HashMap;
import java.util.Map;

import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.parse.NullChecker;
import ast.parse.Parse;
import ast.parse.ParseException;
import jscan.cstrtox.NumType;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public abstract class TypeApplier {

  //@formatter:off
  private static final CType TYPE_BOOL = new CType(TypeKind.TP_BOOL, StorageKind.ST_NONE);
  private static final CType TYPE_CHAR = new CType(TypeKind.TP_CHAR, StorageKind.ST_NONE);
  private static final CType TYPE_UCHAR = new CType(TypeKind.TP_UCHAR, StorageKind.ST_NONE);
  private static final CType TYPE_SHORT = new CType(TypeKind.TP_SHORT, StorageKind.ST_NONE);
  private static final CType TYPE_USHORT = new CType(TypeKind.TP_USHORT, StorageKind.ST_NONE);
  private static final CType TYPE_INT = new CType(TypeKind.TP_INT, StorageKind.ST_NONE);
  private static final CType TYPE_UINT = new CType(TypeKind.TP_UINT, StorageKind.ST_NONE);
  private static final CType TYPE_LONG = new CType(TypeKind.TP_LONG, StorageKind.ST_NONE);
  private static final CType TYPE_ULONG = new CType(TypeKind.TP_ULONG, StorageKind.ST_NONE);
  private static final CType TYPE_LONG_LONG = new CType(TypeKind.TP_LONG_LONG, StorageKind.ST_NONE);
  private static final CType TYPE_ULONG_LONG = new CType(TypeKind.TP_ULONG_LONG, StorageKind.ST_NONE);
  private static final CType TYPE_FLOAT = new CType(TypeKind.TP_FLOAT, StorageKind.ST_NONE);
  private static final CType TYPE_DOUBLE = new CType(TypeKind.TP_DOUBLE, StorageKind.ST_NONE);
  private static final CType TYPE_LONG_DOUBLE = new CType(TypeKind.TP_LONG_DOUBLE, StorageKind.ST_NONE);
  
  private static Map<NumType, CType> bindings = new HashMap<NumType, CType>();
  static {
    bindings.put(NumType.N_INT           , TYPE_INT);
    bindings.put(NumType.N_UINT          , TYPE_UINT);
    bindings.put(NumType.N_LONG          , TYPE_LONG);
    bindings.put(NumType.N_ULONG         , TYPE_ULONG);
    bindings.put(NumType.N_LONG_LONG     , TYPE_LONG_LONG);
    bindings.put(NumType.N_ULONG_LONG    , TYPE_ULONG_LONG);
    bindings.put(NumType.N_FLOAT         , TYPE_FLOAT);
    bindings.put(NumType.N_DOUBLE        , TYPE_DOUBLE);
    bindings.put(NumType.N_LONG_DOUBLE   , TYPE_LONG_DOUBLE);
  }
  //@formatter:on

  private static void assertResultType(CExpression e) {
    if (e.getResultType() == null) {
      throw new ParseException("error:" + e.getToken().getLocation() + "result type is null");
    }
  }

  private static void assertIsArithmetic(Parse p, CExpression e) {
    if (!e.getResultType().isArithmetic()) {
      p.perror("expect arithmetic expression, but was: " + e.toString());
    }
  }

  private static void assertIsInteger(Parse p, CExpression e) {
    if (!e.getResultType().isIntegerType()) {
      p.perror("exprect integer expression, but was: " + e.toString());
    }
  }

  private static CType ipromote(Parse p, CExpression e) {
    assertIsInteger(p, e);

    CType res = e.getResultType();

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

  public static CType applyBinaryUsualArithConv(Parse p, Token token, CExpression lhs, CExpression rhs) {
    NullChecker.check(lhs.getResultType());
    NullChecker.check(rhs.getResultType());

    // TODO:
    if (token.ofType(T.T_AND_AND) || token.ofType(T.T_OR_OR)) {
      return TYPE_INT;
    }

    assertIsArithmetic(p, lhs);
    assertIsArithmetic(p, rhs);

    CType res = null;
    CType lhstype = lhs.getResultType();
    CType rhstype = rhs.getResultType();

    if (lhstype.isLongDouble() || rhstype.isLongDouble()) {
      res = TYPE_LONG_DOUBLE;
    } else if (lhstype.isDouble() || rhstype.isDouble()) {
      res = TYPE_DOUBLE;
    } else if (lhstype.isFloat() || rhstype.isFloat()) {
      res = TYPE_FLOAT;
    } else {
      CType prom_1 = ipromote(p, lhs);
      CType prom_2 = ipromote(p, rhs);

      if (prom_1.getSize() > prom_2.getSize()) {
        res = prom_1;
      } else if (prom_2.getSize() > prom_1.getSize()) {
        res = prom_2;
      } else {
        if (prom_1.isUnsigned()) {
          res = prom_1;
        } else {
          res = prom_2;
        }
      }
    }

    NullChecker.check(res);

    return res;
  }

  public static void applyType(CExpression e, Parse parser) {
    applyTypeInternal(e, parser);
    assertResultType(e);
  }

  private static void applyTypeInternal(CExpression e, Parse parser) {
    if (e.getResultType() != null) {
      return;
    }
    CExpressionBase base = e.getBase();
    if (base == CExpressionBase.EUNARY) {
      applyTypeInternal(e.getOperand(), parser);
      e.setResultType(e.getOperand().getResultType()); // TODO:
    }

    //    else if (base == CExpressionBase.EBINARY) {
    //      applyTypeInternal(e.getLhs(), parser);
    //      applyTypeInternal(e.getRhs(), parser);
    //      e.setResultType(e.getLhs().getResultType());
    //    }

    else if (base == CExpressionBase.ETERNARY) {
      applyTypeInternal(e.getCnd(), parser);
      applyTypeInternal(e.getLhs(), parser);
      applyTypeInternal(e.getRhs(), parser);
      e.setResultType(e.getRhs().getResultType()); // TODO:
    }

    else if (base == CExpressionBase.EPRIMARY_CONST) {
      final NumType numtype = e.getCnumber().getNumtype();
      e.setResultType(bindings.get(numtype));
    }

    else if (base == CExpressionBase.EPRIMARY_IDENT) {
      e.setResultType(e.getSymbol().getType());
    }

    else if (base == CExpressionBase.ECAST) {
      e.setResultType(e.getTypename());
    }

    else if (base == CExpressionBase.ECOMMA) {
      applyTypeInternal(e.getLhs(), parser);
      applyTypeInternal(e.getRhs(), parser);
      e.setResultType(e.getRhs().getResultType());
    }

    else if (base == CExpressionBase.ESUBSCRIPT) {
      applyTypeInternal(e.getLhs(), parser);
      applyTypeInternal(e.getRhs(), parser);
      e.setResultType(e.getLhs().getResultType());
    }

    else if (base == CExpressionBase.EFCALL) {
      applyTypeInternal(e.getLhs(), parser);
      e.setResultType(e.getLhs().getResultType());
    }

    else {
      parser.perror("unimpl." + base.toString());
    }
  }

}
