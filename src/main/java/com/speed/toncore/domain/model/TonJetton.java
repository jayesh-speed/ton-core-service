package com.speed.toncore.domain.model;

import com.speed.toncore.constants.DbFields;
import com.speed.toncore.constants.TableNames;
import com.speed.toncore.domain.jpa.IdentityJpaDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = TableNames.TON_JETTONS)
public class TonJetton extends IdentityJpaDomain {

	@Column(name = DbFields.JETTON_MASTER_ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String jettonMasterAddress;

	@Column(name = DbFields.JETTON_NAME, nullable = false, columnDefinition = "VARCHAR(30)")
	private String jettonName;

	@Column(name = DbFields.JETTON_SYMBOL, nullable = false, columnDefinition = "VARCHAR(30)")
	private String jettonSymbol;

	@Column(name = DbFields.MAIN_NET, columnDefinition = "TINYINT(1)")
	private boolean mainNet;

	@Column(name = DbFields.FORWARD_TON_AMOUNT, columnDefinition = "DECIMAL(19,9)")
	private BigDecimal forwardTonAmount;

	@Column(name = DbFields.CHAIN_ID, nullable = false, columnDefinition = "TINYINT UNSIGNED")
	private Integer chainId;

	@Column(name = DbFields.DECIMALS, nullable = false, columnDefinition = "INT")
	private Integer decimals;
}
