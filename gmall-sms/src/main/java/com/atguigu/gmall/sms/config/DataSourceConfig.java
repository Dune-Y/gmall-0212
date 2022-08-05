package com.atguigu.gmall.sms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


/**
 * @ ClassName DataSourceConfig
 * @ Description  TODO
 * @ Author Nimodo
 * @ Date 2022/8/2 9:58
 * @ Version 1.0
 */

@Configuration
public class DataSourceConfig {

//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSource hikariDataSource(@Value("${spring.datasource.url}") String url) {
//        HikariDataSource hikariDataSource = new HikariDataSource();
//        hikariDataSource.setJdbcUrl(url);
//        return hikariDataSource;
//    }

    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     * @return The default datasource
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(
            @Value("${spring.datasource.driver-class-name}")String driverClassName,
            @Value("${spring.datasource.url}")String url,
            @Value("${spring.datasource.username}")String username,
            @Value("${spring.datasource.password}")String password
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new DataSourceProxy(dataSource);
    }



}
