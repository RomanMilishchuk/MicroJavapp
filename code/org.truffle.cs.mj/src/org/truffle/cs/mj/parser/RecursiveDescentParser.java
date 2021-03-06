package org.truffle.cs.mj.parser;

import static org.truffle.cs.mj.parser.Token.Kind.and;
import static org.truffle.cs.mj.parser.Token.Kind.assign;
import static org.truffle.cs.mj.parser.Token.Kind.break_;
import static org.truffle.cs.mj.parser.Token.Kind.charConst;
import static org.truffle.cs.mj.parser.Token.Kind.class_;
import static org.truffle.cs.mj.parser.Token.Kind.comma;
import static org.truffle.cs.mj.parser.Token.Kind.continue_;
import static org.truffle.cs.mj.parser.Token.Kind.else_;
import static org.truffle.cs.mj.parser.Token.Kind.eof;
import static org.truffle.cs.mj.parser.Token.Kind.final_;
import static org.truffle.cs.mj.parser.Token.Kind.ident;
import static org.truffle.cs.mj.parser.Token.Kind.if_;
import static org.truffle.cs.mj.parser.Token.Kind.lbrace;
import static org.truffle.cs.mj.parser.Token.Kind.lbrack;
import static org.truffle.cs.mj.parser.Token.Kind.lpar;
import static org.truffle.cs.mj.parser.Token.Kind.minus;
import static org.truffle.cs.mj.parser.Token.Kind.new_;
import static org.truffle.cs.mj.parser.Token.Kind.number;
import static org.truffle.cs.mj.parser.Token.Kind.or;
import static org.truffle.cs.mj.parser.Token.Kind.period;
import static org.truffle.cs.mj.parser.Token.Kind.plus;
import static org.truffle.cs.mj.parser.Token.Kind.print;
import static org.truffle.cs.mj.parser.Token.Kind.program;
import static org.truffle.cs.mj.parser.Token.Kind.rbrace;
import static org.truffle.cs.mj.parser.Token.Kind.rbrack;
import static org.truffle.cs.mj.parser.Token.Kind.read;
import static org.truffle.cs.mj.parser.Token.Kind.rem;
import static org.truffle.cs.mj.parser.Token.Kind.return_;
import static org.truffle.cs.mj.parser.Token.Kind.rpar;
import static org.truffle.cs.mj.parser.Token.Kind.semicolon;
import static org.truffle.cs.mj.parser.Token.Kind.slash;
import static org.truffle.cs.mj.parser.Token.Kind.times;
import static org.truffle.cs.mj.parser.Token.Kind.void_;
import static org.truffle.cs.mj.parser.Token.Kind.while_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.truffle.cs.mj.nodes.MJBinaryNode;
import org.truffle.cs.mj.nodes.MJBlock;
import org.truffle.cs.mj.nodes.MJBreakNode;
import org.truffle.cs.mj.nodes.MJConditionalNode;
import org.truffle.cs.mj.nodes.MJConstantNode;
import org.truffle.cs.mj.nodes.MJConstantNodeFactory;
import org.truffle.cs.mj.nodes.MJContinueNode;
import org.truffle.cs.mj.nodes.MJExpressionNode;
import org.truffle.cs.mj.nodes.MJExpressionStatement;
import org.truffle.cs.mj.nodes.MJFunction;
import org.truffle.cs.mj.nodes.MJInvokeNode;
import org.truffle.cs.mj.nodes.MJPrintNodeGen;
import org.truffle.cs.mj.nodes.MJReadNode;
import org.truffle.cs.mj.nodes.MJReadParameterNode;
import org.truffle.cs.mj.nodes.MJReturnNode;
import org.truffle.cs.mj.nodes.MJStatementNode;
import org.truffle.cs.mj.parser.identifiertable.TypeTable;
import org.truffle.cs.mj.nodes.MJWhileLoop;
import org.truffle.cs.mj.nodes.MJBinaryNodeFactory;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;

public final class RecursiveDescentParser {
    /** Maximum number of global variables per program */
    protected static final int MAX_GLOBALS = 32767;

    /** Maximum number of fields per class */
    protected static final int MAX_FIELDS = 32767;

    /** Maximum number of local variables per method */
    protected static final int MAX_LOCALS = 127;

    /** Last recognized token; */
    protected Token t;

    /** Lookahead token (not recognized).) */
    protected Token la;

    /** Shortcut to kind attribute of lookahead token (la). */
    protected Token.Kind sym;

    /** According scanner */
    public final RecursiveDescendScanner scanner;

    public RecursiveDescentParser(RecursiveDescendScanner scanner) {
        this.scanner = scanner;
        // Avoid crash when 1st symbol has scanner error.
        la = new Token(Token.Kind.none, 1, 1);
        firstExpr = EnumSet.of(ident, number, charConst, minus, lpar, new_);
        firstStat = EnumSet.of(ident, semicolon, lbrace, break_, continue_, if_,
                        print, read, return_, while_, final_);
        firstMethodDecl = EnumSet.of(void_, ident);
    }

    /** Sets of starting tokens for some productions. */
    private EnumSet<Token.Kind> firstExpr, firstStat, firstMethodDecl;

    /** Reads ahead one symbol. */
    private Token.Kind scan() {
        t = la;
        la = scanner.next();
        sym = la.kind;
        return t.kind;
    }

    /** Verifies symbol and reads ahead. */
    private void check(Token.Kind expected) {
        if (sym == expected) {
            scan();
        } else {
            throw new Error("Token " + expected + " expected");
        }
    }

    /**
     * Program = <br>
     * "program" ident <br>
     * { ConstDecl | VarDecl | ClassDecl } <br>
     * "{" { MethodDecl } "}" .
     */
    private void Program() {
        check(program);
        check(ident);
        for (;;) {
            if (sym == final_) {
                ConstExprDecl();
            } else if (sym == ident) {
                VarDecl();
            } else if (sym == class_) {
                ClassDecl();
            } else {
                break;
            }
        }
        check(lbrace);
        for (;;) {
            if (sym == rbrace || sym == eof) {
                break;
            } else if (firstMethodDecl.contains(sym)) {
                MethodDecl();
            }
        }
        check(rbrace);
    }

    /** ConstExprDecl = "final" Type ident "=" ( number | charConst ) ";" . */
    private void ConstExprDecl() {
        check(final_);
        String typeName = Type();
        check(ident);
        String varname = t.str;
        check(assign);
        if (sym == number) {
            scan();
            currentContext.createGlobalConstant(typeName, varname, Integer.valueOf(t.val));
        } else if (sym == charConst) {
            scan();
            currentContext.createGlobalConstant(typeName, varname, Character.valueOf((char) t.val));
        } else {
            throw new Error("Constexpr declaration should be of type Int of Char");
        }
        check(semicolon);
    }

    /** ConstDecl = "final" Type ident "=" Expr ";" . */
    private MJStatementNode ConstDecl() {
        check(final_);
        String typeName = Type();
        check(ident);
        String name = t.str;
        check(assign);
        MJStatementNode ret = currentContext.createConstLocalVarAndWrite(typeName, name, Expr());
        check(semicolon);
        return ret;
    }

    /** VarDecl = Type ident { "," ident } ";" . */
    private void VarDecl() {
        String typeName = Type();
        check(ident);
        currentContext.createLocalVar(typeName, t.str);
        while (sym == comma) {
            scan();
            check(ident);
            currentContext.createLocalVar(typeName, t.str);
        }
        check(semicolon);
    }

    /** ClassDecl = "class" ident "{" { VarDecl } "}" . */
    private void ClassDecl() {
        check(class_);
        check(ident);
        check(lbrace);
        while (sym == ident) {
            VarDecl();
        }
        check(rbrace);
    }

    MJFunctionContext currentContext = new MJFunctionContext();
    public List<MJFunction> functions = new ArrayList<>();
    public HashMap<MJFunction, CallTarget> callAble = new HashMap<MJFunction, CallTarget>();

    public MJFunction getFunction(String Name) {
        for (MJFunction f : functions) {
            if (f.getName().equals(Name))
                return f;
        }
        return null;
    }

    public MJFunction getMain() {
        for (MJFunction f : functions) {
            if (f.getName().equals("main"))
                return f;
        }
        return null;
    }

    public MJExpressionNode callFunction(String funcName) {
        List<MJExpressionNode> params = ActPars();
        MJFunction caleeFunction = getFunction(funcName);
        if (caleeFunction == null)
            throw new Error("Function does not exists");
        CallTarget callTarget = callAble.get(caleeFunction);
        if (callTarget == null) {
            callTarget = Truffle.getRuntime().createCallTarget(caleeFunction);
            callAble.put(caleeFunction, callTarget);
        }
        return new MJInvokeNode(callTarget, params.toArray(new MJExpressionNode[params.size()]), caleeFunction.returnType);
    }

    /**
     * MethodDecl = <br>
     * ( Type | "void" ) ident "(" [ FormPars ] ")" <br>
     * ( ";" | { VarDecl } Block ) .
     */
    private void MethodDecl() {
        String funcType = null;
        if (sym == ident) {
            funcType = Type();
        } else if (sym == void_) {
            scan();
        } else {
            throw new Error("Method declaration");
        }
        currentContext.stepInFunction();
        check(ident);
        String name = t.str;
        check(lpar);
        if (sym == ident) {
            FormPars();
        }
        check(rpar);
        while (sym == ident) {
            VarDecl();
        }
        functions.add(new MJFunction(name, null, currentContext.getContextFrameDescriptor(), funcType == null ? null : currentContext.getTypeDescriptor(funcType)));
        functions.get(functions.size() - 1).changeBody(Block());
    }

    /** FormPars = Type ident { "," Type ident } . */
    private void FormPars() {
        String typeName = Type();
        check(ident);
        currentContext.createParameter(typeName, t.str);
        while (sym == comma) {
            scan();
            typeName = Type();
            check(ident);
            currentContext.createParameter(typeName, t.str);
        }
    }

    /** Type = ident . */
    private String Type() {
        check(ident);
        return t.str;
// if (sym == lbrack) {
// scan();
// check(rbrack);
// }
    }

    /** Block = "{" { Statement } "}" . */
    private MJStatementNode Block() {
        check(lbrace);
        currentContext.stepInBlock();
        List<MJStatementNode> statements = Statements();
        MJBlock block = new MJBlock(statements.toArray(new MJStatementNode[statements.size()]), currentContext.getContextFrameDescriptor());
        currentContext.stepOutBlock();
        check(rbrace);
        return block;
    }

    private List<MJStatementNode> Statements() {
        List<MJStatementNode> statementNodes = new ArrayList<>();
        for (;;) {
            if (firstStat.contains(sym)) {
                statementNodes.add(Statement());
            } else {
                break;
            }
        }
        return statementNodes;
    }

    /**
     * Statement = <br>
     * Designator ( Assignop Expr | ActPars | "++" | "--" ) ";" <br>
     * | "if" "(" Condition ")" Statement [ "else" Statement ] <br>
     * | "while" "(" Condition ")" Statement <br>
     * | "break" ";" <br>
     * | "return" [ Expr ] ";" <br>
     * | "read" "(" Designator ")" ";" <br>
     * | "print" "(" Expr [ comma number ] ")" ";" <br>
     * | Block <br>
     * | ";" .
     */
    private MJStatementNode Statement() {
        MJStatementNode curStatementNode = null;
        switch (sym) {
            // ----- assignment, method call, in- or decrement, variable initializaton
            // ----- Designator ( Assignop Expr | ActPars | "++" | "--" ) ";"
            // ----- OR
            // ----- Type ident
            case ident:
                if (TypeTable.getInstance().getAvailableTypes().contains(la.str)) {
                    VarDecl();
                    curStatementNode = new MJStatementNode() {
                        @Override
                        public Object execute(VirtualFrame frame) {
                            return null;
                        }
                    };
                    break;
                }
                String des = Designator();
                switch (sym) {
                    case assign:
                        scan();
                        curStatementNode = currentContext.writeVariable(des, Expr());
                        break;
                    case plusas:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.AddNodeGen.create(currentContext.readVariable(des), Expr()));
                        break;
                    case minusas:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.SubtractNodeGen.create(currentContext.readVariable(des), Expr()));
                        break;
                    case timesas:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.MultiplicationNodeGen.create(currentContext.readVariable(des), Expr()));
                        break;
                    case slashas:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.DividerNodeGen.create(currentContext.readVariable(des), Expr()));
                        break;
                    case remas:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.ModulationNodeGen.create(currentContext.readVariable(des), Expr()));
                        break;
                    case lpar:
                        curStatementNode = new MJExpressionStatement(callFunction(des));
                        break;
                    case pplus:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.AddNodeGen.create(currentContext.readVariable(des), MJConstantNodeFactory.IntNodeGen.create(1)));
                        break;
                    case mminus:
                        scan();
                        curStatementNode = currentContext.writeVariable(des,
                                        MJBinaryNodeFactory.SubtractNodeGen.create(currentContext.readVariable(des), MJConstantNodeFactory.IntNodeGen.create(1)));
                        break;
                    default:
                        throw new Error("Designator Follow");
                }
                check(semicolon);
                break;
            case final_:
                curStatementNode = ConstDecl();
                break;
            // ----- "if" "(" Condition ")" Statement [ "else" Statement ]
            case if_:
                scan();
                check(lpar);
                MJExpressionNode condition = Condition();
                check(rpar);
                MJStatementNode trueBlock = Statement();
                MJStatementNode falseBlock = null;
                if (sym == else_) {
                    scan();
                    falseBlock = Statement();
                }
                curStatementNode = new MJConditionalNode(condition, trueBlock, falseBlock);
                break;
            // ----- "while" "(" Condition ")" Statement
            case while_:
                scan();
                check(lpar);
                MJExpressionNode conditionNode = Condition();
                check(rpar);
                MJStatementNode block = Statement();
                curStatementNode = new MJWhileLoop(conditionNode, block);
                break;
            // ----- "break" ";"
            case break_:
                scan();
                check(semicolon);
                curStatementNode = new MJBreakNode();
                break;

            // ----- "break" ";"
            case continue_:
                scan();
                check(semicolon);
                curStatementNode = new MJContinueNode();
                break;
            // ----- "return" [ Expr ] ";"
            case return_:
                scan();
                MJExpressionNode retValue = null;
                if (sym != semicolon) {
                    retValue = Expr();
                } else {
                }
                curStatementNode = new MJReturnNode(retValue);
                check(semicolon);
                break;
            // ----- "read" "(" Designator ")" ";"
            case read:
                scan();
                check(lpar);
                des = Designator();
                check(rpar);
                check(semicolon);
                curStatementNode = currentContext.writeVariable(des, new MJReadNode());
                break;
            // ----- "print" "(" Expr [ comma number ] ")" ";"
            case print:
                scan();
                check(lpar);
                MJExpressionNode expr = Expr();
                if (sym == comma) {
                    scan();
                    check(number);
                }
                check(rpar);
                check(semicolon);
                curStatementNode = MJPrintNodeGen.create(expr);
                break;
            case lbrace:
                curStatementNode = Block();
                break;
            case semicolon:
                scan();
                break;
            default:
                throw new Error("Invalid start...");
        }
        return curStatementNode;
    }

    /** ActPars = "(" [ Expr { "," Expr } ] ")" . */
    private List<MJExpressionNode> ActPars() {
        List<MJExpressionNode> exprsExpressionNodes = new ArrayList<>();
        check(lpar);
        if (firstExpr.contains(sym)) {
            for (;;) {
                exprsExpressionNodes.add(Expr());
                if (sym == comma) {
                    scan();
                } else {
                    break;
                }
            }
        }
        check(rpar);
        return exprsExpressionNodes;
    }

    /** Condition = CondTerm { "||" CondTerm } . */
    private MJExpressionNode Condition() {
        MJExpressionNode expressionNode = CondTerm();
        while (sym == or) {
            scan();
            expressionNode = MJBinaryNodeFactory.OrNodeGen.create(expressionNode, CondTerm());
        }
        return expressionNode;
    }

    /** CondTerm = CondFact { "&&" CondFact } . */
    private MJExpressionNode CondTerm() {
        MJExpressionNode expressionNode = CondFact();
        while (sym == and) {
            scan();
            expressionNode = MJBinaryNodeFactory.AndNodeGen.create(expressionNode, CondFact());
        }
        return expressionNode;

    }

    /** CondFact = Expr Relop Expr . */
    private MJExpressionNode CondFact() {
        MJExpressionNode expressionNode = Expr();
        switch (scan()) {
            case neq:
                expressionNode = MJBinaryNodeFactory.NotEqualNodeGen.create(expressionNode, Expr());
                break;
            case lss:
                expressionNode = MJBinaryNodeFactory.LessNodeGen.create(expressionNode, Expr());
                break;
            case leq:
                expressionNode = MJBinaryNodeFactory.LessEqualNodeGen.create(expressionNode, Expr());
                break;
            case eql:
                expressionNode = MJBinaryNodeFactory.EqualNodeGen.create(expressionNode, Expr());
                break;
            case geq:
                expressionNode = MJBinaryNodeFactory.GreaterEqualNodeGen.create(expressionNode, Expr());
                break;
            case gtr:
                expressionNode = MJBinaryNodeFactory.GreaterNodeGen.create(expressionNode, Expr());
                break;
            default:
                break;
        }
        return expressionNode;
    }

    /** Expr = [ "-" ] Term { Addop Term } . */
    private MJExpressionNode Expr() {
        MJExpressionNode expressionNode = null;
        boolean neg = false;
        if (sym == minus) {
            scan();
            neg = true;
        }
        expressionNode = Term();
        if (neg)
            expressionNode = MJBinaryNodeFactory.MultiplicationNodeGen.create(
                            MJConstantNodeFactory.IntNodeGen.create(-1),
                            expressionNode);
        while (sym == plus || sym == minus) {
            if (sym == plus) {
                scan();
                expressionNode = MJBinaryNodeFactory.AddNodeGen.create(expressionNode, Term());
            } else if (sym == minus) {
                scan();
                expressionNode = MJBinaryNodeFactory.SubtractNodeGen.create(expressionNode, Term());
            }
        }
        return expressionNode;
    }

    /** Term = Factor { Mulop Factor } . */
    private MJExpressionNode Term() {
        MJExpressionNode expressionNode = null;
        expressionNode = Factor();
        while (sym == times || sym == slash || sym == rem) {
            switch (scan()) {
                case times:
                    expressionNode = MJBinaryNodeFactory.MultiplicationNodeGen.create(expressionNode, Factor());
                    break;
                case slash:
                    expressionNode = MJBinaryNodeFactory.DividerNodeGen.create(expressionNode, Factor());
                    break;
                case rem:
                    expressionNode = MJBinaryNodeFactory.ModulationNodeGen.create(expressionNode, Factor());
                    break;
                default:
                    break;
            }
        }
        return expressionNode;
    }

    /**
     * Factor = <br>
     * Designator [ ActPars ] <br>
     * | number <br>
     * | charConst <br>
     * | "new" ident [ "[" Expr "]" ] <br>
     * | "(" Expr ")" .
     */
    private MJExpressionNode Factor() {
        MJExpressionNode expressionNode = null;
        switch (sym) {
            case abs:
                scan();
                check(lpar);
                Expr();
                check(rpar);
                break;
            case ident:
                String name = Designator();
                if (sym == lpar) {
                    expressionNode = callFunction(name);
                } else {
                    expressionNode = currentContext.readVariable(name);
                }
                break;
            case number:
                scan();
                expressionNode = MJConstantNodeFactory.IntNodeGen.create(t.val);
                break;
            case charConst:
                scan();
                expressionNode = MJConstantNodeFactory.CharNodeGen.create((char) t.val);
                break;
            case new_:
                scan();
                check(ident);
                if (sym == lbrack) {
                    scan();
                    Expr();
                    check(rbrack);
                }
                break;
            case lpar:
                scan();
                Expr();
                check(rpar);
                break;
            default:
                throw new Error("Invalid fact");
        }
        return expressionNode;
    }

    /** Designator = ident { "." ident | "[" Expr "]" } . */
    private String Designator() {
        check(ident);
        while (sym == period || sym == lbrack) {
            if (sym == period) {
                scan();
                check(ident);
                throw new Error("Fields ignored for now");
            } else {
                // scan();
                // Expr();
                // check(rbrack);
                throw new Error("Arrays ignored for now...");
            }
        }
        return t.str;
    }

    public void parse() {
        scan(); // scan first symbol
        Program(); // start analysis

        check(eof);
    }
}