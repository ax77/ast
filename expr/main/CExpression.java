package ast.expr.main;

import static ast.expr.main.CExpressionBase.EASSIGN;
import static ast.expr.main.CExpressionBase.EBINARY;
import static ast.expr.main.CExpressionBase.ECAST;
import static ast.expr.main.CExpressionBase.ECOMMA;
import static ast.expr.main.CExpressionBase.ECOMPSEL;
import static ast.expr.main.CExpressionBase.EFCALL;
import static ast.expr.main.CExpressionBase.EPOSTINCDEC;
import static ast.expr.main.CExpressionBase.EPREINCDEC;
import static ast.expr.main.CExpressionBase.EPRIMARY_GENERIC;
import static ast.expr.main.CExpressionBase.ETERNARY;
import static ast.expr.main.CExpressionBase.EUNARY;

import java.util.List;

import jscan.cstrtox.C_strtox;
import jscan.sourceloc.SourceLocation;
import jscan.tokenize.Token;
import ast._typesnew.CStructField;
import ast._typesnew.CType;
import ast.declarations.InitializerList;
import ast.parse.ILocation;
import ast.parse.NodeTemp;
import ast.parse.ParseException;
import ast.symtabg.elements.CSymbol;
import ast.symtabg.elements.NumericConstant;

public class CExpression implements ILocation {

  private static final int LHS_INDEX = 0;
  private static final int RHS_INDEX = 1;
  private static final int CND_INDEX = 2;

  // TODO: location

  private CExpressionBase base; // what union contains
  private final long tname; // just unique id. for codegen.
  private final SourceLocation location;

  private CType resultType; // what expression doe's after evaluation

  private final Token token; // operator, position
  private final CExpression tree[]; // unary, binary, assign, array-subscript

  private CType typename; // cast lhs to typename
  private InitializerList initializerList; // (typename) { initializer-list } compound literal
  private CStructField fieldName; //  field name (compsel)
  private CSymbol symbol; // primary ident

  private List<CExpression> arglist; // function-arguments
  private String cstring;
  private NumericConstant cnumber;

  private void assertBaseIsOneOf(CExpressionBase... bases) {
    boolean contains = false;
    for (CExpressionBase b : bases) {
      if (base == b) {
        contains = true;
        break;
      }
    }
    if (!contains) {
      throw new ParseException("you want get tree-node that doe's not exists for this base: " + base.toString());
    }
  }

  public CExpression getLhs() {
    assertBaseIsOneOf(EASSIGN, EBINARY, ETERNARY, ECOMMA, ECAST /*, ESUBSCRIPT*/, EFCALL, EPRIMARY_GENERIC);
    return tree[LHS_INDEX];
  }

  public CExpression getPostfix() {
    assertBaseIsOneOf(ECOMPSEL);
    return tree[LHS_INDEX];
  }

  public CExpression getRhs() {
    assertBaseIsOneOf(EASSIGN, EBINARY, ETERNARY, ECOMMA /*, ESUBSCRIPT*/);
    return tree[RHS_INDEX];
  }

  public CExpression getOperand() {
    assertBaseIsOneOf(EUNARY, EPREINCDEC, EPOSTINCDEC);
    return tree[LHS_INDEX];
  }

  private void setLhs(CExpression e) {
    tree[LHS_INDEX] = e;
  }

  private void setRhs(CExpression e) {
    tree[RHS_INDEX] = e;
  }

  public CExpression getCnd() {
    assertBaseIsOneOf(ETERNARY);
    return tree[CND_INDEX];
  }

  private void setCnd(CExpression condition) {
    tree[CND_INDEX] = condition;
  }

  public CType getTypename() {
    return typename;
  }

  private CExpression[] emptyTree() {
    return new CExpression[3];
  }

  // pre-post inc-dec
  public CExpression(CExpressionBase base, Token op, CExpression lhs) {
    this.base = base;
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(op);
    this.token = op;
    this.tree = emptyTree();

    setLhs(lhs);
  }

  public Token getToken() {
    return token;
  }

  // binary, asssign, comma, array-subscript
  public CExpression(CExpressionBase base, CExpression lhs, CExpression rhs, Token token) {
    this.base = base;
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(token);
    this.token = token;
    this.tree = emptyTree();

    setLhs(lhs);
    setRhs(rhs);
  }

  // unary
  public CExpression(Token op, CExpression lhs) {
    this.base = CExpressionBase.EUNARY;
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(op);
    this.token = op;
    this.tree = emptyTree();

    setLhs(lhs);
  }

  public CExpression(CType typename, InitializerList initializerList, Token token) {
    this.tname = NodeTemp.gettemp();
    this.tree = emptyTree();
    this.location = new SourceLocation(token);
    this.token = token;
    this.base = CExpressionBase.ECOMPLITERAL;
    this.typename = typename;
    this.initializerList = initializerList;
  }

  public CExpression(CExpression function, List<CExpression> arguments, Token token) {
    this.tname = NodeTemp.gettemp();
    this.tree = emptyTree();
    this.location = new SourceLocation(token);
    this.token = token;
    this.base = CExpressionBase.EFCALL;
    setLhs(function);
    this.arglist = arguments;
  }

  public CExpression(CType typename, CExpression tocast, Token token) {
    this.tname = NodeTemp.gettemp();
    this.tree = emptyTree();
    this.location = new SourceLocation(token);
    this.token = token;
    this.base = CExpressionBase.ECAST;
    this.typename = typename;
    setLhs(tocast);
  }

  // (*a) -> x
  // a . x
  public CExpression(CExpression postfis, Token operator, CStructField fieldName) {
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(operator);
    this.tree = emptyTree();
    this.token = operator;
    this.base = CExpressionBase.ECOMPSEL;

    setLhs(postfis);
    this.fieldName = fieldName;
  }

  public CExpression(CExpression condition, CExpression branchTrue, CExpression branchFalse, Token token) {
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(token);
    this.tree = emptyTree();
    this.token = token;
    this.base = CExpressionBase.ETERNARY;

    setCnd(condition);
    setLhs(branchTrue);
    setRhs(branchFalse);
  }

  public CExpression(CSymbol e, Token token) {
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(token);
    this.tree = emptyTree();
    this.token = token;
    this.base = CExpressionBase.EPRIMARY_IDENT;
    this.symbol = e;
  }

  public CExpression(C_strtox e, Token token) {
    e.ev(); // XXX:

    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(token);
    this.tree = emptyTree();
    this.token = token;
    this.base = CExpressionBase.EPRIMARY_CONST;

    NumericConstant number = null;
    if (e.isIntegerKind()) {
      number = new NumericConstant(e.getClong(), e.getNumtype());
    } else {
      number = new NumericConstant(e.getCdouble(), e.getNumtype());
    }

    this.cnumber = number;
  }

  public CExpression(String cstring, Token token) {
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(token);
    this.tree = emptyTree();
    this.token = token;
    this.base = CExpressionBase.EPRIMARY_STRING;
    this.cstring = cstring;
  }

  public CExpression(CExpression genericSelectionResult, Token token) {
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(token);
    this.tree = emptyTree();
    this.token = token;
    this.base = CExpressionBase.EPRIMARY_GENERIC;

    setLhs(genericSelectionResult);
  }

  public CExpression(NumericConstant number, Token from) {
    this.tname = NodeTemp.gettemp();
    this.location = new SourceLocation(from);
    this.tree = emptyTree();
    this.token = from;
    this.base = CExpressionBase.EPRIMARY_CONST;
    this.cnumber = number;
  }

  public CType getResultType() {
    return resultType;
  }

  public void setResultType(CType resultType) {
    this.resultType = resultType;
  }

  public CExpressionBase getBase() {
    return base;
  }

  public void setBase(CExpressionBase base) {
    this.base = base;
  }

  public InitializerList getInitializerList() {
    return initializerList;
  }

  public void setInitializerList(InitializerList initializerList) {
    this.initializerList = initializerList;
  }

  public CStructField getFieldName() {
    return fieldName;
  }

  public CSymbol getSymbol() {
    return symbol;
  }

  public void setSymbol(CSymbol symbol) {
    this.symbol = symbol;
  }

  public List<CExpression> getArglist() {
    return arglist;
  }

  public void setArglist(List<CExpression> arglist) {
    this.arglist = arglist;
  }

  public CExpression getGenericSelectionResult() {
    return getLhs();
  }

  public long getTname() {
    return tname;
  }

  public CExpression[] getTree() {
    return tree;
  }

  public void setTypename(CType typename) {
    this.typename = typename;
  }

  private String tokenTos(Token t) {
    return " " + t.getValue() + " ";
  }

  @Override
  public String toString() {

    switch (base) {
    case EASSIGN: {
      return "(" + getLhs().toString().trim() + tokenTos(getToken()) + getRhs().toString().trim() + ")";
    }
    case EBINARY: {
      return "(" + getLhs().toString() + tokenTos(getToken()) + getRhs().toString() + ")";
    }
    case ECOMMA: {
      return getLhs().toString() + tokenTos(getToken()) + getRhs().toString();
    }
    case ETERNARY: {
      return "("
          + getCnd().toString().trim()
          + " ? "
          + getLhs().toString().trim()
          + " : "
          + getRhs().toString().trim()
          + ")";
    }
    case EUNARY: {
      return "(" + getToken().getValue() + getOperand().toString() + ")";
    }
    case ECOMPSEL: {
      // TODO:
      return "(" + getPostfix().toString() + "." + fieldName.getName().getName() + ")";
    }
    case ECAST: {
      return "(" + typename.toString() + ") " + "(" + getLhs().toString() + ")";
    }
    case EFCALL: {
      StringBuilder sb = new StringBuilder();
      sb.append(getLhs().toString() + "(");

      int argc = 0;
      for (CExpression e : arglist) {
        sb.append(e.toString());
        if (argc < arglist.size() - 1) {
          sb.append(",");
        }
        ++argc;
      }

      sb.append(")");
      return sb.toString();
    }
    case EPREINCDEC: {
      return "(" + getToken().getValue() + getOperand().toString() + ")";
    }
    case EPOSTINCDEC: {
      return "(" + getOperand().toString() + getToken().getValue() + ")";
    }
    case ECOMPLITERAL: {
      return "(" + typename.toString() + ") {" + initializerList.toString() + " }";
    }
    case EPRIMARY_IDENT: {
      return token.getValue(); // TODO: this for unit-tests now.
    }
    case EPRIMARY_CONST: {
      // TODO:
      return String.format("%d", cnumber.getClong());
    }
    case EPRIMARY_STRING: {
      return cstring;
    }
    case EPRIMARY_GENERIC: {
      return getGenericSelectionResult().toString();
    }
    default: {
      throw new ParseException("unknown: " + base.toString());
    }
    }

  }

  public String getCstring() {
    return cstring;
  }

  public void setCstring(String cstring) {
    this.cstring = cstring;
  }

  public NumericConstant getCnumber() {
    return cnumber;
  }

  public void setCnumber(NumericConstant cnumber) {
    this.cnumber = cnumber;
  }

  @Override
  public SourceLocation getLocation() {
    return location;
  }

  @Override
  public String getLocationToString() {
    return location.toString();
  }

  @Override
  public int getLocationLine() {
    return location.getLine();
  }

  @Override
  public int getLocationColumn() {
    return location.getColumn();
  }

  @Override
  public String getLocationFile() {
    return location.getFilename();
  }

  public boolean isIntegerZero() {
    return base == CExpressionBase.EPRIMARY_CONST && cnumber.isInteger() && cnumber.getClong() == 0;
  }

  public boolean isModifiableLvalue() {
    // TODO : XXX
    return true;
  }

}
