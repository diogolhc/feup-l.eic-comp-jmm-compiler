package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public abstract class PreorderSemanticAnalyser extends PreorderJmmVisitor<SymbolTableImpl, Integer> implements SemanticAnalyser{

    private final List<Report> reports;

    protected PreorderSemanticAnalyser() {
        this.reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    public void addReport(Report report){
        reports.add(report);
    }

}
