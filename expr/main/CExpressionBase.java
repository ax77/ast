package ast.expr.main;

//@formatter:off
public enum CExpressionBase {
   EASSIGN
 , EBINARY
 , ECOMMA
 , ETERNARY
 , EUNARY
 , EPRIMARY_IDENT
 , EPRIMARY_CONST
 , EPRIMARY_STRING
 , EPRIMARY_GENERIC
 , ECOMPSEL // . ->
 , ECAST
 , EFCALL
 , EPREINCDEC
 , EPOSTINCDEC
 , ECOMPLITERAL
}
