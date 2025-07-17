package com.speed.toncore.domain.model;

import com.speed.toncore.constants.DbFields;
import com.speed.toncore.constants.TableNames;
import com.speed.toncore.domain.jpa.IdentityJpaDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = TableNames.TON_USED_WALLET_ADDRESS)
public class TonUsedAddress extends IdentityJpaDomain {

	@Column(name = DbFields.ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String address;

	@Column(name = DbFields.PUBLIC_KEY, columnDefinition = "VARCHAR(255)")
	private String publicKey;

	@Column(name = DbFields.PRIVATE_KEY, nullable = false, columnDefinition = "VARCHAR(255)")
	private String privateKey;

	@Column(name = DbFields.ADDRESS_TYPE, nullable = false, columnDefinition = "VARCHAR(30)")
	private String addressType;

	@Column(name = DbFields.CHAIN_ID, nullable = false, columnDefinition = "TINYINT UNSIGNED")
	private Integer chainId;

	@Column(name = DbFields.MAIN_NET, columnDefinition = "TINYINT(1)")
	private boolean mainNet;
}
