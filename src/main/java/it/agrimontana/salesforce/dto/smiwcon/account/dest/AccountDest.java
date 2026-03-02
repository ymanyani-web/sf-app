package it.agrimontana.salesforce.dto.smiwcon.account.dest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDest {
    @JsonProperty("customerReference")
    private String customerReference;          // Riferimento cliente

    @JsonProperty("customerCode")
    private String customerCode;               // Codice cliente associato al destinatario

    @JsonProperty("newDifferentDestination")
    private String newDifferentDestination;    // S se nuovo destinatario

    @JsonProperty("agent")
    private String agent;                      // Codice agente Galileo associato al cliente

    @JsonProperty("username")
    private String username;                   // Username destinatario

    @JsonProperty("destinationCompanyName")
    private String destinationCompanyName;     // Ragione sociale destinatario

    @JsonProperty("destinationAddress")
    private String destinationAddress;         // Indirizzo cliente

    @JsonProperty("paymentType")
    private String paymentType;                // 006

    @JsonProperty("type")
    private String type;                       // D se destinatario

    @JsonProperty("destinationCity")
    private String destinationCity;            // Città del cliente

    @JsonProperty("destinationProvince")
    private String destinationProvince;        // Provincia (SIGLA)

    @JsonProperty("destinationPostalCode")
    private String destinationPostalCode;      // CAP della città del cliente

    @JsonProperty("destinationPhone")
    private String destinationPhone;           // Numero di telefono

    @JsonProperty("destinationEmail")
    private String destinationEmail;           // Email cliente

    @JsonProperty("destinationCountryIso")
    private String destinationCountryIso;      // Codice ISO del cliente

    @JsonProperty("destinationVatNumber")
    private String destinationVatNumber;       // Partita IVA del cliente
}
