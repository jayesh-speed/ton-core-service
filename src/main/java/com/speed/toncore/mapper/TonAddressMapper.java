package com.speed.toncore.mapper;

import com.speed.toncore.domain.model.TonAddress;
import com.speed.toncore.domain.model.TonUsedAddress;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TonAddressMapper {

	TonAddressMapper INSTANCE = Mappers.getMapper(TonAddressMapper.class);

	TonUsedAddress mapAddressToUsedAddress(TonAddress tonAddress);
	TonAddress mapUsedAddressToAddress(TonUsedAddress tonWalletAddress);
}
