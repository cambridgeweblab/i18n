package ucles.weblab.common.i18n.countries.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ucles.weblab.common.domain.BuilderProxyFactory;

import java.util.function.Supplier;

/**
 * Factory beans for domain object builders.
 *
 * @since 30/07/15
 */
@Configuration
public class CountriesBuilders {
    protected final BuilderProxyFactory builderProxyFactory = new BuilderProxyFactory();

    @Bean
    public Supplier<CountryEntity.Builder> countryEntityBuilder() {
        return () -> builderProxyFactory.builder(CountryEntity.Builder.class, CountryEntity.class);
    }
}
