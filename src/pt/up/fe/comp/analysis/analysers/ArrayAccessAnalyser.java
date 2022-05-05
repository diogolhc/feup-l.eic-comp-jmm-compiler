package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Optional;

public class ArrayAccessAnalyser extends PreorderSemanticAnalyser {

    public ArrayAccessAnalyser(){
        super();
        addVisit(AstNode.ARRAY_ACCESS, this::visitArrayAccess);
    }

    public Integer visitArrayAccess(JmmNode expression, SymbolTableImpl symbolTable){

        Optional<JmmNode> ancestor_id = expression.getAncestor("Id");

        if (ancestor_id.isPresent()){
            ancestor_id.get().get("name");
        } else {
            // TODO throw error?
            return -1;
        }

        return 0;
    }

}
