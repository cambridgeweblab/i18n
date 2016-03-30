package ucles.weblab.common.i18n.countries.webapi.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * View model, designed for JSON serialization, of country data.
 *
 * @since 18/05/15
 */
public class CountryResource extends ResourceSupport {
    private String name;
    private Collection<String> code;
    private String iso;
    private Collection<String> languages;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long population;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Map<String, CurrencyResource> currency;

    public static class CurrencyResource {
        private String symbol;

        protected CurrencyResource() {
        }

        public CurrencyResource(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CurrencyResource that = (CurrencyResource) o;
            return Objects.equals(symbol, that.symbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbol);
        }

        @Override
        public String toString() {
            return "CurrencyResource{" +
                    "symbol='" + symbol + '\'' +
                    '}';
        }
    }

    protected CountryResource() { // For Jackson
    }

    public CountryResource(String name, String iso, Collection<String> languages, Collection<String> code, Long population, Map<String, CurrencyResource> currency) {
        this.name = name;
        this.languages = languages;
        this.iso = iso;
        this.code = code;
        this.population = population;
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getCode() {
        return code;
    }

    public String getIso() {
        return iso;
    }

    public Collection<String> getLanguages() {
        return languages;
    }

    public Long getPopulation() {
        return population;
    }

    public Map<String, CurrencyResource> getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "CountryResource{" +
                "name='" + name + '\'' +
                ", code=" + code +
                ", iso='" + iso + '\'' +
                ", currency=" + currency  +
                '}';
    }
}
