package com.sooo.pdfmaker.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages="com.sooo.pdfmaker.service")
@MapperScan(basePackages= {"com.sooo.pdfmaker.mapper"})
@PropertySource("classpath:application.properties")
public class RootConfig {
	
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    
   @Bean
   public DataSource dataSource() {
      HikariConfig hikariConfig = new HikariConfig();
      
      hikariConfig.setDriverClassName(driverClassName);
      hikariConfig.setJdbcUrl(url);
      hikariConfig.setUsername(username);
      hikariConfig.setPassword(password);
      
      HikariDataSource dataSource = new HikariDataSource(hikariConfig);
      
      return dataSource;
   }
   
   @Bean
   public SqlSessionFactory sqlSessionFactory() throws Exception {
      SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
      sqlSessionFactory.setDataSource(dataSource());
      return (SqlSessionFactory) sqlSessionFactory.getObject();
   }
}