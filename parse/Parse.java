package ast.parse;

import static jscan.tokenize.T.TOKEN_EOF;
import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_SEMI_COLON;

import java.util.ArrayList;
import java.util.List;

import jscan.Tokenlist;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.errors.ParseErrors;
import ast.errors.ParseException;
import ast.symtab.Symtab;
import ast.symtab.elements.CSymbol;
import ast.symtab.elements.CSymbolBase;
import ast.types.CType;
import ast.types.decl.CDecl;
import ast.types.parser.ParseBase;
import ast.types.parser.ParseDecl;
import ast.types.util.TypeMerger;
import ast.unit.ExternalDeclaration;
import ast.unit.FunctionDefinition;
import ast.unit.TranslationUnit;
import ast.unit.parser.ParseToplevel;

public class Parse {

  // main thing's
  private final Tokenlist tokenlist;
  private Token tok;

  // need for labels, also for binding local variable's
  private FunctionDefinition currentFn;

  // symbol-tables
  private Symtab<Ident, CSymbol> symbols;
  private Symtab<Ident, CSymbol> tags;

  // location, error-handling
  private String lastloc;
  private List<Token> ringBuffer;
  private Token prevtok;

  public Symtab<Ident, CSymbol> getSymbols() {
    return symbols;
  }

  public void setSymbols(Symtab<Ident, CSymbol> symbols) {
    this.symbols = symbols;
  }

  public void setTags(Symtab<Ident, CSymbol> tags) {
    this.tags = tags;
  }

  public Symtab<Ident, CSymbol> getTags() {
    return tags;
  }

  public Token tok() {
    return tok;
  }

  public FunctionDefinition getCurrentFn() {
    return currentFn;
  }

  public void defineSym(Ident key, CSymbol sym) {

    CSymbol prevsym = symbols.getsymFromCurrentScope(key);
    if (prevsym != null) {
      if (prevsym.getBase() == CSymbolBase.SYM_TYPEDEF) {
        if (!prevsym.getType().isEqualTo(sym.getType())) {
          perror("redefinition, previous defined here: " + prevsym.getLocationToString());
        }
      } else {
        perror("redefinition, previous defined here: " + prevsym.getLocationToString());
      }
    }

    if (currentFn != null) {
      currentFn.addLocal(sym);
    }

    symbols.addsym(key, sym);
  }

  public void defineTag(Ident key, CSymbol sym) {
    tags.addsym(key, sym);
  }

  public boolean isHasTag(Ident name) {
    return getTag(name) != null;
  }

  public CSymbol getSym(Ident name) {
    return symbols.getsym(name);
  }

  public CSymbol getTag(Ident name) {
    return tags.getsym(name);
  }

  public boolean isFileScope() {
    return symbols.isFileScope();
  }

  //TODO:SEMANTIC
  //
  public void pushscope() {
    tags.pushscope(".");
    symbols.pushscope(".");
  }

  public void popscope() {
    tags.popscope();
    symbols.popscope();
  }

  //
  // TODO:SEMANTIC

  public Parse(List<Token> tokens) {
    this.tokenlist = new Tokenlist(tokens);

    initDefaults();
    initScopes();
    move();
  }

  public Parse(Tokenlist tokenlist) {
    this.tokenlist = tokenlist;

    initDefaults();
    initScopes();
    move();
  }

  private void initDefaults() {
    this.currentFn = null;
    this.ringBuffer = new ArrayList<Token>(0);
    this.lastloc = "";
  }

  private void initScopes() {
    this.symbols = new Symtab<Ident, CSymbol>();
    this.tags = new Symtab<Ident, CSymbol>();
  }

  public String getLastLoc() {
    return lastloc;
  }

  public Token getPrevtok() {
    return prevtok;
  }

  public void setPrevtok(Token prevtok) {
    this.prevtok = prevtok;
  }

  public List<Token> getRingBuffer() {
    return ringBuffer;
  }

  public T tp() {
    return tok.getType();
  }

  public void move() {

    tok = tokenlist.next();
    if (tok.ofType(T.TOKEN_STREAMBEGIN) || tok.ofType(T.TOKEN_STREAMEND)) {
      tok = tokenlist.next();
    }

    addLoc();
  }

  private void addLoc() {

    if (ringBuffer.size() >= 230) {
      ringBuffer.remove(0);
    }
    ringBuffer.add(tok);

    lastloc = (prevtok == null ? tok.loc() : prevtok.loc());
    prevtok = tok;
  }

  //////////////////////////////////////////////////////////////////////

  public void perror(String m) {

    StringBuilder sb = new StringBuilder();
    sb.append("error: " + m + "\n");
    sb.append("  --> " + lastloc + "\n\n");
    sb.append(ringBufferToStringLines() + "\n");

    throw new ParseException(sb.toString());
  }

  public void pwarning(String m) {

    StringBuilder sb = new StringBuilder();
    sb.append("warning: " + m + "\n");
    sb.append("  --> " + lastloc + "\n\n");
    sb.append(ringBufferToStringLines() + "\n");

    //System.out.println(sb.toString());
  }

  public void perror(ParseErrors code) {
    perror(code.toString());
  }

  public Token checkedMove(Ident expect) {
    if (!tok.isIdent(expect)) {
      perror("expect id: " + expect.getName() + ", but was: " + tok.getValue());
    }
    Token saved = tok();
    move();
    return saved;
  }

  public Ident getIdent() {
    if (!tok.ofType(TOKEN_IDENT)) {
      perror("expect ident, but was: " + tok.getValue());
    }
    Token saved = tok;
    move();
    final Ident ident = saved.getIdent();
    if (ident.isBuiltin()) {
      perror("unexpected builtin ident: " + ident.getName());
    }
    return ident;
  }

  public Token checkedMove(T expect) {
    if (tp() != expect) {
      perror("expect: " + expect.toString() + ", but was: " + tok.getValue());
    }
    Token saved = tok;
    move();
    return saved;
  }

  public boolean moveOptional(T t) {
    if ((tp() == t)) {
      move();
      return true;
    }
    return false;
  }

  public void unexpectedEof() {
    if (tok.ofType(TOKEN_EOF)) {
      perror("EOF unexpected at this context");
    }
  }

  public void unimplemented(String what) {
    perror("unimplemented: " + what);
  }

  public String ringBufferToStringLines() {

    List<List<Token>> lines = new ArrayList<List<Token>>(0);
    List<Token> line = new ArrayList<Token>(0);

    for (Token t : ringBuffer) {
      line.add(t);
      if (t.isNewLine()) {
        lines.add(line);
        line = new ArrayList<Token>(0);
      }
    }
    if (!line.isEmpty()) {
      lines.add(line);
      line = new ArrayList<Token>(0);
    }

    StringBuilder sb = new StringBuilder();

    for (List<Token> oneline : lines) {
      StringBuilder tmp = new StringBuilder();
      boolean first = true;
      for (Token t : oneline) {
        if (first) {
          tmp.append(String.format("%-5d|", t.getRow()));
          first = false;
        }
        if (t.hasLeadingWhitespace()) {
          tmp.append(" ");
        }
        tmp.append(t.getValue());
      }
      tmp.append("\n");
      sb.append(tmp);
    }

    return sb.toString();
  }

  public Token lparen() {
    return checkedMove(T.T_LEFT_PAREN);
  }

  public Token rparen() {
    return checkedMove(T.T_RIGHT_PAREN);
  }

  public Token lbracket() {
    return checkedMove(T.T_LEFT_BRACKET);
  }

  public Token rbracket() {
    return checkedMove(T.T_RIGHT_BRACKET);
  }

  public Token semicolon() {
    return checkedMove(T_SEMI_COLON);
  }

  //////////////////////////////////////////////////////////////////////

  public boolean isAssignOperator() {
    return Pcheckers.isAssignOperator(tok);
  }

  public boolean isUnaryOperator() {
    return Pcheckers.isUnaryOperator(tok);
  }

  public boolean isStorageClassSpec() {
    return Pcheckers.isStorageClassSpec(tok);
  }

  public boolean isTypeSpec() {
    return Pcheckers.isTypeSpec(tok);
  }

  public boolean isTypeQual() {
    return Pcheckers.isTypeQual(tok);
  }

  public boolean isFuncSpec() {
    return Pcheckers.isFuncSpec(tok);
  }

  public boolean isEnumSpecStart() {
    return Pcheckers.isEnumSpecStart(tok);
  }

  public boolean isStructOrUnionSpecStart() {
    return Pcheckers.isStructOrUnionSpecStart(tok);
  }

  public boolean isStaticAssert() {
    return Pcheckers.isStaticAssert(tok);
  }

  //@formatter:off
  public boolean isDeclSpecStart() {
    return isStorageClassSpec()
        || isTypeSpec()
        || isTypeQual()
        || isFuncSpec()
        || isEnumSpecStart()
        || isStructOrUnionSpecStart()
        || isStaticAssert()
        || isTypedefName(tok());
  }
  
  // this one need for cast expression and compound literal
  // XXX : fix
  public boolean isDeclSpecStart(Token what) {
    return Pcheckers.isStorageClassSpec(what)
        || Pcheckers.isTypeSpec(what)
        || Pcheckers.isTypeQual(what)
        || Pcheckers.isFuncSpec(what)
        || Pcheckers.isEnumSpecStart(what)
        || Pcheckers.isStructOrUnionSpecStart(what)
        || Pcheckers.isStaticAssert(what)
        || isTypedefName(what);
  }
  //@formatter:on

  public boolean isAttributeStart() {
    return Pcheckers.isAttributeStart(tok);
  }

  public boolean isAsmStart() {
    return Pcheckers.isAsmStart(tok);
  }

  public boolean isTypedefName(Token tok) {
    if (!tok.ofType(TOKEN_IDENT)) {
      return false;
    }
    if (tok.isBuiltinIdent()) {
      return false;
    }
    CSymbol s = getSym(tok.getIdent());
    return s != null && s.getBase() == CSymbolBase.SYM_TYPEDEF;
  }

  public boolean isEof() {
    return tok.ofType(T.TOKEN_EOF);
  }

  public CType parse_typename() {

    // TODO: simplify...
    CType base = new ParseBase(this).parseBase();
    CDecl decl = new ParseDecl(this).parseDecl();
    CType type = TypeMerger.build(base, decl);

    if (!decl.isAstract()) {
      perror("expect abstract declarator.");
    }
    return type;

  }

  public Tokenlist getTokenlist() {
    return tokenlist;
  }

  public void setCurrentFn(FunctionDefinition currentFn) {
    this.currentFn = currentFn;
  }

  public void restoreState(ParseState parseState) {
    this.tokenlist.setOffset(parseState.getTokenlistOffset());
    this.tok = parseState.getTok();
    this.currentFn = parseState.getCurrentFn();
    this.ringBuffer = new ArrayList<Token>(parseState.getRingBuffer());
    this.lastloc = parseState.getLastloc();
    this.prevtok = parseState.getPrevtok();
  }

  ///////////////////////////////////////////////////////////////////
  // ENTRY

  private void moveStraySemicolon() {
    while (tp() == T.T_SEMI_COLON) {
      move();
    }
  }

  public TranslationUnit parse_unit() {
    TranslationUnit tu = new TranslationUnit();
    pushscope();

    // top-level
    moveStraySemicolon();

    while (!tok.ofType(TOKEN_EOF)) {

      // before each function or global declaration
      moveStraySemicolon();

      ExternalDeclaration ed = new ParseToplevel(this).parseExternalDeclaration();
      tu.push(ed);
    }

    popscope();
    return tu;
  }

}
