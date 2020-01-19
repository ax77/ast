package ast._typesnew.sem;

import ast._typesnew.CBitfieldType;
import ast._typesnew.CType;
import ast._typesnew.main.TypeKind;
import ast._typesnew.main.TypeSizes;
import ast.parse.Parse;

public class SemanticBitfield {
  private final Parse parser;

  public SemanticBitfield(Parse parser) {
    this.parser = parser;
  }

  public CType buildBitfield(CType base, int width) {

    if (width < 0) {
      parser.perror("negative bitfield-width");
    }

    TypeKind kind = base.getKind();

    //@formatter:off
    boolean isPrimitiveIntegerType = 
           kind == TypeKind.TP_CHAR
        || kind == TypeKind.TP_UCHAR
        || kind == TypeKind.TP_SHORT
        || kind == TypeKind.TP_USHORT
        || kind == TypeKind.TP_INT
        || kind == TypeKind.TP_UINT
        || kind == TypeKind.TP_LONG
        || kind == TypeKind.TP_ULONG
        || kind == TypeKind.TP_LONG_LONG
        || kind == TypeKind.TP_ULONG_LONG;
    //@formatter:on

    // TODO: warning about enum
    // 
    if (!isPrimitiveIntegerType) {
      parser.perror("bitfield type error.");
    }

    int maxbits = TypeSizes.get(kind) * 8; // TODO: settings __CHAR_BIT__
    if (width > maxbits) {
      parser.perror("width exceeds its type");
    }

    CBitfieldType bf = new CBitfieldType(base, width);
    return new CType(bf);
  }

}
