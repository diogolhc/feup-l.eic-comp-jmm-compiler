package pt.up.fe.comp.visitors;

import pt.up.fe.comp.BaseNode;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class LineColAnnotator extends PreorderJmmVisitor<Integer, Integer> {
    public LineColAnnotator() {
        setDefaultVisit(this::annotateLineCol);
    }

    private Integer annotateLineCol(JmmNode node, Integer dummy) {
        var baseNode = (BaseNode) node;

        node.put("line", Integer.toString(baseNode.getBeginLine()));
        node.put("col", Integer.toString(baseNode.getBeginColumn()));

        return 0;
    }
}
