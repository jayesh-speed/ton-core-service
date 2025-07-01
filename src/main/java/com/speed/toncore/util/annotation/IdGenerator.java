package com.speed.toncore.util.annotation;

import com.speed.toncore.util.SpeedIdGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(SpeedIdGenerator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IdGenerator {}
