package sit.int221.nw1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages ="sit.int221.nw1.repositories.server",
        entityManagerFactoryRef = "serverEntityManagerFactory",
        transactionManagerRef ="serverTransactionManager"
)
public class SeverDataSourceConfig {
    @Autowired
    private DataSourceProperties serverDataSourceProperties;

    @Bean(name ="serverDataSource")
    public DataSource serverDataSource(){
        return DataSourceBuilder.create()
                .driverClassName(serverDataSourceProperties.getDriverClassName())
                .url(serverDataSourceProperties.getUrl())
                .username(serverDataSourceProperties.getUsername())
                .password(serverDataSourceProperties.getPassword())
                .build();
    }
    @Bean(name="serverEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean serverEntityManagerFactory(
            @Qualifier("serverDataSource") DataSource dataSource){
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("sit.int221.nw1.models.server");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaProperties(additionalProperties());
        return emf;
    }


    @Bean(name = "serverTransactionManager")
    public PlatformTransactionManager serverTransactionManager(
            @Qualifier("serverEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfb) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emfb.getObject());
        return transactionManager;
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        return properties;
    }
}
