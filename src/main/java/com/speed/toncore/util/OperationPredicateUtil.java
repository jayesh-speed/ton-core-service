package com.speed.toncore.util;

import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class OperationPredicateUtil {

    public static final String CREATE_OPERATION = "c";
    public static final String READ_OPERATION = "r";
    public static final String UPDATE_OPERATION = "u";

    public static Predicate<String> isEntityCreated() {
        return CREATE_OPERATION::equals;
    }

    public static Predicate<String> isEntityUpdated() {
        return UPDATE_OPERATION::equals;
    }

    public static Predicate<String> isEntityRead() {
        return READ_OPERATION::equals;
    }
}
