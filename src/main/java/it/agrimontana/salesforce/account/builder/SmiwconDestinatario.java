package it.agrimontana.salesforce.account.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.agrimontana.salesforce.dto.smiwcon.account.AgentInfo;
import lombok.Getter;

import static it.agrimontana.salesforce.service.smiwcon.AccountService.validNumber;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmiwconDestinatario {
    private final String customerReference;
    private final String customerCode;
    private final String newDifferentDestination;
    private final String agent;
    private final String username;
    private final String destinationCompanyName;
    private final String destinationAddress;
    private final String paymentType;
    private final String type;
    private final String destinationCity;
    private final String destinationProvince;
    private final String destinationPostalCode;
    private final String destinationPhone;
    private final String destinationEmail;
    private final String destinationCountryIso;
    private final String destinationVatNumber;

    private SmiwconDestinatario(Builder builder) {
        this.customerReference = builder.customerReference;
        this.customerCode = builder.customerCode;
        this.newDifferentDestination = builder.newDifferentDestination;
        this.agent = builder.agent;
        this.username = builder.username;
        this.destinationCompanyName = builder.destinationCompanyName;
        this.destinationAddress = builder.destinationAddress;
        this.paymentType = builder.paymentType;
        this.type = builder.type;
        this.destinationCity = builder.destinationCity;
        this.destinationProvince = builder.destinationProvince;
        this.destinationPostalCode = builder.destinationPostalCode;
        this.destinationPhone = builder.destinationPhone;
        this.destinationEmail = builder.destinationEmail;
        this.destinationCountryIso = builder.destinationCountryIso;
        this.destinationVatNumber = builder.destinationVatNumber;
    }

    @Override
    public String toString() {
        return "SmiwconDestinatario{" +
            "customerReference='" + customerReference + '\'' +
            ", customerCode='" + customerCode + '\'' +
            ", newDifferentDestination='" + newDifferentDestination + '\'' +
            ", agent='" + agent + '\'' +
            ", username='" + username + '\'' +
            ", destinationCompanyName='" + destinationCompanyName + '\'' +
            ", destinationAddress='" + destinationAddress + '\'' +
            ", paymentType='" + paymentType + '\'' +
            ", type='" + type + '\'' +
            ", destinationCity='" + destinationCity + '\'' +
            ", destinationProvince='" + destinationProvince + '\'' +
            ", destinationPostalCode='" + destinationPostalCode + '\'' +
            ", destinationPhone='" + destinationPhone + '\'' +
            ", destinationEmail='" + destinationEmail + '\'' +
            ", destinationCountryIso='" + destinationCountryIso + '\'' +
            ", destinationVatNumber='" + destinationVatNumber + '\'' +
            '}';
    }

    public static class Builder {
        private String customerReference;
        private String customerCode;
        private String newDifferentDestination;
        private String agent;
        private String username;
        private String destinationCompanyName;
        private String destinationAddress;
        private String paymentType;
        private String type;
        private String destinationCity;
        private String destinationProvince;
        private String destinationPostalCode;
        private String destinationPhone;
        private String destinationEmail;
        private String destinationCountryIso;
        private String destinationVatNumber;

        public Builder customerReference(String customerReference) {
            if(customerReference == null) {
                this.customerReference = "";
                return this;
            }
            this.customerReference = String.format("RIF-%s", customerReference);
            return this;
        }

        public Builder customerCode(String customerCode, String erpkey) {
            if(customerCode != null) {
                this.customerCode = customerCode;
                return this;
            }
            this.customerCode = erpkey;
            return this;
        }

        public Builder newDifferentDestination(String newDifferentDestination) {
            if(newDifferentDestination == null || newDifferentDestination.trim().isEmpty()) {
                this.newDifferentDestination = "S";
                return this;
            }
            this.newDifferentDestination = "N";
            return this;
        }

        public Builder agent(AgentInfo agent, AgentInfo capoArea) {
            // Usa agent se presente, altrimenti capoArea
            AgentInfo selected = null;
            if (agent != null && agent.getErpKey() != null && !agent.getErpKey().trim().isEmpty()) {
                selected = agent;
            } else if (capoArea != null && capoArea.getErpKey() != null && !capoArea.getErpKey().trim().isEmpty()) {
                selected = capoArea;
            }

            if (selected == null) {
                throw new IllegalArgumentException("Agente e CapoArea mancanti: almeno uno è obbligatorio");
            }

            this.agent = selected.getErpKey();
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder destinationCompanyName(String destinationCompanyName) {
            this.destinationCompanyName = destinationCompanyName.toUpperCase();
            return this;
        }

        public Builder destinationAddress(String destinationAddress) {
            this.destinationAddress = destinationAddress.toUpperCase();
            return this;
        }

        public Builder paymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder destinationCity(String destinationCity) {
            this.destinationCity = destinationCity.toUpperCase();
            return this;
        }

        public Builder destinationProvince(String destinationProvince) {
            this.destinationProvince = destinationProvince;
            return this;
        }

        public Builder destinationPostalCode(String destinationPostalCode) {
            this.destinationPostalCode = destinationPostalCode;
            return this;
        }

        public Builder destinationPhone(String destinationPhone) {
            if (validNumber(destinationPhone)) {
                this.destinationPhone = destinationPhone;
                return this;
            }

            this.destinationPhone = "";
            return this;
        }

        public Builder destinationEmail(String destinationEmail) {
            this.destinationEmail = destinationEmail;
            return this;
        }

        public Builder destinationCountryIso(String destinationCountryIso) {
            if(destinationCountryIso == null || destinationCountryIso.length() < 2) {
                this.destinationCountryIso = "";
                return this;
            }
            this.destinationCountryIso = destinationCountryIso.substring(0,2).toUpperCase();
            return this;
        }

        public Builder destinationVatNumber(String destinationVatNumber) {
            this.destinationVatNumber = destinationVatNumber;
            //this.destinationVatNumber = "\u00A0";
            return this;
        }

        public SmiwconDestinatario build() {
            return new SmiwconDestinatario(this);
        }
    }
}
