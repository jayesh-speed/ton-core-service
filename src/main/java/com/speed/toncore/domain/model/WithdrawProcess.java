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
@Table(name = TableNames.WITHDRAW_PROCESS)
public class WithdrawProcess extends IdentityJpaDomain {

	@Column(name = DbFields.REFERENCE_ID, nullable = false, columnDefinition = "varchar(40)")
	private String referenceId;

	@Column(name = DbFields.ACCOUNT_ID, columnDefinition = "varchar(40)")
	private String accountId;

	@Column(name = DbFields.WITHDRAW_REQUEST, nullable = false, columnDefinition = "varchar(80)")
	private String withdrawRequest;

	@Column(name = DbFields.TRANSACTION_HASH, columnDefinition = "varchar(80)")
	private String transactionHash;

	@Column(name = DbFields.TARGET_AMOUNT, columnDefinition = "decimal(32,16)")
	private BigDecimal targetAmount;

	@Column(name = DbFields.TARGET_CURRENCY, columnDefinition = "varchar(100)")
	private String targetCurrency;

	@Column(name = DbFields.STATUS, columnDefinition = "varchar(50)")
	private String status;

	@Column(name = DbFields.REFERENCE, columnDefinition = "varchar(40)")
	private String reference;

	@Column(name = DbFields.ACTUAL_FEE, columnDefinition = "decimal(32,16)")
	private BigDecimal actualFee;

	@Column(name = DbFields.WITHDRAW_TYPE,columnDefinition = "varchar(50)")
	private String withdrawType;

	@Column(name = DbFields.TX_REFERENCE, columnDefinition = "varchar(255)")
	private String txReference;

	@Column(name = DbFields.FAILURE_REASON, columnDefinition = "varchar(255)")
	private String failureReason;

	@Column(name = DbFields.TARGET_AMOUNT_PAID_AT, columnDefinition = "bigint(20)")
	private Long targetAmountPaidAt;

	@Column(name = DbFields.ADDRESS, columnDefinition = "varchar(80)")
	private String address;

	@Column(name = DbFields.MAIN_NET, columnDefinition = "boolean default false")
	private boolean mainNet;
}
