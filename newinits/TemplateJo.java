package ast.newinits;

import java.util.ArrayList;
import java.util.List;

import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CArrayType;
import ast._typesnew.CType;
import ast.declarations.Initializer;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.symtabg.elements.CSymbol;

public class TemplateJo {

  private final CSymbol sym;
  private final Token from;
  private final CExpression zero;
  private final List<JustOut> outlist;

  private int oneElementOffset;

  public TemplateJo(CSymbol sym) {
    if (!sym.isArray()) {
      throw new ParseException("expect array");
    }
    if (sym.getInitializer() == null) {
      throw new ParseException("expect initializer");
    }

    SizeFinder.buildArraySize(sym);
    this.sym = sym;

    from = sym.getFrom();
    zero = CExpressionBuilderHelper.digitZero(from);

    outlist = new ArrayList<JustOut>();
    buildIndices(sym.getInitializer());

    // remove outer {  };
    outlist.remove(0);
    outlist.remove(outlist.size() - 1);

    this.oneElementOffset = -1;

    g();
  }

  private List<JustOut> gTemplateFromText() {
    List<Integer> dimensions = new ArrayList<Integer>();
    buildArrayDimensions(sym.getType(), dimensions);

    String templateTextual = TemplateTextual.gTemplate(dimensions);
    List<JustOut> template = new ArrayList<JustOut>();

    for (int i = 0; i < templateTextual.length(); i++) {
      char c = templateTextual.charAt(i);
      if (c == ' ') {
        continue;
      }
      if (c == '{') {
        template.add(joLbr());
      } else if (c == '}') {
        template.add(joRbr());
      } else {
        template.add(joZero());
      }
    }
    return template;
  }

  public void g() {
    List<JustOut> template = gTemplateFromText();

    // fill leading '{'
    int lbrStartTemplate = startLbrCount(template);
    int lbrStartReal = startLbrCount(outlist);
    if (lbrStartTemplate > lbrStartReal) {
      int diff = lbrStartTemplate - lbrStartReal;
      for (int j = 0; j < diff; j++) {
        outlist.add(0, joLbr());
      }
    }

    // NOTE: your __ALWAYS__ have open and closed braces
    // even if init-list is empty, template __NEVER__ empty
    // so: 
    // int arr[1][2][3] = {     };
    // template: { { 0 0 0 } { 0 0 0 } } 
    // outlist : { { } } 

    for (int indexOf = 0; indexOf < template.size(); indexOf++) {
      expandOneIndex(template, indexOf);
    }

    // fill trailing '}'
    int lbrEndTemplate = endLbrCount(template);
    int lbrEndReal = endLbrCount(outlist);
    if (lbrEndTemplate > lbrEndReal) {
      int diff = lbrEndTemplate - lbrEndReal;
      for (int j = 0; j < diff; j++) {
        outlist.add(joRbr());
      }
    }
  }

  private void buildArrayDimensions(CType typeGiven, List<Integer> dimensions) {
    if (!typeGiven.isArray()) {
      if (dimensions.isEmpty()) {
        throw new ParseException("zero sized array");
      }
      oneElementOffset = typeGiven.getSize();
      return;
    }
    final CArrayType arr = typeGiven.getTpArray();
    if (arr.getArrayLen() <= 0) {
      throw new ParseException("array length must be positive non-zero numeric constant");
    }
    dimensions.add(arr.getArrayLen());
    buildArrayDimensions(arr.getArrayOf(), dimensions);
  }

  private void buildIndices(Initializer initializer) {

    if (!initializer.isInitializerList()) {
      outlist.add(new JustOut(null, initializer.getAssignment()));
    }

    else {

      outlist.add(joLbr());

      final List<InitializerListEntry> initializers = initializer.getInitializerList();
      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        buildIndices(entry.getInitializer());
      }

      outlist.add(joRbr());

    }
  }

  private Token rbr() {
    return CExpressionBuilderHelper.copyTokenAddNewType(from, T.T_RIGHT_BRACE, "}");
  }

  private Token lbr() {
    return CExpressionBuilderHelper.copyTokenAddNewType(from, T.T_LEFT_BRACE, "{");
  }

  private JustOut joRbr() {
    return new JustOut(rbr(), null);
  }

  private JustOut joLbr() {
    return new JustOut(lbr(), null);
  }

  private JustOut joZero() {
    return new JustOut(null, zero);
  }

  private int startLbrCount(List<JustOut> where) {
    int c = 0;
    for (JustOut jo : where) {
      if (!jo.isOpen()) {
        break;
      }
      ++c;
    }
    return c;
  }

  private int endLbrCount(List<JustOut> where) {
    int c = 0;
    for (int j = where.size(); --j >= 0;) {
      if (!where.get(j).isClose()) {
        break;
      }
      ++c;
    }
    return c;
  }

  private int countInits(List<JustOut> where, int fromIndex) {
    int c = 0;
    for (int j = fromIndex; j < where.size(); j++) {
      JustOut jo = where.get(j);
      if (jo.isOpen() || jo.isClose()) {
        break;
      }
      ++c;
    }
    return c;
  }

  private List<JustOut> cutBraces(List<JustOut> template, int idxset) {
    List<JustOut> r = new ArrayList<JustOut>();
    for (int j = idxset; j < template.size(); j++) {
      JustOut jo = template.get(j);
      if (jo.isClose()) {
        r.add(jo);
      } else {
        break;
      }
    }
    return r;
  }

  private void addExpand(List<JustOut> where, JustOut what, int indexOf) {
    if (where.size() <= indexOf) {
      where.add(what);
    } else {
      where.add(indexOf, what);
    }
  }

  private void checkOpen(int indexOf) {
    if (outlist.size() <= indexOf || !outlist.get(indexOf).isOpen()) {
      addExpand(outlist, joLbr(), indexOf);
    }
  }

  private void fillClosedBraces(List<JustOut> template, int indexOf) {
    List<JustOut> bracesAfter = cutBraces(template, indexOf);
    for (JustOut joBrace : bracesAfter) {
      if (outlist.size() > indexOf && outlist.get(indexOf).isClose() && joBrace.isClose()) {
      } else {
        addExpand(outlist, joBrace, indexOf);
      }
    }
  }

  private void expandOneIndex(List<JustOut> template, int indexOf) {
    JustOut cur = template.get(indexOf);
    JustOut nex = null;
    if (indexOf + 1 <= template.size() - 1) {
      nex = template.get(indexOf + 1);
    }
    if (cur.isOpen()) {
      checkOpen(indexOf);
      // { { 0 0 0 } { 0 0 0 } }
      // ^.^
      if (nex != null && nex.isOpen()) {
        checkOpen(indexOf);
        return;
      }
    }

    // NOTE: pos now is '{'
    // search index == indexOf+1

    indexOf += 1;

    int initsInTemplate = countInits(template, indexOf);
    int initsInOutlist = countInits(outlist, indexOf);

    // I)
    //
    if (initsInTemplate < initsInOutlist) {
      // move index
      for (int j = 0; j < initsInTemplate; j++) {
        indexOf += 1;
      }

      fillClosedBraces(template, indexOf);
    }

    // II)
    //
    else if (initsInTemplate > initsInOutlist) {
      indexOf += initsInOutlist; // ?

      // fill with '0'
      int diff = initsInTemplate - initsInOutlist;
      for (int j = 0; j < diff; j++) {
        addExpand(outlist, joZero(), indexOf);
        indexOf += 1;
      }

      fillClosedBraces(template, indexOf);

    }

    // III) 
    //
    else {
      fillClosedBraces(template, indexOf);
    }

  }

  public List<JustOut> getOutlist() {
    return outlist;
  }

}
