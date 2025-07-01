package com.speed.toncore.domain.model;

import com.speed.toncore.constants.DbFields;
import com.speed.toncore.constants.TableNames;
import com.speed.toncore.domain.jpa.IdentityJpaDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.logstash.logback.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableNames.TON_LISTENERS)
public class TonListener extends IdentityJpaDomain {

	@Column(name = DbFields.STATUS, nullable = false, columnDefinition = "VARCHAR(10)")
	private String status;

	@Column(name = DbFields.CHAIN_ID, nullable = false, columnDefinition = "TINYINT UNSIGNED")
	private Integer chainId;

	@Column(name = DbFields.MAIN_NET, columnDefinition = "TINYINT(1)")
	private boolean mainNet;

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TonListener tonLis)) {
			return false;
		}
		return this.getId().equals(tonLis.getId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + StringUtils.trimToEmpty(String.valueOf(chainId)).hashCode();
		result = prime * result + StringUtils.trimToEmpty(getId()).hashCode();
		return result;
	}
}
