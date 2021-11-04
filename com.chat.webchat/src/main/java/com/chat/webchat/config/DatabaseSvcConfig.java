package com.chat.webchat.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;


/**
 * DatabaseSvcConfig
 *
 */
@Configuration
@MapperScan(
		basePackages = {
				"com.chat.webchat.mapper"
		}
	,	sqlSessionFactoryRef = "sqlSessionFactorySvc"
)

public class DatabaseSvcConfig {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String MYBATIS_CONFIG_FILE = "config/mybatis/mybatis-config.xml";
	private static final String MYBATIS_MAPPER_PATH = "classpath:config/mybatis/mapper/**/*Mapper.xml";

	@Value("${db.jdbc.svc.driver}")
	private String driveClassName;

	@Value("${db.jdbc.svc.url}")
	private String jdbcURL;

	@Value("${db.jdbc.svc.username}")
	private String username;

	@Value("${db.jdbc.svc.password}")
	private String password;

	@Value("${db.jdbc.svc.minimumIdle}")
	private String minIdle;

	@Value("${db.jdbc.svc.maximumPoolSize}")
	private String maxIdle;

	@Value("${db.jdbc.svc.connection.test.query}")
	private String connectionTestQuery;

	/**
	 * @return DataSource
	 */
	@Bean(name = "dataSourceSvc")
	public DataSource dataSource() {
		logger.debug("database url : {}", jdbcURL);
		HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setDriverClassName(StringUtils.trim(driveClassName));
		hikariDataSource.setJdbcUrl(StringUtils.trim(jdbcURL));
		hikariDataSource.setUsername(StringUtils.trim(username));
		hikariDataSource.setPassword(StringUtils.trim(password));
		hikariDataSource.setConnectionTestQuery(StringUtils.trim(connectionTestQuery));
		hikariDataSource.setMinimumIdle(NumberUtils.toInt(minIdle, 0));
		hikariDataSource.setMaximumPoolSize(NumberUtils.toInt(maxIdle, 0));
		return new DataSourceSpy(hikariDataSource);
	}

	/**
	 * @param dataSource
	 * @return SqlSessionFactory
	 */
	@Bean(name = "sqlSessionFactorySvc")
	public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSourceSvc") DataSource dataSource){

		try {
			SqlSessionFactoryBean bean = new SqlSessionFactoryBean();

			//
			bean.setDataSource(dataSource);

			// MyBatis 설정파일 위치 설정
			bean.setConfigLocation(new ClassPathResource(MYBATIS_CONFIG_FILE));

			// XML을 매퍼로 등록
			List<Resource> resourceList = new ArrayList<Resource>();
			PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
			resourceList.addAll(Arrays.asList(pathMatchingResourcePatternResolver.getResources(MYBATIS_MAPPER_PATH)));
			Resource[] resources = new Resource[resourceList.size()];
			bean.setMapperLocations(resourceList.toArray(resources));

			return bean.getObject();			
		}catch (Exception e) {
			logger.error(String.format("[%s][%s] - %s","DatabaseSvcConfig.sqlSessionFactory","FAIL", e.getMessage()));
			return null;
		}
		
	}

	/**
	 * @param dataSource
	 * @return PlatformTransactionManager
	 */
	@Bean(name = "txManagerSvc")
	public PlatformTransactionManager txManager(@Qualifier("dataSourceSvc") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

}
