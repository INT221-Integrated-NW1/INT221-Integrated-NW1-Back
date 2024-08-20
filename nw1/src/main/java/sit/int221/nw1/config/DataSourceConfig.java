package sit.int221.nw1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.server")
    public DataSourceProperties serverDataSourceProperties(){
        return new DataSourceProperties();
    }
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.client")
    public DataSourceProperties clientDataSourceProperties(){
        return new DataSourceProperties();
    }
}
