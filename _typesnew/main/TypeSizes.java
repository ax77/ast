package ast._typesnew.main;

import java.util.HashMap;
import java.util.Map;

public abstract class TypeSizes {

  //@formatter:off
  private static Map<TypeKind, Integer> BSIZES = new HashMap<TypeKind, Integer>();
  static {
    BSIZES.put(TypeKind.TP_VOID           , 1);
    BSIZES.put(TypeKind.TP_BOOL           , 1);
    BSIZES.put(TypeKind.TP_CHAR           , 1);
    BSIZES.put(TypeKind.TP_UCHAR          , 1);
    BSIZES.put(TypeKind.TP_SHORT          , 2);
    BSIZES.put(TypeKind.TP_USHORT         , 2);
    BSIZES.put(TypeKind.TP_INT            , 4);
    BSIZES.put(TypeKind.TP_UINT           , 4);
    BSIZES.put(TypeKind.TP_LONG           , 8);
    BSIZES.put(TypeKind.TP_ULONG          , 8);
    BSIZES.put(TypeKind.TP_LONG_LONG      , 8);
    BSIZES.put(TypeKind.TP_ULONG_LONG     , 8);
    BSIZES.put(TypeKind.TP_FLOAT          , 4);
    BSIZES.put(TypeKind.TP_DOUBLE         , 8);
    BSIZES.put(TypeKind.TP_LONG_DOUBLE    , 16);
    
    BSIZES.put(TypeKind.TP_FUNCTION       , 1);
    BSIZES.put(TypeKind.TP_ENUM           , 4);
    BSIZES.put(TypeKind.TP_POINTER_TO     , 8);
  }
  //@formatter:on

  public static int get(TypeKind b) {
    return BSIZES.get(b);
  }
}