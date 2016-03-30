package ucles.weblab.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ucles.weblab.common.i18n.countries.config.CountriesConfig;

/**
 * Auto-configuration for the internationalisation services.
 *
 * @since 15/10/15
 */
@Configuration
@Import(CountriesConfig.class)
public class I18nAutoConfiguration {
}
