package ast._typesnew;

import java.util.HashMap;
import java.util.Map;

import jscan.cstrtox.NumType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;

public class CTypeImpl {
  //@formatter:off
  public static final CType TYPE_BOOL = new CType(TypeKind.TP_BOOL, StorageKind.ST_NONE);
  public static final CType TYPE_CHAR = new CType(TypeKind.TP_CHAR, StorageKind.ST_NONE);
  public static final CType TYPE_UCHAR = new CType(TypeKind.TP_UCHAR, StorageKind.ST_NONE);
  public static final CType TYPE_SHORT = new CType(TypeKind.TP_SHORT, StorageKind.ST_NONE);
  public static final CType TYPE_USHORT = new CType(TypeKind.TP_USHORT, StorageKind.ST_NONE);
  public static final CType TYPE_INT = new CType(TypeKind.TP_INT, StorageKind.ST_NONE);
  public static final CType TYPE_UINT = new CType(TypeKind.TP_UINT, StorageKind.ST_NONE);
  public static final CType TYPE_LONG = new CType(TypeKind.TP_LONG, StorageKind.ST_NONE);
  public static final CType TYPE_ULONG = new CType(TypeKind.TP_ULONG, StorageKind.ST_NONE);
  public static final CType TYPE_LONG_LONG = new CType(TypeKind.TP_LONG_LONG, StorageKind.ST_NONE);
  public static final CType TYPE_ULONG_LONG = new CType(TypeKind.TP_ULONG_LONG, StorageKind.ST_NONE);
  public static final CType TYPE_FLOAT = new CType(TypeKind.TP_FLOAT, StorageKind.ST_NONE);
  public static final CType TYPE_DOUBLE = new CType(TypeKind.TP_DOUBLE, StorageKind.ST_NONE);
  public static final CType TYPE_LONG_DOUBLE = new CType(TypeKind.TP_LONG_DOUBLE, StorageKind.ST_NONE);
  
  public static final CType VOID_TYPE = new CType(TypeKind.TP_VOID, StorageKind.ST_NONE);
  public static final CType FUNC_DESIGNATOR_TODO_STUB = null; // TODO:
  
  public static Map<NumType, CType> bindings = new HashMap<NumType, CType>();
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

  public static final int QCONST = 1 << 0;
  public static final int QRESTR = 1 << 1;
  public static final int QVOLAT = 1 << 2;
  public static final int FINLIN = 1 << 3;
  public static final int FNORET = 1 << 4;
}
