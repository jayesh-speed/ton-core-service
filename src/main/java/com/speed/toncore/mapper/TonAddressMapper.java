package com.speed.toncore.mapper;

import com.speed.toncore.domain.model.TonUsedWalletAddress;
import com.speed.toncore.domain.model.TonWalletAddress;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TonAddressMapper {

	TonAddressMapper INSTANCE = Mappers.getMapper(TonAddressMapper.class);

	TonUsedWalletAddress mapAddressToUsedAddress(TonWalletAddress tonWalletAddress);
	TonWalletAddress mapUsedAddressToAddress(TonUsedWalletAddress tonWalletAddress);
}
