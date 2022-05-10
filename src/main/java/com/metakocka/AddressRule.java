package com.metakocka;

public class AddressRule {
    private Long id;
    private String province, city, street, post_number, validation_type, street_prefix;
    private boolean additional_rule, generated;

    public AddressRule() {
    }

    public AddressRule(Long id, String province, String city, String street, String post_number, String validation_type, String street_prefix, boolean additional_rule, boolean generated) {
        this.id = id;
        this.province = province;
        this.city = city;
        this.street = street;
        this.post_number = post_number;
        this.validation_type = validation_type;
        this.street_prefix = street_prefix;
        this.additional_rule = additional_rule;
        this.generated = generated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPost_number() {
        return post_number;
    }

    public void setPost_number(String post_number) {
        this.post_number = post_number;
    }

    public String getValidation_type() {
        return validation_type;
    }

    public void setValidation_type(String validation_type) {
        this.validation_type = validation_type;
    }

    public String getStreet_prefix() {
        return street_prefix;
    }

    public void setStreet_prefix(String street_prefix) {
        this.street_prefix = street_prefix;
    }

    public boolean isAdditional_rule() {
        return additional_rule;
    }

    public void setAdditional_rule(boolean additional_rule) {
        this.additional_rule = additional_rule;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    @Override
    public String toString() {
        return "AddressRule{" +
                "id=" + id +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", post_number='" + post_number + '\'' +
                ", validation_type='" + validation_type + '\'' +
                ", street_prefix='" + street_prefix + '\'' +
                ", additional_rule=" + additional_rule +
                ", generated=" + generated +
                '}';
    }
}
