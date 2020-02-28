package ast.parse;

import static jscan.tokenize.T.TOKEN_EOF;
import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_LEFT_BRACE;
import static jscan.tokenize.T.T_SEMI_COLON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jscan.Tokenlist;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CFuncParam;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.decl.CDeclEntry;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast._typesnew.parser.ParseBase;
import ast._typesnew.parser.ParseDecl;
import ast._typesnew.util.TypeMerger;
import ast.declarations.Declaration;
import ast.declarations.Initializer;
import ast.declarations.parser.ParseDeclarations;
import ast.errors.ParseErrors;
import ast.errors.ParseException;
import ast.stmt.Sswitch;
import ast.stmt.main.CStatement;
import ast.stmt.parser.ParseStatement;
import ast.symtabg.Symtab;
import ast.symtabg.elements.CSymbol;
import ast.symtabg.elements.CSymbolBase;
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

  public void setSwitches(Stack<Sswitch> switches) {
    this.switches = switches;
  }

  public void setLoops(Stack<String> loops) {
    this.loops = loops;
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

  public Token expectIdentifier() {
    if (!tok.ofType(TOKEN_IDENT)) {
      perror("expect ident, but was: " + tok.getValue());
    }
    Token saved = tok;
    move();
    return saved;
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

  // TODO:
  private void define__func__(Ident funcName) {
  }

  private void defineParameters(CType signature) {

    final List<CFuncParam> parameters = signature.getTpFunction().getParameters();

    if (parameters.size() == 1) {
      CFuncParam first = parameters.get(0);
      if (first.getType().isVoid() && first.getName() == null) {
        return;
      }
    }

    for (CFuncParam fparam : parameters) {
      CSymbol paramsym = new CSymbol(CSymbolBase.SYM_LVAR, fparam.getName(), fparam.getType(), tok());
      defineSym(fparam.getName(), paramsym);
    }
  }

  public ExternalDeclaration parse_external_declaration() {

    ParseBase pb = new ParseBase(this);
    CType declspecs = pb.findTypeAgain();
    StorageKind storageSpec = pb.getStorageSpec();

    if (tp() == T_SEMI_COLON) {
      boolean isStructUnionEnum = declspecs.isStrUnion() || declspecs.isEnumeration();
      if (!isStructUnionEnum) {
        perror("strange declaration-specifiers: " + declspecs.toString());
      }
      Declaration declaration = new Declaration(tok(), tok(), declspecs); // TODO:pos

      semicolon(); // XXX:
      return new ExternalDeclaration(declaration);
    }

    CDecl declarator = new ParseDecl(this).parseDecl();
    CType type = TypeMerger.build(declspecs, declarator);

    // if we here -> the next may one of
    // 1) KnR declarations
    // 2) {
    // 3) ;
    // 4) , for this: int f(void), *fip(), (*pfi)(), *ap[3];

    boolean isDeclstartOrSemicolonOrLbraceOrComma = tp() == T.T_SEMI_COLON
        || tp() == T.T_LEFT_BRACE
        || tp() == T.T_COMMA
        || tp() == T.T_ASSIGN
        || isDeclSpecStart();

    if (!isDeclstartOrSemicolonOrLbraceOrComma) {
      perror("strange declarator: " + type.toString());
    }

    Token current = tok();

    // int x;
    // .....^
    if (current.ofType(T_SEMI_COLON)) {
      CSymbolBase base = CSymbolBase.SYM_LVAR;
      if (storageSpec == StorageKind.ST_TYPEDEF) {
        base = CSymbolBase.SYM_TYPEDEF;
      }
      CSymbol sym = new CSymbol(base, declarator.getName(), type, current);
      defineSym(declarator.getName(), sym);

      List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

      initDeclaratorList.add(sym);
      Declaration declaration = new Declaration(current, current, initDeclaratorList);

      semicolon(); // XXX:
      return new ExternalDeclaration(declaration);
    }

    if (current.ofType(T.T_COMMA) || current.ofType(T.T_ASSIGN)) {

      if (current.ofType(T.T_COMMA)) {

        List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

        // head
        CSymbolBase base = CSymbolBase.SYM_LVAR;
        if (storageSpec == StorageKind.ST_TYPEDEF) {
          base = CSymbolBase.SYM_TYPEDEF;
        }
        CSymbol sym = new CSymbol(base, declarator.getName(), type, current);
        defineSym(declarator.getName(), sym);
        initDeclaratorList.add(sym);

        // tail from 'parse-declaration'
        //
        while (tp() == T.T_COMMA) {
          move();
          CSymbol initDeclaratorSeq = new ParseDeclarations(this, declspecs, storageSpec).parseInitDeclarator();
          initDeclaratorList.add(initDeclaratorSeq);
        }

        Declaration declaration = new Declaration(current, current, initDeclaratorList);

        semicolon(); // XXX:
        return new ExternalDeclaration(declaration);

      }

      else {

        List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

        // initializer's
        //

        checkedMove(T.T_ASSIGN);
        List<Initializer> initializer = new ParseDeclarations(this, type, storageSpec).parseInitializer(type);

        if (storageSpec == StorageKind.ST_TYPEDEF) {
          perror("typedef with initializer.");
        }

        CSymbol sym = new CSymbol(CSymbolBase.SYM_LVAR, declarator.getName(), type, initializer, current);
        defineSym(declarator.getName(), sym);

        initDeclaratorList.add(sym);

        // tail from 'parse-declaration'
        //
        while (tp() == T.T_COMMA) {
          move();
          CSymbol initDeclaratorSeq = new ParseDeclarations(this, declspecs, storageSpec).parseInitDeclarator();
          initDeclaratorList.add(initDeclaratorSeq);
        }

        Declaration declaration = new Declaration(current, current, initDeclaratorList);

        semicolon(); // XXX:
        return new ExternalDeclaration(declaration);
      }

    }

    //////////////////////////////////////////////////////////////////////
    //
    // normal cases for function begin here

    // K&R function style declaration-list
    //
    if (isDeclSpecStart()) {
      perror("unimpl. KnR function declaration.");
    }

    // and corner case: ANSI function-definition
    //
    if (tp() != T_LEFT_BRACE || !type.isFunction()) {
      perror("expect function definition...");
    }

    CSymbol funcSymbol = new CSymbol(CSymbolBase.SYM_FUNC, declarator.getName(), type, tok());
    defineSym(declarator.getName(), funcSymbol);
    FunctionDefinition fd = new FunctionDefinition(funcSymbol);

    currentFn = fd;
    pushscope();

    defineParameters(fd.getSignature().getType());
    define__func__(fd.getSymbol().getName());

    CStatement cst = new ParseStatement(this).parse_coumpound_stmt(true);
    fd.setCompoundStatement(cst);

    currentFn = null;
    popscope();

    return new ExternalDeclaration(fd);

  }

  private void assertTrue(boolean what) {
    if (!what) {
      perror("assertion fail.");
    }
  }

  private void applyTypes(List<Declaration> decllist, CDecl decl) {
    final List<CDeclEntry> typelist = decl.getTypelist();
    assertTrue(typelist.size() == 1);
    assertTrue(typelist.get(0).getBase() == TypeKind.TP_FUNCTION);
    List<CFuncParam> parameters = typelist.get(0).getParameters();

    Map<Ident, CType> vars = new HashMap<Ident, CType>(0);
    for (Declaration declaration : decllist) {
      if (!declaration.isVarlist()) {
        perror("expect variables declaration for old-style function-definition.");
      }
      for (CSymbol id : declaration.getVariables()) {
        final Ident name = id.getName();
        if (vars.containsKey(name)) {
          perror("declaration name duplicate: " + name.getName()); // TODO: not here.
        }
        vars.put(name, id.getType());
      }
    }

    if (vars.size() != parameters.size()) {
      perror("different size between parameters and declarations.");
    }

    for (CFuncParam p : typelist.get(0).getParameters()) {
      CType tp = vars.get(p.getName());
      p.setType(tp);
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

      ExternalDeclaration ed = parse_external_declaration();
      tu.push(ed);
    }

    popscope();
    return tu;
  }

}
