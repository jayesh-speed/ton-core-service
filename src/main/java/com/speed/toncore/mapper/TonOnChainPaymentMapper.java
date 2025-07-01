package com.speed.toncore.mapper;

import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.domain.model.TonOnChainTx;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Date;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TonOnChainPaymentMapper {

	TonOnChainPaymentMapper INSTANCE = Mappers.getMapper(TonOnChainPaymentMapper.class);

	@Mapping(source = "value", target = "amount")
	TonOnChainTx mapWithdrawReqToOnChainTx(WithdrawRequest withdrawRequest);

	@AfterMapping
	default void updateContextInfoInOnChainTx(@MappingTarget TonOnChainTx onChainTx) {
		onChainTx.setMainNet(ExecutionContextUtil.getContext().isMainNet());
		onChainTx.setChainId(ExecutionContextUtil.getContext().getChainId());
		onChainTx.setTimestamp(System.currentTimeMillis());
		onChainTx.setTransactionDate(DateTimeUtil.convertToLocalDateTime(Date.from(Instant.now())));
	}
}
