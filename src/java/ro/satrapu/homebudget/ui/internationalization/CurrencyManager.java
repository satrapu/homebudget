package ro.satrapu.homebudget.ui.internationalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

/**
 * Manages currencies using the current {@link Locale} instance.
 *
 * @author Satrapu
 */
@Model
public class CurrencyManager {

    @Inject
    @CurrentLocale
    Locale currentLocale;
    @Inject
    @AvailableLocales
    Collection<Locale> availableLocales;
    private List<SelectItem> currencies;

    @PostConstruct
    public void init() {
        currencies = new ArrayList<>();

        for (Locale locale : availableLocales) {
            Currency currency = Currency.getInstance(locale);
            currencies.add(new SelectItem(currency.getCurrencyCode(), currency.getDisplayName(currentLocale)));
        }

        Collections.sort(currencies, new Comparator<SelectItem>() {

            @Override
            public int compare(SelectItem leftCurrency, SelectItem rightCurrency) {
                return leftCurrency.getLabel().compareTo(rightCurrency.getLabel());
            }
        });
    }

    public Collection<SelectItem> getAvailableCurrencies() {
        return currencies;
    }

    public String getDisplayValue(String currencyCode) {
        return Currency.getInstance(currencyCode).getDisplayName(currentLocale);
    }
}
