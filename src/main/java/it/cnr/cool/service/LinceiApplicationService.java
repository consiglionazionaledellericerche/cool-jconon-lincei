package it.cnr.cool.service;

import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.application.ApplicationService;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

@Primary
@Service
public class LinceiApplicationService extends ApplicationService {
    @Autowired
    private I18nService i18nService;

    @Override
    public void validateAllegatiLinked(Folder call, Folder application, Session cmisSession, Locale locale) {
        /**
         * Controllo se impostata sul bando l'età massima
         */
        Optional.ofNullable(call.<BigInteger>getPropertyValue("jconon_call:eta_massima"))
                .ifPresent(etaMax -> {
                    LocalDate dataFineInvioDomande = Optional.ofNullable(call.<Calendar>getPropertyValue(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value()))
                            .map(Calendar::toInstant)
                            .map(instant -> instant.atZone(ZoneId.systemDefault()))
                            .map(ZonedDateTime::toLocalDate)
                            .orElse(LocalDate.now());
                    LocalDate dataNascita = Optional.ofNullable(application.<Calendar>getPropertyValue(JCONONPropertyIds.APPLICATION_DATA_NASCITA.value()))
                            .map(Calendar::toInstant)
                            .map(instant -> instant.atZone(ZoneId.systemDefault()))
                            .map(ZonedDateTime::toLocalDate)
                            .orElse(LocalDate.now());
                    int eta = Period.between(dataNascita, dataFineInvioDomande).getYears();
                    if (eta > etaMax.intValue()) {
                        throw new ClientMessageException("message.error.eta.max", this.i18nService.getLabel("message.error.eta.max", locale, new Object[]{eta, etaMax}));
                    }
                });
        super.validateAllegatiLinked(call, application, cmisSession, locale);
    }
}
