package ast.parse;

import static jscan.tokenize.T.TOKEN_EOF;
import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_LEFT_PAREN;
import static jscan.tokenize.T.T_SEMI_COLON;
import static jscan.tokenize.T.T_TIMES;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import jscan.Tokenlist;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.main.StorageKind;
import ast._typesnew.parser.ParseBase;
import ast._typesnew.parser.ParseDecl;
import ast._typesnew.util.TypeMerger;
import ast.declarations.main.Declaration;
import ast.declarations.parser.ParseDeclarations;
import ast.stmt.Scompound;
import ast.stmt.Sswitch;
import ast.stmt.main.CStatement;
import ast.stmt.parser.ParseStatement;
import ast.symtabg.Symtab;
import ast.symtabg.elements.CSymbol;
import ast.unit.ExternalDeclaration;
import ast.unit.FunctionDefinition;
import ast.unit.TranslationUnit;

public class Parse {

  private final Tokenlist tokenlist;
  private Token tok;
  private FunctionDefinition currentFn;

  private Symtab<Ident, CSymbol> symbols;
  private Symtab<Ident, CSymbol> tags;

  private Stack<Sswitch> switches;
  private Stack<String> loops;

  private String lastloc;
  private List<Token> ringBuffer;

  //for easy unit-testing, like printing, and parse some parts
  private boolean isSemanticEnable;

  public Symtab<Ident, CSymbol> getTags() {
    return tags;
  }

  public boolean isSemanticEnable() {
    return isSemanticEnable;
  }

  public void setSemanticEnable(boolean isSemanticEnable) {
    this.isSemanticEnable = isSemanticEnable;
  }

  public Token tok() {
    return tok;
  }

  public FunctionDefinition getCurrentFn() {
    return currentFn;
  }

  public void defineSym(Ident key, CSymbol sym) {
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

  public void pushSwitch(Sswitch s) {
    switches.push(s);
  }

  public Sswitch peekSwitch() {
    return switches.peek();
  }

  public void popSwitch() {
    switches.pop();
  }

  public void pushLoop(String s) {
    loops.push(s);
  }

  public void popLoop() {
    loops.pop();
  }

  public Stack<Sswitch> getSwitches() {
    return switches;
  }

  public Stack<String> getLoops() {
    return loops;
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
    this.isSemanticEnable = true;
    this.currentFn = null;
    this.ringBuffer = new ArrayList<Token>(0);
    this.lastloc = "";
  }

  private void initScopes() {
    this.symbols = new Symtab<Ident, CSymbol>();
    this.tags = new Symtab<Ident, CSymbol>();

    this.switches = new Stack<Sswitch>();
    this.loops = new Stack<String>();
  }

  public String getLastLoc() {
    return lastloc;
  }

  public List<Token> getRingBuffer() {
    return ringBuffer;
  }

  public T tp() {
    return tok.getType();
  }

  public void move() {

    do {

      savePositions();

      tok = tokenlist.next();

    } while (tok.ofType(T.TOKEN_STREAMBEGIN) || tok.ofType(T.TOKEN_STREAMEND));

  }

  private void savePositions() {

    if (tok != null) {
      addLoc(tok);
    } else {
      addLoc(tokenlist.peek());
    }

    if (ringBuffer.size() > 120) {
      for (int i = 0; i < 50; i++) {
        ringBuffer.remove(0);
      }
    }
  }

  private void addLoc(Token from) {
    if (!from.typeIsSpecialStreamMarks()) {
      ringBuffer.add(from);
      lastloc = from.loc();
    }
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

  public void checkedMove(T expected) {
    if (tp() == expected) {
      move();
    } else {
      perror("expect " + expected + ", but was " + tp().toString());
    }
  }

  public Token checkedMoveIdent(Ident expect) {
    if (!tok.isIdent(expect)) {
      perror("expect id: " + expect.getName() + ", but was: " + tok.getValue());
    }
    Token saved = tok();
    move();
    return saved;
  }

  public Token expectIdentifier() {
    if (!tok.ofType(TOKEN_IDENT)) {
      perror("expect ident, but was: " + tok.getValue());
    }
    Token saved = tok;
    move();
    return saved;
  }

  public Token checkedGetT(T expect) {
    if (tp() != expect) {
      perror("expect: " + expect.toString() + ", but was: " + tok.getValue());
    }
    Token saved = tok;
    move();
    return saved;
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
    return checkedGetT(T.T_LEFT_PAREN);
  }

  public Token rparen() {
    return checkedGetT(T.T_RIGHT_PAREN);
  }

  public Token lbracket() {
    return checkedGetT(T.T_LEFT_BRACKET);
  }

  public Token rbracket() {
    return checkedGetT(T.T_RIGHT_BRACKET);
  }

  public Token semicolon() {
    return checkedGetT(T_SEMI_COLON);
  }

  //////////////////////////////////////////////////////////////////////

  //TODO:move to Pexpression
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
        || isTypedefName(tok()) ;
        //|| (tok.ofType(TOKEN_IDENT) && !tok.isBuiltinIdent() && tags.getsym(tok.getIdent()) != null);
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
        //|| (what.ofType(TOKEN_IDENT) && !what.isBuiltinIdent() && tags.getsym(what.getIdent()) != null);
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
    Ident what = tok.getIdent();

    CSymbol s = getSym(what);
    return s != null && s.getType().getStorage() == StorageKind.ST_TYPEDEF;
  }

  public boolean isEof() {
    return tok.ofType(T.TOKEN_EOF);
  }

  @SuppressWarnings("unused")
  private boolean isAbstractDeclaratorStart(Token what) {
    T tp = what.getType();
    return tp == T_LEFT_PAREN || tp == T.T_LEFT_BRACKET || tp == T_TIMES;
  }

  public CType parse_typename() {

    // TODO: simplify...
    CType base = new ParseBase(this).parseBase();
    CDecl decl = new ParseDecl(this).parseDecl();
    CType type = TypeMerger.build(base, decl);

    if (!decl.isAstract()) {
      //perror("expect abstract declarator.");
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

    this.ringBuffer.clear();// = parseState.getRingBuffer();
    savePositions();

    //TODO:
    //this.lastloc = parseState.getLastloc();
  }

  ///////////////////////////////////////////////////////////////////
  // ENTRY

  private void moveStraySemicolon() {
    while (tp() == T.T_SEMI_COLON) {
      move();
    }
  }

  public ExternalDeclaration parse_external_declaration() {

    FunctionDefinition fd = new Pfunction(this).isNextFunctionDefinition();

    if (fd != null) {
      currentFn = fd;
      pushscope();

      Scompound cst = new ParseStatement(this).parse_coumpound_stmt();
      CStatement compst = new CStatement(cst);

      fd.setCompoundStatement(compst);
      ExternalDeclaration ret = new ExternalDeclaration(fd);

      popscope();
      return ret;
    }

    Declaration declaration = new ParseDeclarations(this).parseDeclaration();
    return new ExternalDeclaration(declaration);

  }

  public TranslationUnit parse_unit() {
    TranslationUnit tu = new TranslationUnit();
    pushscope();

    // top-level
    moveStraySemicolon();

    while (!tok.ofType(TOKEN_EOF)) {

      // before each function or global declaration
      moveStraySemicolon();

      ExternalDeclaration ed = parse_external_declaration();
      tu.push(ed);
    }

    popscope();
    return tu;
  }

}
