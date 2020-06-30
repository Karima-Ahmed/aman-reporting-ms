package com.isoft.reporting.web.rest;

import com.isoft.reporting.config.JasperRerportsSimpleConfig;
import com.isoft.reporting.service.SimpleReportExporter;
import com.isoft.reporting.service.SimpleReportFiller;
import io.github.jhipster.web.util.HeaderUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportResource {

//    private final SimpleReportFiller simpleReportFiller;
//
//    private final SimpleReportExporter simpleExporter;
//
//    public ReportResource(SimpleReportFiller simpleReportFiller, SimpleReportExporter simpleExporter) {
//        this.simpleReportFiller = simpleReportFiller;
//        this.simpleExporter = simpleExporter;
//    }

    //        public static void main(String[] args) {
    @GetMapping("/generate-report")
    public ResponseEntity<Void> generateReport() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(JasperRerportsSimpleConfig.class);
        ctx.refresh();

        SimpleReportFiller simpleReportFiller = ctx.getBean(SimpleReportFiller.class);
        simpleReportFiller.setReportFileName("employeeEmailReport.jrxml");
        simpleReportFiller.compileReport();

        simpleReportFiller.setReportFileName("employeeReport.jrxml");
        simpleReportFiller.compileReport();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Employee Report Example");
        parameters.put("minSalary", 15000.0);
        parameters.put("condition", " LAST_NAME ='Smith' ORDER BY FIRST_NAME");

        simpleReportFiller.setParameters(parameters);
        simpleReportFiller.fillReport();

        SimpleReportExporter simpleExporter = ctx.getBean(SimpleReportExporter.class);
        simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());

        simpleExporter.exportToPdf("employeeReport.pdf", "baeldung");
        simpleExporter.exportToXlsx("employeeReport.xlsx", "Employee Data");
        simpleExporter.exportToCsv("employeeReport.csv");
        simpleExporter.exportToHtml("employeeReport.html");
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityCreationAlert("appName", true, "entityNsme", "id")).build();
    }
}
