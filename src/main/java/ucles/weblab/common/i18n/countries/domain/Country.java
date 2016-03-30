package ucles.weblab.common.i18n.countries.domain;

import java.util.List;
import java.util.Optional;

/**
 * Value object representing a country, which has natural entity status.
 *
 * @since 23/07/15
 */
public interface Country {
    Optional<Long> getPopulation();
    List<String> getCallingCodes();
}
