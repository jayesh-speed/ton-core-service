package com.speed.toncore.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationMessages {

	public final String DecimalMax = " {jakarta.validation.constraints.DecimalMax.message}.";
	public final String DecimalMin = " {jakarta.validation.constraints.DecimalMin.message}.";
	public final String Email = " {jakarta.validation.constraints.Email.message}.";
	public final String Min = " {jakarta.validation.constraints.Min.message}.";
	public final String Max = " {jakarta.validation.constraints.Max.message}.";
	public final String NOTNULL = " {jakarta.validation.constraints.NotNull.message}.";
	public final String NULL = " {jakarta.validation.constraints.Null.message}.";
	public final String NotBlank = " {jakarta.validation.constraints.NotBlank.message}.";
	public final String NotBlank_Collection = " should not contain blank values.";
	public final String NotNull_Collection = " should not contain null values.";
	public final String NotEmpty = " {jakarta.validation.constraints.NotEmpty.message}.";
	public final String Size = " {jakarta.validation.constraints.Size.message}.";
}
