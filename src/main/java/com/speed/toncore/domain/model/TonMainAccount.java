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

import java.math.BigDecimal;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TableNames.TON_MAIN_ACCOUNT)
public class TonMainAccount extends IdentityJpaDomain {

	@Column(name = DbFields.ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String address;

	@Column(name = DbFields.PUBLIC_KEY, columnDefinition = "VARCHAR(255)")
	private String publicKey;

	@Column(name = DbFields.PRIVATE_KEY, nullable = false, columnDefinition = "VARCHAR(255)")
	private String privateKey;

	@Column(name = DbFields.DEPLOYMENT_TX_HASH, columnDefinition = "VARCHAR(80)")
	private String deploymentTxHash;

	@Column(name = DbFields.ADDRESS_TYPE, nullable = false, columnDefinition = "VARCHAR(30)")
	private String addressType;

	@Column(name = DbFields.TON_BALANCE, nullable = false, columnDefinition = "DECIMAL(19,9)")
	private BigDecimal tonBalance;

	@Column(name = DbFields.TOKEN_ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String tokenAddress;

	@Column(name = DbFields.TOKEN_CONTRACT_ADDRESS, columnDefinition = "VARCHAR(80)")
	private String tokenContractAddress;

	@Column(name = DbFields.CHAIN_ID, nullable = false, columnDefinition = "TINYINT UNSIGNED")
	private Integer chainId;

	@Column(name = DbFields.MAIN_NET, columnDefinition = "TINYINT(1)")
	private boolean mainNet;
}
