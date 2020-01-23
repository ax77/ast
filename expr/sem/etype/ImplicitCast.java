package ast.expr.sem.etype;

import static jscan.tokenize.T.T_AND;
import static jscan.tokenize.T.T_PLUS;
import static jscan.tokenize.T.T_TIMES;
import jscan.tokenize.Token;
import ast._typesnew.CPointerType;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast.expr.main.CExpression;
import ast.expr.main.CExpressionBase;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.parse.NullChecker;

public abstract class ImplicitCast {

  public static CExpression genPointer(CExpression inputExpr) {

    //  int main() {
    //    int arr[2];
    //    int x;
    //      
    //    //arr[0] = 32;
    //    //arr[1] = 64;
    //    //x = *(arr+1);
    //    
    //      *((&*((int*)arr+0))+0)=32;
    //      *((&*((int*)arr+0))+1)=64;
    //      x = *((&*((int*)arr+0))+1);
    //      
    //      return x==64;
    //  }

    NullChecker.check(inputExpr, inputExpr.getResultType(), inputExpr.getToken());

    final Token operator = inputExpr.getToken();
    final CType typeOfNode = inputExpr.getResultType();

    if (typeOfNode.isArray()) {
      // &*((type*)arr+0)
      //
      // 1) arr+0
      // 2) (type*)arr+0
      // 3) *((type*)arr+0)
      // 4) &(*((type*)arr+0))

      CType arrtype = typeOfNode.getTpArray().getArrayOf();
      CType ptrtype = new CType(new CPointerType(arrtype, false), StorageKind.ST_NONE);

      //1)
      Token operatorPlus = CExpressionBuilderHelper.copyTokenAddNewType(operator, T_PLUS, "+");
      CExpression binary = new CExpression(CExpressionBase.EBINARY, inputExpr,
          CExpressionBuilderHelper.digitZero(operatorPlus), operatorPlus);
      binary.setResultType(ptrtype);

      //2)
      CExpression castExpr = new CExpression(ptrtype, binary, operator);
      castExpr.setResultType(ptrtype);

      //3)
      Token operatorDeref = CExpressionBuilderHelper.copyTokenAddNewType(operator, T_TIMES, "*");
      CExpression unaryDeref = new CExpression(operatorDeref, castExpr);
      unaryDeref.setResultType(ptrtype);

      //4)
      Token operatorAddressOf = CExpressionBuilderHelper.copyTokenAddNewType(operator, T_AND, "&");
      CExpression addressOfFirstElem = new CExpression(operatorAddressOf, unaryDeref);
      addressOfFirstElem.setResultType(ptrtype);

      return addressOfFirstElem;
    }

    if (typeOfNode.isFunction()) {
      CType ptrtype = new CType(new CPointerType(typeOfNode, false), StorageKind.ST_NONE);
      Token operatorAddressOf = CExpressionBuilderHelper.copyTokenAddNewType(operator, T_AND, "&");

      CExpression addressOfFunction = new CExpression(operatorAddressOf, inputExpr);
      addressOfFunction.setResultType(ptrtype);

      return addressOfFunction;
    }

    return inputExpr;
  }
}
