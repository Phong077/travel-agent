package com.example.travelagent.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "rag.vector-store.enabled", havingValue = "true")
@MapperScan(
        basePackages = "com.example.travelagent.knowledge.persistence",
        sqlSessionFactoryRef = "ragSqlSessionFactory"
)
public class RagMyBatisPlusConfig {

    @Bean
    public DataSource ragDataSource(
            @Value("${rag.vector-store.jdbc-url:jdbc:postgresql://localhost:5432/travel_agent}") String jdbcUrl,
            @Value("${rag.vector-store.username:travel_agent}") String username,
            @Value("${rag.vector-store.password:travel_agent}") String password
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("org.postgresql.Driver");
        // 数据库短暂不可用时允许应用先启动，真正检索时再由业务兜底处理。
        dataSource.setInitializationFailTimeout(-1);
        return dataSource;
    }

    @Bean
    public SqlSessionFactory ragSqlSessionFactory(DataSource ragDataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(ragDataSource);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }
}
