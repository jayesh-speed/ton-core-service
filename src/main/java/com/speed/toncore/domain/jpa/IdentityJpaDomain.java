package com.speed.toncore.domain.jpa;

import com.speed.javacommon.domain.model.TimestampJpaDomain;
import com.speed.toncore.constants.DbFields;
import com.speed.toncore.util.annotation.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public class IdentityJpaDomain extends TimestampJpaDomain {

	@Id
	@Column(name = DbFields.ID, nullable = false, length = 40, updatable = false)
	@IdGenerator
	private String id;
}
