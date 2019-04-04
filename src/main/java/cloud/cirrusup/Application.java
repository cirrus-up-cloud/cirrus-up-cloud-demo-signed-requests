package cloud.cirrusup;

import cloud.cirrusup.filter.HTTPSignatureCheckerFilter;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Service entry point.
 */
@SpringBootApplication
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Autowired
    GenericApplicationContext server;

    public static void main(String[] args) {
        LOG.info("Starting application... ");
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public FilterRegistrationBean filterAuthenticationBean() {

        LOG.info("Registering Authentication Filter");

        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(0);
        registrationBean.setFilter(server.getBean(HTTPSignatureCheckerFilter.class));
        registrationBean.setUrlPatterns(ImmutableList.of("/hello"));

        LOG.info("Authentication filter registered.");

        return registrationBean;
    }
}
