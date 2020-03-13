package ast.parse;

import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_EXCLAMATION;
import static jscan.tokenize.T.T_MINUS;
import static jscan.tokenize.T.T_PLUS;
import static jscan.tokenize.T.T_TILDE;
import static jscan.tokenize.T.T_TIMES;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._entry.IdentMap;

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
  

  // & * + - ~ !
  public static boolean isUnaryOperator(Token what) {
    return what.ofType(T_AND) 
        || what.ofType(T_TIMES)
        || what.ofType(T_PLUS)
        || what.ofType(T_MINUS) 
        || what.ofType(T_TILDE)
        || what.ofType(T_EXCLAMATION);
  }
  
  public static boolean isStorageClassSpec(Token what) {
    return what.isIdent(IdentMap.static_ident) 
        || what.isIdent(IdentMap.extern_ident)
        || what.isIdent(IdentMap.auto_ident)
        || what.isIdent(IdentMap.register_ident)
        || what.isIdent(IdentMap.typedef_ident)
        ;
  }
  
  public static boolean isTypeSpec(Token what) {
    return what.isIdent(IdentMap.void_ident)
        || what.isIdent(IdentMap.char_ident)
        || what.isIdent(IdentMap.short_ident)
        || what.isIdent(IdentMap.int_ident)
        || what.isIdent(IdentMap.long_ident)
        || what.isIdent(IdentMap.float_ident)
        || what.isIdent(IdentMap.double_ident)
        || what.isIdent(IdentMap.signed_ident)
        || what.isIdent(IdentMap.unsigned_ident)
        || what.isIdent(IdentMap._Bool_ident)
        || what.isIdent(IdentMap._Complex_ident)
        ;
  }
  
  public static boolean isConstIdent(Token what) {
    return what.isIdent(IdentMap.const_ident)
        || what.isIdent(IdentMap.__const___ident)
        || what.isIdent(IdentMap.__const_ident);
  }
  
  public static boolean isVolatileIdent(Token what) {
    return what.isIdent(IdentMap.volatile_ident)
        || what.isIdent(IdentMap.__volatile___ident)
        || what.isIdent(IdentMap.__volatile_ident);
  }
  
  private static boolean isRestrictIdent(Token what) {
    return what.isIdent(IdentMap.restrict_ident)
        || what.isIdent(IdentMap.__restrict___ident)
        || what.isIdent(IdentMap.__restrict_ident);
  }
  
  public static boolean isTypeQual(Token what) {
    return isConstIdent(what)
        || isVolatileIdent(what)
        || isRestrictIdent(what);
  }
  
  public static boolean isInlineIdent(Token what) {
    return what.isIdent(IdentMap.inline_ident) 
        || what.isIdent(IdentMap.__inline_ident) 
        || what.isIdent(IdentMap.__inline___ident);
  }
  
  public static boolean isFuncSpec(Token what) {
    return isInlineIdent(what)
        || isNoreturnIdent(what);
  }

  public static boolean isNoreturnIdent(Token what) {
    return what.isIdent(IdentMap._Noreturn_ident);
  }
  
  public static boolean isEnumSpecStart(Token what) {
    return what.isIdent(IdentMap.enum_ident);
  }
  
  public static boolean isStructOrUnionSpecStart(Token what) {
    return what.isIdent(IdentMap.struct_ident) 
        || what.isIdent(IdentMap.union_ident);
  }
  
  public static boolean isAttributeStartGnuc(Token what) {
    return what.isIdent(IdentMap.__attribute___ident)
        || what.isIdent(IdentMap.__attribute_ident);
  }
  
  public static boolean isAsmStart(Token what) {
    return what.isIdent(IdentMap.asm_ident)
        || what.isIdent(IdentMap.__asm___ident)
        || what.isIdent(IdentMap.__asm_ident);
  }
  
  public static boolean isStaticAssert(Token what) {
    return what.isIdent(IdentMap._Static_assert_ident);
  }
}
