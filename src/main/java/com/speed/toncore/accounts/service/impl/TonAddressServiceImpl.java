package com.speed.toncore.accounts.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.toncore.accounts.request.TonAddressRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonAddressService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.domain.model.QTonAddress;
import com.speed.toncore.domain.model.QTonMainAccount;
import com.speed.toncore.domain.model.QTonUsedAddress;
import com.speed.toncore.domain.model.TonAddress;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.domain.model.TonUsedAddress;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.mapper.TonAddressMapper;
import com.speed.toncore.repository.TonAddressRepository;
import com.speed.toncore.repository.TonMainAccountRepository;
import com.speed.toncore.repository.TonUsedAddressRepository;
import com.speed.toncore.util.TonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TonAddressServiceImpl implements TonAddressService {

	private static final QTonUsedAddress qTonUsedTonAddress = QTonUsedAddress.tonUsedAddress;
	private static final QTonAddress qTonAddress = QTonAddress.tonAddress;
	private static final QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	private final TonAddressServiceHelper tonAddressServiceHelper;
	private final TonAddressRepository tonAddressRepository;
	private final TonUsedAddressRepository tonUsedAddressRepository;
	private final TonMainAccountRepository tonMainAccountRepository;

	@Override
	public TonAccountResponse getNewTonAddress() {
		TonAddress tonAddress = tonAddressServiceHelper.fetchTonAddress();
		TonUsedAddress usedTonAddress = TonAddressMapper.INSTANCE.mapAddressToUsedAddress(tonAddress);
		usedTonAddress.setId(null);
		tonUsedAddressRepository.save(usedTonAddress);
		return TonAccountResponse.builder().address(TonUtil.rawToUserFriendlyAddress(usedTonAddress.getAddress())).build();
	}

	@Async
	@Override
	public void checkAddressAvailabilityAndCreate() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate tonUsedAddressPredicate = new BooleanBuilder(qTonUsedTonAddress.chainId.eq(chainId));
		Predicate tonAddressPredicate = new BooleanBuilder(qTonAddress.chainId.eq(chainId));
		long usedCount = tonUsedAddressRepository.findAndProject(tonUsedAddressPredicate, qTonUsedTonAddress, qTonUsedTonAddress.id).size();
		long total = tonAddressRepository.findAndProject(tonAddressPredicate, qTonAddress, qTonAddress.id).size() + usedCount;
		if ((double) usedCount / total > 0.8) {
			TonAddressRequest newAddressCreateRequest = new TonAddressRequest();
			newAddressCreateRequest.setCount(Math.toIntExact(total * 2 - usedCount));
			createPoolOfTonAddresses(newAddressCreateRequest);
		}
	}

	@Override
	@Transactional
	public void removeUsedTonAddress(String address) {
		address = TonUtil.toRawAddress(address);
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonUsedTonAddress.address.eq(address));
		TonUsedAddress usedTonAddress = tonUsedAddressRepository.findAndProjectUnique(queryPredicate, qTonUsedTonAddress, qTonUsedTonAddress.address,
				qTonUsedTonAddress.publicKey, qTonUsedTonAddress.privateKey, qTonUsedTonAddress.addressType, qTonUsedTonAddress.chainId,
				qTonUsedTonAddress.mainNet, qTonUsedTonAddress.id);
		tonUsedAddressRepository.delete(usedTonAddress);
		usedTonAddress.setId(null);
		TonAddress tonAddress = TonAddressMapper.INSTANCE.mapUsedAddressToAddress(usedTonAddress);
		tonAddressRepository.save(tonAddress);
	}

	@Override
	public void createPoolOfTonAddresses(TonAddressRequest tonAddressRequest) {
		tonAddressServiceHelper.createPoolOfTonAddresses(tonAddressRequest);
	}

	private List<TonAddress> getAllTonAddresses() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonAddress.chainId.eq(chainId));
		return tonAddressRepository.findAndProject(queryPredicate, qTonAddress, qTonAddress.address, qTonAddress.id, qTonAddress.mainNet,
				qTonAddress.chainId);
	}

	private List<TonUsedAddress> getAllUsedTonAddresses() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonUsedTonAddress.chainId.eq(chainId));
		return tonUsedAddressRepository.findAndProject(queryPredicate, qTonUsedTonAddress, qTonUsedTonAddress.address, qTonUsedTonAddress.id,
				qTonUsedTonAddress.mainNet, qTonUsedTonAddress.chainId);
	}

	private List<TonMainAccount> getMainAccounts() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.chainId.eq(chainId));
		return tonMainAccountRepository.findAndProject(queryPredicate, qTonMainAccount, qTonMainAccount.id, qTonMainAccount.address,
				qTonMainAccount.chainId, qTonMainAccount.privateKey, qTonMainAccount.publicKey);
	}

	@Override
	@Cacheable(value = Constants.CacheNames.RECEIVE_ADDRESSES, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public Set<String> fetchReceiveAddresses(Integer chainId) {
		Set<String> receiveAddresses = new HashSet<>();
		List<TonAddress> tonAddressList = getAllTonAddresses();
		if (CollectionUtil.nonNullNonEmpty(tonAddressList)) {
			receiveAddresses.addAll(tonAddressList.stream().map(TonAddress::getAddress).map(String::toUpperCase).collect(Collectors.toSet()));
		}
		List<TonUsedAddress> usedTonAddressList = getAllUsedTonAddresses();
		if (CollectionUtil.nonNullNonEmpty(usedTonAddressList)) {
			receiveAddresses.addAll(usedTonAddressList.stream().map(TonUsedAddress::getAddress).map(String::toUpperCase).collect(Collectors.toSet()));
		}
		return receiveAddresses;
	}

	@Override
	@CacheEvict(value = Constants.CacheNames.RECEIVE_ADDRESSES, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public void clearReceiveAddressesCache(Integer chainId) {}

	@Override
	@Cacheable(value = Constants.CacheNames.SEND_ADDRESSES, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public Set<String> fetchSendAddresses(Integer chainId) {
		List<TonMainAccount> mainAccountList = getMainAccounts();
		if (CollectionUtil.nonNullNonEmpty(mainAccountList)) {
			return mainAccountList.stream().map(TonMainAccount::getAddress).map(String::toUpperCase).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	@Override
	@CacheEvict(value = Constants.CacheNames.SEND_ADDRESSES, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public void clearSendAddressesCache(Integer chainId) {}
}
