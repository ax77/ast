package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import jscan.tokenize.Stream;
import jscan.tokenize.Token;

import org.junit.Ignore;
import org.junit.Test;

import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class TestSimple1 {

  private static Stream getHashedStream(String source) throws IOException {
    return new Stream("<utest>", source);
  }

  @Ignore
  @Test
  public void test() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  static void *malloc(unsigned long long);                            \n");
    sb.append(" /*002*/  typedef int elemtype;                                               \n");
    sb.append(" /*003*/  //static const int NULL = 0;                                        \n");
    sb.append(" /*004*/  struct lnode { struct lnode *prev, *next; elemtype e; };            \n");
    sb.append(" /*005*/  struct llist { struct lnode *first, *last; int size; };             \n");
    sb.append(" /*006*/  static inline struct llist *llist_new()                             \n");
    sb.append(" /*007*/  {                                                                   \n");
    sb.append(" /*008*/    struct llist *l = (struct llist*) malloc(sizeof(struct llist));   \n");
    sb.append(" /*009*/    l->first = l->last = 0;                                        \n");
    sb.append(" /*010*/    return l;                                                         \n");
    sb.append(" /*011*/  }                                                                   \n");
    sb.append(" /*012*/  static inline struct lnode *lnode_new(                              \n");
    sb.append(" /*013*/    struct lnode *prev,                                               \n");
    sb.append(" /*014*/    elemtype e,                                                       \n");
    sb.append(" /*015*/    struct lnode *next)                                               \n");
    sb.append(" /*016*/  {                                                                   \n");
    sb.append(" /*017*/    struct lnode *n = (struct lnode*) malloc(sizeof(struct lnode));   \n");
    sb.append(" /*018*/    /*assert(n);*/                                                    \n");
    sb.append(" /*019*/    n->prev = prev;                                                   \n");
    sb.append(" /*020*/    n->e = e;                                                         \n");
    sb.append(" /*021*/    n->next = next;                                                   \n");
    sb.append(" /*022*/    return n;                                                         \n");
    sb.append(" /*023*/  }                                                                   \n");
    sb.append(" /*024*/  void llist_pushf(struct llist *list, elemtype e)                    \n");
    sb.append(" /*025*/  {                                                                   \n");
    sb.append(" /*026*/    /*assert(list);*/                                                 \n");
    sb.append(" /*027*/    struct lnode *f = list->first;                                    \n");
    sb.append(" /*028*/    struct lnode *n = lnode_new(0, e, f);                          \n");
    sb.append(" /*029*/    list->first = n;                                                  \n");
    sb.append(" /*030*/    if(f) {                                                           \n");
    sb.append(" /*031*/      f->prev = n;                                                    \n");
    sb.append(" /*032*/    } else {                                                          \n");
    sb.append(" /*033*/      list->last = n;                                                 \n");
    sb.append(" /*034*/    }                                                                 \n");
    sb.append(" /*035*/    list->size++;                                                     \n");
    sb.append(" /*036*/  }                                                                   \n");
    sb.append(" /*037*/  void llist_pushb(struct llist *list, elemtype e)                    \n");
    sb.append(" /*038*/  {                                                                   \n");
    sb.append(" /*039*/    /*assert(list);*/                                                 \n");
    sb.append(" /*040*/    struct lnode *l = list->last;                                     \n");
    sb.append(" /*041*/    struct lnode *n = lnode_new(l, e, 0);                          \n");
    sb.append(" /*042*/    list->last = n;                                                   \n");
    sb.append(" /*043*/    if(l) {                                                           \n");
    sb.append(" /*044*/      l->next = n;                                                    \n");
    sb.append(" /*045*/    } else {                                                          \n");
    sb.append(" /*046*/      list->first = n;                                                \n");
    sb.append(" /*047*/    }                                                                 \n");
    sb.append(" /*048*/    list->size++;                                                     \n");
    sb.append(" /*049*/  }                                                                   \n");
    sb.append(" /*050*/  int main()                                                          \n");
    sb.append(" /*051*/  {                                                                   \n");
    sb.append(" /*052*/    int ret = 0;                                                      \n");
    sb.append(" /*053*/    struct llist *begin = llist_new();                                \n");
    sb.append(" /*054*/    llist_pushf(begin, 0);                                            \n");
    sb.append(" /*055*/    llist_pushf(begin, 1);                                            \n");
    sb.append(" /*056*/    llist_pushf(begin, 2);                                            \n");
    sb.append(" /*057*/    llist_pushb(begin, 0);                                            \n");
    sb.append(" /*058*/    llist_pushb(begin, 1);                                            \n");
    sb.append(" /*059*/    llist_pushb(begin, 2);                                            \n");
    sb.append(" /*060*/    /* I */                                                           \n");
    sb.append(" /*061*/    int x = 0;                                                        \n");
    sb.append(" /*062*/    struct lnode *tmp = begin->first;                                 \n");
    sb.append(" /*063*/    while(tmp) {                                                      \n");
    sb.append(" /*064*/      x += tmp->e;                                                    \n");
    sb.append(" /*065*/      tmp = tmp->next;                                                \n");
    sb.append(" /*066*/    }                                                                 \n");
    sb.append(" /*067*/    ret += !(x == 6);                                                 \n");
    sb.append(" /*068*/    return ret;                                                       \n");
    sb.append(" /*069*/  }                                                                   \n");

    List<Token> tokenlist = getHashedStream(sb.toString()).getTokenlist();
    Parse p = new Parse(tokenlist);
    TranslationUnit unit = p.parse_unit();

    assertEquals(10-1, unit.getExternalDeclarations().size()); // comment NULL for now...
    assertEquals(5, unit.countOfFunctionDefinitions(unit));
    assertEquals(5-1, unit.countOfDeclarations(unit));
  }

}
