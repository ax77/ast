package ast.declarations.sem;

import java.util.List;

import jscan.tokenize.Token;
import ast.declarations.main.Declaration;
import ast.symtabg.elements.CSymbol;

public abstract class FinalizeInitializers {

  public static Declaration sVarlist(Token from, Token to, List<CSymbol> variables) {
    return new Declaration(from, to, variables);
  }

}
