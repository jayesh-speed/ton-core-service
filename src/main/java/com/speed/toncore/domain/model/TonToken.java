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
@Table(name = TableNames.TON_TOKENS)
public class TonToken extends IdentityJpaDomain {

	@Column(name = DbFields.TOKEN_ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String tokenAddress;

	@Column(name = DbFields.TOKEN_NAME, nullable = false, columnDefinition = "VARCHAR(30)")
	private String tokenName;

	@Column(name = DbFields.TOKEN_SYMBOL, nullable = false, columnDefinition = "VARCHAR(30)")
	private String tokenSymbol;

	@Column(name = DbFields.MAIN_NET, columnDefinition = "TINYINT(1)")
	private boolean mainNet;

	@Column(name = DbFields.NO_OF_CELL, columnDefinition = "TINYINT UNSIGNED")
	private Integer noOfCell;

	@Column(name = DbFields.NO_OF_BITS, columnDefinition = "INT")
	private Integer noOfBits;

	@Column(name = DbFields.GAS_UNIT, columnDefinition = "TINYINT UNSIGNED")
	private Integer gasUnit;

	@Column(name = DbFields.DEPLOYMENT_COST, columnDefinition = "BIGINT")
	private Long deploymentCost;

	@Column(name = DbFields.NO_OF_CELL_V3, columnDefinition = "TINYINT UNSIGNED")
	private Integer noOfCellV3;

	@Column(name = DbFields.NO_OF_BITS_V3, columnDefinition = "INT")
	private Integer noOfBitsV3;

	@Column(name = DbFields.GAS_UNIT_V3, columnDefinition = "TINYINT UNSIGNED")
	private Integer gasUnitV3;

	@Column(name = DbFields.RESERVE_STORAGE_FEE, columnDefinition = "BIGINT")
	private Long reserveStorageFee;

	@Column(name = DbFields.CHAIN_ID, nullable = false, columnDefinition = "TINYINT UNSIGNED")
	private Integer chainId;

	@Column(name = DbFields.DECIMALS, nullable = false, columnDefinition = "INT")
	private Integer decimals;
}
