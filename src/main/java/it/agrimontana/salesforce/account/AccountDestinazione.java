package it.agrimontana.salesforce.account;

import it.agrimontana.salesforce.service.database.As400Service;
import it.agrimontana.salesforce.service.salesforce.SalesforceService;
import it.agrimontana.salesforce.account.builder.SmiwconDestinatario;
import it.agrimontana.salesforce.dto.smiwcon.account.cli.SfAccountCli;
import it.agrimontana.salesforce.dto.smiwcon.account.dest.SfAccountDest;
import it.agrimontana.salesforce.service.smiwcon.AccountService;
import it.agrimontana.salesforce.service.smiwcon.SmiwconRestService;
import org.jboss.logging.Logger;

public class AccountDestinazione extends Account {
  private static final Logger logger = Logger.getLogger(AccountDestinazione.class);

  public AccountDestinazione(String id, SalesforceService salesforceService, SmiwconRestService smiwconRestService, AccountService accountService, As400Service as400Service) {
    super(id, salesforceService, smiwconRestService, accountService, as400Service);
  }

  public SmiwconDestinatario structSmiwconDestinazione(SfAccountDest destinatario, SfAccountCli cliente) {
    logger.debugf(
        "Costruzione dto smiwcon, funzione: %s, tipologia: cliente, account: %s",
        "structSmiwconOrder",
        destinatario.getId()
    );

    SmiwconDestinatario destinatario1 = new SmiwconDestinatario.Builder()
        .agent(cliente.getAgentObject(), cliente.getCapoAreaObject())
        .username(cliente.getCompanyName())
        .customerReference(cliente.getCompanyName())
        .customerCode(this.id, cliente.getERPKey__c())
        .newDifferentDestination(destinatario.getCustomerCode())
        .paymentType(cliente.getPaymentType())
        .type("D")
        .destinationCompanyName(cliente.getCompanyName())
        .destinationPhone(destinatario.getDestinationPhone())
        .destinationEmail(destinatario.getDestinationEmail())
        .destinationVatNumber(cliente.getVatNumber())
        .destinationCity(destinatario.getDestinationCity())
        .destinationAddress(destinatario.getDestinationAddress())
        .destinationProvince(destinatario.getDestinationProvince())
        .destinationPostalCode(destinatario.getDestinationPostalCode())
        .destinationCountryIso(destinatario.getDestinationCountryIso())
        .build();

    return destinatario1;

  }
}
