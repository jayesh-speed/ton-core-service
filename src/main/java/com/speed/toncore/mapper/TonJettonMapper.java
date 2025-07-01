package com.speed.toncore.mapper;

import com.speed.toncore.domain.model.TonJetton;
import com.speed.toncore.jettons.request.TonJettonRequest;
import com.speed.toncore.jettons.response.TonJettonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TonJettonMapper {

	TonJettonMapper INSTANCE = Mappers.getMapper(TonJettonMapper.class);

	TonJetton mapJettonRequestToModal(TonJettonRequest jettonRequest);

	TonJettonResponse mapModalToJettonResponse(TonJetton tonJetton);
}
