package it.agrimontana.salesforce.dto.smiwcon.account.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountCli {

    @JsonProperty("customerReference")
    private String customerReference;   // Riferimento cliente

    @JsonProperty("agent")
    private String agent;               // Codice agente Galileo da associare al cliente

    @JsonProperty("companyName")
    private String companyName;         // Ragione sociale cliente

    @JsonProperty("address")
    private String address;             // Indirizzo cliente

    @JsonProperty("firstName")
    private String firstName;           // Nome cliente

    @JsonProperty("lastName")
    private String lastName;            // Cognome cliente

    @JsonProperty("paymentType")
    private String paymentType;         // 006

    private String newCustomer = "S";   // S se nuovo cliente
    private String type = "C";          // C se cliente

    @JsonProperty("city")
    private String city;                // Città del cliente

    @JsonProperty("province")
    private String province;            // Provincia (SIGLA)

    @JsonProperty("postalCode")
    private String postalCode;          // CAP della città del cliente

    @JsonProperty("phone")
    private String phone;               // Numero di telefono

    @JsonProperty("email")
    private String email;               // Email cliente

    @JsonProperty("countryIso")
    private String countryIso;          // Codice ISO del cliente

    @JsonProperty("vatNumber")
    private String vatNumber;           // Partita IVA del cliente

    @JsonProperty("username")
    private String username;

}

