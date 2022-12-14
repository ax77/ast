package ast.types;

import java.util.HashMap;
import java.util.Map;

import jscan.cstrtox.NumType;
import ast.types.main.CTypeKind;

public class CTypeImpl {
  public static final CType TYPE_VOID = new CType(CTypeKind.TP_VOID);
  public static final CType TYPE_BOOL = new CType(CTypeKind.TP_BOOL);
  public static final CType TYPE_CHAR = new CType(CTypeKind.TP_CHAR);
  public static final CType TYPE_UCHAR = new CType(CTypeKind.TP_UCHAR);
  public static final CType TYPE_SHORT = new CType(CTypeKind.TP_SHORT);
  public static final CType TYPE_USHORT = new CType(CTypeKind.TP_USHORT);
  public static final CType TYPE_INT = new CType(CTypeKind.TP_INT);
  public static final CType TYPE_UINT = new CType(CTypeKind.TP_UINT);
  public static final CType TYPE_LONG = new CType(CTypeKind.TP_LONG);
  public static final CType TYPE_ULONG = new CType(CTypeKind.TP_ULONG);
  public static final CType TYPE_LONG_LONG = new CType(CTypeKind.TP_LONG_LONG);
  public static final CType TYPE_ULONG_LONG = new CType(CTypeKind.TP_ULONG_LONG);
  public static final CType TYPE_FLOAT = new CType(CTypeKind.TP_FLOAT);
  public static final CType TYPE_DOUBLE = new CType(CTypeKind.TP_DOUBLE);
  public static final CType TYPE_LONG_DOUBLE = new CType(CTypeKind.TP_LONG_DOUBLE);

  public static Map<NumType, CType> bindings = new HashMap<NumType, CType>();
  static {
    bindings.put(NumType.N_INT, TYPE_INT);
    bindings.put(NumType.N_UINT, TYPE_UINT);
    bindings.put(NumType.N_LONG, TYPE_LONG);
    bindings.put(NumType.N_ULONG, TYPE_ULONG);
    bindings.put(NumType.N_LONG_LONG, TYPE_LONG_LONG);
    bindings.put(NumType.N_ULONG_LONG, TYPE_ULONG_LONG);
    bindings.put(NumType.N_FLOAT, TYPE_FLOAT);
    bindings.put(NumType.N_DOUBLE, TYPE_DOUBLE);
    bindings.put(NumType.N_LONG_DOUBLE, TYPE_LONG_DOUBLE);
  }

  public static final int QCONST = 1 << 0;
  public static final int QRESTR = 1 << 1;
  public static final int QVOLAT = 1 << 2;
  public static final int FINLIN = 1 << 3;
  public static final int FNORET = 1 << 4;
}
