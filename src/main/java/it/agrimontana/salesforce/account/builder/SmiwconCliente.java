package it.agrimontana.salesforce.account.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.agrimontana.salesforce.dto.smiwcon.account.AgentInfo;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmiwconCliente {
    private final String newCustomer;
    private final String agent;
    private final String companyName;
    private final String customerReference;
    private final String address;
    private final String firstName;
    private final String lastName;
    private final String paymentType;
    private final String city;
    private final String province;
    private final String postalCode;
    private final String phone;
    private final String email;
    private final String countryIso;
    private final String vatNumber;
    private final String username;
    private final String type;
    private final String taxCode;
    private final String iban;
    private final String abi;
    private final String cab;
    private final String supportingBank;
    private final String pecAddress;
    private final String sdiCode;
    private final String template;
    private final String customerAcquisitionDate;

    private SmiwconCliente(Builder builder) {
        this.newCustomer = builder.newCustomer;
        this.agent = builder.agent;
        this.companyName = builder.companyName;
        this.customerReference = builder.customerReference;
        this.address = builder.address;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.paymentType = builder.paymentType;
        this.city = builder.city;
        this.province = builder.province;
        this.postalCode = builder.postalCode;
        this.phone = builder.phone;
        this.email = builder.email;
        this.countryIso = builder.countryIso;
        this.vatNumber = builder.vatNumber;
        this.username = builder.username;
        this.type = builder.type;
        this.taxCode = builder.taxCode;
        this.iban = builder.iban;
        this.abi = builder.abi;
        this.cab = builder.cab;
        this.supportingBank = builder.supportingBank;
        this.pecAddress = builder.pecAddress;
        this.sdiCode = builder.sdiCode;
        this.template = builder.template;
        this.customerAcquisitionDate = builder.customerAcquisitionDate;
    }

    @Override
    public String toString() {
        return "SmiwconCliente{" +
            "newCustomer='" + newCustomer + '\'' +
            ", agent='" + agent + '\'' +
            ", companyName='" + companyName + '\'' +
            ", customerReference='" + customerReference + '\'' +
            ", address='" + address + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", paymentType='" + paymentType + '\'' +
            ", city='" + city + '\'' +
            ", province='" + province + '\'' +
            ", postalCode='" + postalCode + '\'' +
            ", phone='" + phone + '\'' +
            ", email='" + email + '\'' +
            ", countryIso='" + countryIso + '\'' +
            ", vatNumber='" + vatNumber + '\'' +
            ", username='" + username + '\'' +
            ", type='" + type + '\'' +
            '}';
    }

    public static class Builder {
        private String newCustomer;
        private String agent;
        private String companyName;
        private String customerReference;
        private String address;
        private String firstName;
        private String lastName;
        private String paymentType;
        private String city;
        private String province;
        private String postalCode;
        private String phone;
        private String email;
        private String countryIso;
        private String vatNumber;
        private String username;
        private String type;
        private String taxCode;
        private String iban;
        private String abi;
        private String cab;
        private String supportingBank;
        private String pecAddress;
        private String sdiCode;
        private String template;
        private String customerAcquisitionDate;

        public Builder newCustomer(String newCustomer) {
            if(newCustomer == null || newCustomer.trim().isEmpty()) {
                this.newCustomer = "S";
                return this;
            }

            this.newCustomer = "N";
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

        public Builder companyName(String companyName) {
            this.companyName = companyName.toUpperCase();
            return this;
        }

        public Builder customerReference(String customerReference) {
            if(customerReference == null) {
                this.customerReference = "";
                return this;
            }
            this.customerReference = String.format("RIF-%s", customerReference);
            return this;
        }

        public Builder address(String address) {
            this.address = address.toUpperCase();
            return this;
        }

        public Builder firstName(String firstName) {
            if(firstName == null || firstName.trim().isEmpty()) {
                this.firstName = this.companyName.toUpperCase();
                return this;
            }
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            if(lastName == null || lastName.trim().isEmpty()) {
                this.lastName = this.companyName.toUpperCase();
                return this;
            }

            this.lastName = lastName;
            return this;
        }

        public Builder paymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public Builder city(String city) {
            this.city = city.toUpperCase();
            return this;
        }

        public Builder province(String province) {
            this.province = province;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder phone(String phone) {
            if (phone == null || phone.trim().isEmpty()) {
                throw new IllegalArgumentException("Telefono non valido");
            }
            this.phone = phone.replaceAll("\\s+", "");
            return this;
        }


        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder countryIso(String countryIso) {
            if(countryIso == null || countryIso.length() < 2) {
                this.countryIso = "";
                return this;
            }
            this.countryIso = countryIso.substring(0,2).toUpperCase();
            return this;
        }

        public Builder vatNumber(String vatNumber) {
            this.vatNumber = vatNumber;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder taxCode(String code) {
            this.taxCode = code;
            return this;
        }

        public Builder iban(String iban, String abi, String cab) {
            if(iban == null || iban.trim().isEmpty()) {
                this.iban = null;
                return this;
            }

            if(abi == null || abi.trim().isEmpty()){
                throw new IllegalArgumentException("Per inserire IBAN definire codice ABI");
            }

            if(cab == null || cab.trim().isEmpty()) {
                throw new IllegalArgumentException("Per inserire IBAN definire codice CAB");
            }

            this.iban = iban;
            return this;
        }

        public Builder abi(String abi) {
            this.abi = abi;
            return this;
        }

        public Builder cab(String cab) {
            this.cab = cab;
            return this;
        }

        public Builder supportingBank(String nomeBanca) {
            this.supportingBank = nomeBanca;
            return this;
        }

        public Builder customerAcquisitionDate(String customerAcquisitionDate) {
            OffsetDateTime dateTime = OffsetDateTime.parse(
                customerAcquisitionDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            );

            this.customerAcquisitionDate = dateTime.toLocalDate().toString();
            return this;
        }

        public Builder pecAddress(String pecAddress) {
            this.pecAddress = pecAddress;
            return this;
        }

        public Builder sdiCode(String sdiCode) {
            this.sdiCode = sdiCode;
            return this;
        }

        public Builder template(String template) {
            if(template == null || template.trim().isEmpty()) {
                throw new IllegalArgumentException("Tipologia cliente assente");
            }

            if("gdo".equalsIgnoreCase(template)) {
                this.template = "0299000004";
                return this;
            } else if ("labo".equalsIgnoreCase(template)){
                this.template = "0299000001";
                return this;
            } else if ("rtl".equalsIgnoreCase(template)){
                this.template = "0299000002";
                return this;
            } else if (template.contains("RTL") && template.contains("LABO")) {
                String[] parts = template.split(";");
                if(parts.length != 2) {
                    throw new IllegalArgumentException("Tipologia cliente multiplo non valido");
                }
                String first = parts[0].trim().toLowerCase();
                String second = parts[1].trim().toLowerCase();
                if ((first.equals("rtl") && second.equals("labo")) ||
                    (first.equals("labo") && second.equals("rtl"))) {
                    this.template = "0299000003";
                    return this;
                } else {
                    throw new IllegalArgumentException("Tipologia cliente inesistente non valida");
                }
            }
            throw new IllegalArgumentException("Tipologia cliente non valida");
        }

        public SmiwconCliente build() {
            return new SmiwconCliente(this);
        }
    }
}
