package ast._typesnew;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jscan.cstrtox.NumType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast._typesnew.main.TypeSizes;
import ast._typesnew.util.TypePrinter;
import ast.parse.ParseException;

public class CType implements CTypeApi {

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

  public static final int FINLIN = 1 << 0;
  public static final int FNORET = 1 << 1;

  private final TypeKind kind;
  private final StorageKind storage;
  private int qualifiers;
  private int funcspecs;
  private final int size;
  private final int align;

  private CType pointerTo;
  private CArrayType tpArray;
  private CFunctionType tpFunction;
  private CStructType tpStruct;
  private CEnumType tpEnum;
  private CBitfieldType tpBitfield;

  public void applyTqual(int f) {
    qualifiers |= f;
  }

  public void applyFspec(int f) {
    funcspecs |= f;
  }

  // for primitives
  public CType(TypeKind kind, StorageKind storage) {
    this.kind = kind;
    this.size = TypeSizes.get(kind);
    this.align = this.size;
    this.storage = storage;
  }

  public CType(CType pointerTo, StorageKind storage) {
    this.kind = TypeKind.TP_POINTER_TO;
    this.pointerTo = pointerTo;
    this.size = TypeSizes.get(TypeKind.TP_POINTER_TO);
    this.align = this.size;
    this.storage = storage;
  }

  public CType(CFunctionType cFunctionType, StorageKind storage) {
    this.kind = TypeKind.TP_FUNCTION;
    this.tpFunction = cFunctionType;
    this.size = TypeSizes.get(TypeKind.TP_FUNCTION);
    this.align = this.size;
    this.storage = storage;
  }

  public CType(CArrayType cArrayType, StorageKind storage) {
    this.kind = TypeKind.TP_ARRAY_OF;
    this.tpArray = cArrayType;
    this.size = cArrayType.getArrayLen() * cArrayType.getArrayOf().getSize();
    this.align = cArrayType.getArrayOf().getAlign();
    this.storage = storage;
  }

  public CType(CStructType tpStruct, int size, int align, StorageKind storage) {
    this.kind = (tpStruct.isUnion() ? TypeKind.TP_UNION : TypeKind.TP_STRUCT);
    this.tpStruct = tpStruct;
    this.size = size;
    this.align = align;
    this.storage = storage;
  }

  public CType(CEnumType tpEnum, StorageKind storage) {
    this.kind = TypeKind.TP_ENUM;
    this.tpEnum = tpEnum;
    this.size = TypeSizes.get(TypeKind.TP_ENUM);
    this.align = this.size;
    this.storage = storage;
  }

  // when we copy the type, building it from typedef name,
  // we apply to this new type all qualifiers we have:
  // typedef int i32;
  // static const i32 x;
  // x is int, and static and const
  //
  public CType(CType from, StorageKind storage, int qualifiers, int funcspecs) {
    this.kind = from.kind;
    this.size = from.size;
    this.align = from.align;
    this.tpArray = from.tpArray;
    this.tpBitfield = from.tpBitfield;
    this.tpEnum = from.tpEnum;
    this.tpFunction = from.tpFunction;
    this.pointerTo = from.pointerTo;
    this.tpStruct = from.tpStruct;
    this.storage = storage;
    this.qualifiers = qualifiers;
    this.funcspecs = funcspecs;
  }

  public CType(CBitfieldType tpBitfield) {
    this.kind = TypeKind.TP_BITFIELD;
    this.tpBitfield = tpBitfield;

    //TODO:
    this.size = tpBitfield.getBase().getSize();
    this.align = 1;
    this.storage = StorageKind.ST_NONE;
  }

  private void assertGetType(TypeKind need) {
    if (need != kind) {
      throw new ParseException("internal error: you want get type " + need.toString() + " from " + kind.toString());
    }
  }

  public boolean isPrimitive() {
    switch (kind) {
    case TP_POINTER_TO:
    case TP_ARRAY_OF:
    case TP_FUNCTION:
    case TP_STRUCT:
    case TP_ENUM:
    case TP_UNION:
    case TP_BITFIELD:
      return false;
    default:
      return true;
    }
  }

  public TypeKind getKind() {
    return kind;
  }

  public StorageKind getStorage() {
    return storage;
  }

  public CType getPointerTo() {
    assertGetType(TypeKind.TP_POINTER_TO);
    return pointerTo;
  }

  public CArrayType getTpArray() {
    assertGetType(TypeKind.TP_ARRAY_OF);
    return tpArray;
  }

  public CFunctionType getTpFunction() {
    assertGetType(TypeKind.TP_FUNCTION);
    return tpFunction;
  }

  public CStructType getTpStruct() {
    if (!isStrUnion()) {
      throw new ParseException("you want get fields from something not a struct or union.");
    }
    return tpStruct;
  }

  public CEnumType getTpEnum() {
    assertGetType(TypeKind.TP_ENUM);
    return tpEnum;
  }

  public int chainLength() {
    int r = 0;
    if (isPrimitive()) {
      r++;
    } else {
      if (kind == TypeKind.TP_POINTER_TO) {
        r++;
        r += pointerTo.chainLength();
      }
      if (kind == TypeKind.TP_ARRAY_OF) {
        r++;
        r += tpArray.getArrayOf().chainLength();
      }
      if (kind == TypeKind.TP_FUNCTION) {
        r++;
        r += tpFunction.getReturnType().chainLength();
      }
    }
    return r;
  }

  @Override
  public String toString() {
    if (isPrimitive()) {
      return TypePrinter.primitiveToString(kind);
    }
    if (isBitfield()) {
      return tpBitfield.toString();
    } else if (kind == TypeKind.TP_POINTER_TO) {
      return "pointer_to(" + pointerTo.toString() + ")";
    } else if (kind == TypeKind.TP_ARRAY_OF) {
      return tpArray.toString();
    } else if (kind == TypeKind.TP_FUNCTION) {
      return tpFunction.toString();
    } else if (isStruct()) {
      return tpStruct.toString();
    } else if (isEnumeration()) {
      return tpEnum.toString();
    } else {
      throw new ParseException("Unknown type: " + kind.toString());
    }
  }

  public CType getArrayElementType() {
    assertGetType(TypeKind.TP_ARRAY_OF);
    return tpArray.getArrayOf();
  }

  public CType getFunctionRetElementType() {
    assertGetType(TypeKind.TP_FUNCTION);
    return tpFunction.getReturnType();
  }

  public boolean isStrUnion() {
    return isStruct() || isUnion();
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public int getAlign() {
    return align;
  }

  @Override
  public boolean isUnion() {
    return kind == TypeKind.TP_UNION;
  }

  @Override
  public boolean isFunction() {
    return kind == TypeKind.TP_FUNCTION;
  }

  @Override
  public boolean isObject() {
    return isScalar() || isNoScalar();
  }

  @Override
  public boolean isScalar() {
    return isPointer() || isArithmetic();
  }

  @Override
  public boolean isNoScalar() {
    return isStruct() || isUnion() || isArray();
  }

  @Override
  public boolean isStruct() {
    return kind == TypeKind.TP_STRUCT;
  }

  @Override
  public boolean isArray() {
    return kind == TypeKind.TP_ARRAY_OF;
  }

  @Override
  public boolean isArithmetic() {
    return isInteger() || isFloatingType();
  }

  @Override
  public boolean isInteger() {
    return isBool()
        || isChar()
        || isUchar()
        || isShort()
        || isUshort()
        || isInt()
        || isUint()
        || isLong()
        || isUlong()
        || isLongLong()
        || isUlongLong()
        //
        || isBitfield()
        || isEnumeration();
  }

  @Override
  public boolean isBitfield() {
    return kind == TypeKind.TP_BITFIELD;
  }

  @Override
  public boolean isPlainBitfield() {
    return isBitfield();
  }

  @Override
  public boolean isSignedBitfield() {
    return isBitfield() && !tpBitfield.getBase().isUnsigned();
  }

  @Override
  public boolean isUnsignedBitfield() {
    return isBitfield() && tpBitfield.getBase().isUnsigned();
  }

  @Override
  public boolean isEnumeration() {
    return kind == TypeKind.TP_ENUM;
  }

  @Override
  public boolean isFloatingType() {
    return isFloat() || isDouble() || isLongDouble();
  }

  @Override
  public boolean isPointer() {
    return kind == TypeKind.TP_POINTER_TO;
  }

  @Override
  public boolean isPointerToFunction() {
    return isPointer() && pointerTo.isFunction();
  }

  @Override
  public boolean isPointerToObject() {
    return isPointer() && pointerTo.isObject();
  }

  @Override
  public boolean isPointerToIncomplete() {
    return isPointer() && pointerTo.isIncomplete();
  }

  @Override
  public boolean isCanBeIncomplete() {
    return isArray() || isStruct() || isUnion() || isVoid();
  }

  @Override
  public boolean isVoid() {
    return kind == TypeKind.TP_VOID;
  }

  @Override
  public boolean isIncompleteStruct() {
    return isStruct() && tpStruct.isIncomplete();
  }

  @Override
  public boolean isIncompleteUnion() {
    return isUnion() && tpStruct.isIncomplete();
  }

  @Override
  public boolean isIncompleteArray() {
    return isArray() && tpArray.isIncomplete();
  }

  @Override
  public boolean isIncomplete() {
    return isCanBeIncomplete() && (isIncompleteArray() || isIncompleteStruct() || isIncompleteUnion() || isVoid());
  }

  @Override
  public boolean isEqualTo(CType another) {
    if (kind != another.getKind()) {
      return false;
    }
    if (isPointer()) {
      return cmpPointers(another);
    }
    if (isFunction()) {
      return cmpFunctions(another);
    }
    return true;
  }

  private boolean cmpPointers(CType another) {
    final CType rhs = another.getPointerTo();
    if (!pointerTo.isEqualTo(rhs)) {
      return false;
    }
    return true;
  }

  private boolean cmpFunctions(CType another) {
    final CFunctionType anotherFn = another.getTpFunction();
    final CType lhsRtype = tpFunction.getReturnType();
    final CType rhsRtype = anotherFn.getReturnType();
    if (!lhsRtype.isEqualTo(rhsRtype)) {
      return false;
    }
    if (tpFunction.isVariadic()) {
      if (!anotherFn.isVariadic()) {
        return false;
      }
    }
    final List<CFuncParam> lhsParams = tpFunction.getParameters();
    final List<CFuncParam> rhsParams = anotherFn.getParameters();
    if (lhsParams.size() != rhsParams.size()) {
      return false;
    }
    for (int i = 0; i < lhsParams.size(); ++i) {
      CFuncParam lhsParam = lhsParams.get(i);
      CFuncParam rhsParam = rhsParams.get(i);
      if (!lhsParam.getType().isEqualTo(rhsParam.getType())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isConst() {
    if (isStrUnion()) {
      return tpStruct.isHasConstFields();
    }
    return (qualifiers & QCONST) == QCONST;
  }

  @Override
  public boolean isHasSignedness() {
    return isInteger();
  }

  @Override
  public boolean isUnsigned() {
    return isUchar() || isUshort() || isUint() || isUlong() || isUlongLong() || isUnsignedBitfield();
  }

  @Override
  public boolean isSigned() {
    return isHasSignedness() && !isUnsigned();
  }

  @Override
  public boolean isLvalue() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isModifiableLvalue() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isStatic() {
    return storage == StorageKind.ST_STATIC;
  }

  @Override
  public boolean isExtern() {
    return storage == StorageKind.ST_EXTERN;
  }

  @Override
  public boolean isInline() {
    return (funcspecs & FINLIN) == FINLIN;
  }

  @Override
  public boolean isNoreturn() {
    return (funcspecs & FNORET) == FNORET;
  }

//@formatter:off
  @Override public boolean isBool() { return kind == TypeKind.TP_BOOL; }
  @Override public boolean isChar() { return kind == TypeKind.TP_CHAR; }
  @Override public boolean isUchar() { return kind == TypeKind.TP_UCHAR; }
  @Override public boolean isShort() { return kind == TypeKind.TP_SHORT; }
  @Override public boolean isUshort() { return kind == TypeKind.TP_USHORT; }
  @Override public boolean isInt() { return kind == TypeKind.TP_INT; }
  @Override public boolean isUint() { return kind == TypeKind.TP_UINT; }
  @Override public boolean isLong() { return kind == TypeKind.TP_LONG; }
  @Override public boolean isUlong() { return kind == TypeKind.TP_ULONG; }
  @Override public boolean isLongLong() { return kind == TypeKind.TP_LONG_LONG; }
  @Override public boolean isUlongLong() { return kind == TypeKind.TP_ULONG_LONG; }
  @Override public boolean isFloat() { return kind == TypeKind.TP_FLOAT; }
  @Override public boolean isDouble() { return kind == TypeKind.TP_DOUBLE; }
  @Override public boolean isLongDouble() { return kind == TypeKind.TP_LONG_DOUBLE; }
//@formatter:on

  public boolean isPointerToCompat(CType lhsRT) {
    // TODO Auto-generated method stub
    return false;
  }

}
