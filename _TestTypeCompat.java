package ast;

import org.junit.Test;

import ast._typesnew.CArrayType;
import ast._typesnew.CEnumType;
import ast._typesnew.CFunctionType;
import ast._typesnew.CPointerType;
import ast._typesnew.CStructType;
import ast._typesnew.CType;

public class _TestTypeCompat {

  private boolean isTypeCompatible(CType lhs, CType rhs) {
    if (lhs.getKind() != rhs.getKind()) {
      return false;
    }
    if (lhs.isPointer()) {
      return isPointerCompatible(lhs.getTpPointer(), rhs.getTpPointer());
    }
    if (lhs.isArray()) {
      return isArrayCompatible(lhs.getTpArray(), rhs.getTpArray());
    }
    if (lhs.isFunction()) {
      return isFunctionCompatible(lhs.getTpFunction(), rhs.getTpFunction());
    }
    if (lhs.isStrUnion()) {
      return isStructCompatible(lhs.getTpStruct(), rhs.getTpStruct());
    }
    if (lhs.isEnumeration()) {
      return isEnumCompatible(lhs.getTpEnum(), rhs.getTpEnum());
    }
    return true;
  }

  private boolean isEnumCompatible(CEnumType lhs, CEnumType rhs) {
    // TODO Auto-generated method stub
    return false;
  }

  private boolean isStructCompatible(CStructType lhs, CStructType rhs) {
    // TODO Auto-generated method stub
    return false;
  }

  private boolean isFunctionCompatible(CFunctionType lhs, CFunctionType rhs) {
    // TODO Auto-generated method stub
    return false;
  }

  private boolean isPointerCompatible(CPointerType lhs, CPointerType rhs) {
    // TODO Auto-generated method stub
    return false;
  }

  private boolean isArrayCompatible(CArrayType lhs, CArrayType rhs) {
    // TODO Auto-generated method stub
    return false;
  }

  @Test
  public void test() {
  }

}
