package com.speed.toncore.mapper;

import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.domain.model.TonOnChainTx;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.JettonTransferDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Date;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TonOnChainTxMapper {
	TonOnChainTxMapper INSTANCE = Mappers.getMapper(TonOnChainTxMapper.class);

	@Mapping(source = "source", target = "fromAddress")
	@Mapping(source = "destination", target = "toAddress")
	@Mapping(source = "transactionLt",target = "logicalTime")
	@Mapping(source = "transactionNow", target = "confirmationTimestamp")
	TonOnChainTx mapTransferToOnChainTx(JettonTransferDto transfer);

	@AfterMapping
	default void updateContextInfoInOnChainTx(@MappingTarget TonOnChainTx onChainTx, JettonTransferDto transferResponse) {
		onChainTx.setMainNet(ExecutionContextUtil.getContext().isMainNet());
		onChainTx.setChainId(ExecutionContextUtil.getContext().getChainId());
		onChainTx.setTokenAddress(transferResponse.getJettonMaster());
		onChainTx.setTransactionHash(transferResponse.getTransactionHash());
		onChainTx.setTimestamp(System.currentTimeMillis());
		onChainTx.setTransactionDate(DateTimeUtil.convertToLocalDateTime(Date.from(Instant.now())));
	}
}
