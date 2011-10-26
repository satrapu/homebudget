package ro.satrapu.homebudget.services.logging;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author satrapu
 */
@Named
@Singleton
public class LoggerProducer {

    /**
     * Creates a new {@link Logger} instance.
     * @param injectionPoint
     * @return
     */
    @Produces
    public Logger getLogger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getBean().getBeanClass());
    }
}