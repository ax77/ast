package ast.parse;

import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_TILDE;

import jscan.hashed.Hash_ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public abstract class Pcheckers {

  //@formatter:off
  
  public static boolean isAssignOperator(Token what) {
    T tp = what.getType();
    return
           tp == T.T_ASSIGN
        || tp == T.T_TIMES_EQUAL
        || tp == T.T_PERCENT_EQUAL
        || tp == T.T_DIVIDE_EQUAL
        || tp == T.T_PLUS_EQUAL
        || tp == T.T_MINUS_EQUAL
        || tp == T.T_LSHIFT_EQUAL
        || tp == T.T_RSHIFT_EQUAL
        || tp == T.T_AND_EQUAL
        || tp == T.T_XOR_EQUAL
        || tp == T.T_OR_EQUAL;
  }
  

  public static boolean isUnaryOperator(Token what) {
    return what.ofType(T_AND) 
        || what.ofType(T.T_TIMES)
        || what.ofType(T.T_PLUS)
        || what.ofType(T_MINUS) 
        || what.ofType(T_TILDE)
        || what.ofType(T.T_EXCLAMATION);
  }
  
  public static boolean isStorageClassSpec(Token what) {
    return what.isIdent(Hash_ident.static_ident) 
        || what.isIdent(Hash_ident.extern_ident)
        || what.isIdent(Hash_ident.auto_ident)
        || what.isIdent(Hash_ident.register_ident)
        || what.isIdent(Hash_ident.typedef_ident)
        ;
  }
  
  public static boolean isTypeSpec(Token what) {
    return what.isIdent(Hash_ident.void_ident)
        || what.isIdent(Hash_ident.char_ident)
        || what.isIdent(Hash_ident.short_ident)
        || what.isIdent(Hash_ident.int_ident)
        || what.isIdent(Hash_ident.long_ident)
        || what.isIdent(Hash_ident.float_ident)
        || what.isIdent(Hash_ident.double_ident)
        || what.isIdent(Hash_ident.signed_ident)
        || what.isIdent(Hash_ident.unsigned_ident)
        || what.isIdent(Hash_ident._Bool_ident)
        || what.isIdent(Hash_ident._Complex_ident)
        ;
  }
  
  public static boolean isConstIdent(Token what) {
    return what.isIdent(Hash_ident.const_ident)
        || what.isIdent(Hash_ident.__const___ident)
        || what.isIdent(Hash_ident.__const_ident);
  }
  
  public static boolean isVolatileIdent(Token what) {
    return what.isIdent(Hash_ident.volatile_ident)
        || what.isIdent(Hash_ident.__volatile___ident)
        || what.isIdent(Hash_ident.__volatile_ident);
  }
  
  private static boolean isRestrictIdent(Token what) {
    return what.isIdent(Hash_ident.restrict_ident)
        || what.isIdent(Hash_ident.__restrict___ident)
        || what.isIdent(Hash_ident.__restrict_ident);
  }
  
  public static boolean isTypeQual(Token what) {
    return isConstIdent(what)
        || isVolatileIdent(what)
        || isRestrictIdent(what);
  }
  
  public static boolean isInlineIdent(Token what) {
    return what.isIdent(Hash_ident.inline_ident) 
        || what.isIdent(Hash_ident.__inline_ident) 
        || what.isIdent(Hash_ident.__inline___ident);
  }
  
  public static boolean isFuncSpec(Token what) {
    return isInlineIdent(what)
        || isNoreturnIdent(what);
  }

  public static boolean isNoreturnIdent(Token what) {
    return what.isIdent(Hash_ident._Noreturn_ident);
  }
  
  public static boolean isEnumSpecStart(Token what) {
    return what.isIdent(Hash_ident.enum_ident);
  }
  
  public static boolean isStructOrUnionSpecStart(Token what) {
    return what.isIdent(Hash_ident.struct_ident) 
        || what.isIdent(Hash_ident.union_ident);
  }
  
  public static boolean isAttributeStart(Token what) {
    return what.isIdent(Hash_ident.__attribute___ident)
        || what.isIdent(Hash_ident.__attribute_ident);
  }
  
  public static boolean isAsmStart(Token what) {
    return what.isIdent(Hash_ident.asm_ident)
        || what.isIdent(Hash_ident.__asm___ident)
        || what.isIdent(Hash_ident.__asm_ident);
  }
  
  public static boolean isStaticAssert(Token what) {
    return what.isIdent(Hash_ident._Static_assert_ident);
  }
}
