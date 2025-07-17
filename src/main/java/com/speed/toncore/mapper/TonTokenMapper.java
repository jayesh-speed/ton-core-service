package com.speed.toncore.mapper;

import com.speed.toncore.domain.model.TonToken;
import com.speed.toncore.tokens.request.TonTokenRequest;
import com.speed.toncore.tokens.response.TonTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TonTokenMapper {

	TonTokenMapper INSTANCE = Mappers.getMapper(TonTokenMapper.class);

	TonToken mapTokenRequestToModal(TonTokenRequest tokenRequest);

	TonTokenResponse mapModalToTokenResponse(TonToken tonToken);
}
