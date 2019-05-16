package com.easytoolsoft.easyreport.web.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 报表业务数据源配置类
 *
 * @author Tom Deng
 **/
@Configuration
@MapperScan(basePackages = MetaDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "metaSqlSessionFactory")
public class MetaDataSourceConfig extends AbstractDataSourceConfig {
    static final String PACKAGE = "com.easytoolsoft.easyreport.meta.data";
    static final String MAPPER_LOCATION = "classpath*:mybatis/mapper/meta/*.xml";

    @Bean
    @ConfigurationProperties(prefix = "easytoolsoft.easyreport.meta.datasource")
    public DataSourceProperties secondDataSourceProperties() {
        return new DataSourceProperties2();
    }

    @Bean(name = "metaDataSource")
    public DataSource dataSource() {
        DruidDataSource dds = secondDataSourceProperties().initializeDataSourceBuilder().type(DruidDataSource.class)
            .build();
        dds.setValidationQuery("select 1");
        return dds;
    }

    @Bean(name = "metaTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("metaDataSource") final DataSource dataSource) {
        return this.createTransactionManager(dataSource);
    }

    @Bean(name = "metaSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("metaDataSource") final DataSource dataSource)
        throws Exception {
        return this.createSqlSessionFactory(dataSource, MAPPER_LOCATION);
    }

    @Bean(name = "metaSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("metaSqlSessionFactory") final
    SqlSessionFactory sqlSessionFactory)
        throws Exception {
        return this.createSqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "metaTransactionTemplate")
    public TransactionTemplate transactionTemplate(@Qualifier("metaTransactionManager") final
    DataSourceTransactionManager transactionManager)
        throws Exception {
        return new TransactionTemplate(transactionManager);
    }
}
