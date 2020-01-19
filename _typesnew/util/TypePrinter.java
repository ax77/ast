package ast._typesnew.util;

import java.util.HashMap;
import java.util.Map;

import ast._typesnew.main.TypeKind;

public abstract class TypePrinter {

  private static Map<TypeKind, String> types = new HashMap<TypeKind, String>();
  static {
    //@formatter:off
    types.put(TypeKind.TP_VOID                     , "void                   ");
    types.put(TypeKind.TP_BOOL                     , "_Bool                  ");
    types.put(TypeKind.TP_CHAR                     , "char                   ");
    types.put(TypeKind.TP_UCHAR                    , "unsigned char          ");
    types.put(TypeKind.TP_SHORT                    , "short                  ");
    types.put(TypeKind.TP_USHORT                   , "unsigned short         ");
    types.put(TypeKind.TP_INT                      , "int                    ");
    types.put(TypeKind.TP_UINT                     , "unsigned int           ");
    types.put(TypeKind.TP_LONG                     , "long                   ");
    types.put(TypeKind.TP_ULONG                    , "unsigned long          ");
    types.put(TypeKind.TP_LONG_LONG                , "long long              ");
    types.put(TypeKind.TP_ULONG_LONG               , "unsigned long long     ");
    types.put(TypeKind.TP_FLOAT                    , "float                  ");
    types.put(TypeKind.TP_DOUBLE                   , "double                 ");
    types.put(TypeKind.TP_LONG_DOUBLE              , "long double            ");
    types.put(TypeKind.TP_FLOAT_IMAGINARY          , "float _Imaginary       ");
    types.put(TypeKind.TP_DOUBLE_IMAGINARY         , "double _Imaginary      ");
    types.put(TypeKind.TP_LONG_DOUBLE_IMAGINARY    , "long double _Imaginary ");
    types.put(TypeKind.TP_FLOAT_COMPLEX            , "float _Complex         ");
    types.put(TypeKind.TP_DOUBLE_COMPLEX           , "double _Complex        ");
    types.put(TypeKind.TP_LONG_DOUBLE_COMPLEX      , "long double _Complex   ");
    //@formatter:on
  }

  public static String primitiveToString(TypeKind kind) {
    return types.get(kind).trim();
  }

}
