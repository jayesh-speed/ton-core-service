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
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = TableNames.TON_ON_CHAIN_TX)
public class TonOnChainTx extends IdentityJpaDomain {

	@Column(name = DbFields.JETTON_MASTER_ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String jettonMasterAddress;

	@Column(name = DbFields.FROM_ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String fromAddress;

	@Column(name = DbFields.TO_ADDRESS, nullable = false, columnDefinition = "VARCHAR(80)")
	private String toAddress;

	@Column(name = DbFields.AMOUNT, nullable = false, columnDefinition = "DECIMAL(32, 16)")
	private BigDecimal amount;

	@Column(name = DbFields.TRANSACTION_HASH, nullable = false, columnDefinition = "VARCHAR(80)")
	private String transactionHash;

	@Column(name = DbFields.TRACE_ID, columnDefinition = "VARCHAR(80)")
	private String traceId;

	@Column(name = DbFields.TRANSACTION_TYPE, nullable = false, columnDefinition = "VARCHAR(20)")
	private String transactionType;

	@Column(name = DbFields.TRANSACTION_STATUS, columnDefinition = "TINYINT")
	private Integer transactionStatus;

	@Column(name = DbFields.MAIN_NET, nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
	private boolean mainNet;

	@Column(name = DbFields.CHAIN_ID, nullable = false, columnDefinition = "TINYINT UNSIGNED")
	private Integer chainId;

	@Column(name = DbFields.TRANSACTION_FEE, columnDefinition = "DECIMAL(19, 9)")
	private BigDecimal transactionFee;

	@Column(name = DbFields.TIMESTAMP, nullable = false, columnDefinition = "BIGINT(20)")
	private Long timestamp;

	@Column(name = DbFields.TX_REFERENCE, nullable = false, columnDefinition = "VARCHAR(255)")
	private String txReference;

	@Column(name = DbFields.CONFIRMATION_TIMESTAMP, columnDefinition = "BIGINT(20)")
	private Long confirmationTimestamp;

	@Column(name = DbFields.LOGICAL_TIME, columnDefinition = "BIGINT(20)")
	private Long logicalTime;

	@Column(name = DbFields.TRANSACTION_DATE, nullable = false, columnDefinition = "DATETIME")
	private LocalDateTime transactionDate;
}
