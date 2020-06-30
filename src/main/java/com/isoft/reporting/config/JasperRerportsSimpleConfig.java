package com.isoft.reporting.config;

import com.isoft.reporting.service.SimpleReportExporter;
import com.isoft.reporting.service.SimpleReportFiller;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
public class JasperRerportsSimpleConfig {

    @Bean
    public DataSource dataSource() throws NamingException {
//        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).addScript("classpath:employee-schema.sql").build();

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://192.168.1.18:5432/demo?currentSchema=reporting");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("postgres");
        return dataSourceBuilder.build();
    }

    @Bean
    public SimpleReportFiller reportFiller() {
        return new SimpleReportFiller();
    }

    @Bean
    public SimpleReportExporter reportExporter() {
        return new SimpleReportExporter();
    }

}
