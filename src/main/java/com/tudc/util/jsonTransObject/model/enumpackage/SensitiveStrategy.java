package com.tudc.util.jsonTransObject.model.enumpackage;

import lombok.Getter;

/**
 * @author
 * @create 2020-08-11 15:18
 * @contact
 **/
@Getter
public enum SensitiveStrategy {
    USERNAME(name->name.replaceAll("(\\S)\\S(\\S*)", "$1*$2")),
    IDCARD(card->card.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1****$2")),
    PHONE(phone->phone.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
    private final Desensitizer desensitizer;

    SensitiveStrategy(Desensitizer desensitizer) {
        this.desensitizer = desensitizer;
    }}
