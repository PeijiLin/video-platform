package com.lpjpro.constant;

import lombok.Getter;

@Getter
public enum StatusEnum {
    EXAMINE( 0,"审核中"),
    PASSED( 1,"已通过"),
    declined( 2,"已拒绝");


    private final int key;
    private final String value;

    StatusEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static StatusEnum getStatusValue(int key) {
        if (key<0 || key > 2) {
            return null;
        }
        StatusEnum[] values = StatusEnum.values();
        for (StatusEnum value : values) {
            if (value.getKey() == key)
                return value;
        }
        return null;
    }





}
