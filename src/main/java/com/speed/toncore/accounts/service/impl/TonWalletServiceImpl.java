package com.speed.toncore.accounts.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.toncore.accounts.request.TonWalletRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonWalletService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.domain.model.QTonMainAccount;
import com.speed.toncore.domain.model.QTonUsedWalletAddress;
import com.speed.toncore.domain.model.QTonWalletAddress;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.domain.model.TonUsedWalletAddress;
import com.speed.toncore.domain.model.TonWalletAddress;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.mapper.TonAddressMapper;
import com.speed.toncore.repository.TonMainAccountRepository;
import com.speed.toncore.repository.TonUsedWalletAddressRepository;
import com.speed.toncore.repository.TonWalletAddressRepository;
import com.speed.toncore.util.TonUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class TonWalletServiceImpl implements TonWalletService {

	private static final QTonUsedWalletAddress qTonUsedWalletAddress = QTonUsedWalletAddress.tonUsedWalletAddress;
	private static final QTonWalletAddress qTonWalletAddress = QTonWalletAddress.tonWalletAddress;
	private static final QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	private final TonWalletServiceHelper tonWalletServiceHelper;
	private final TonWalletAddressRepository tonWalletAddressRepository;
	private final TonUsedWalletAddressRepository tonUsedWalletAddressRepository;
	private final TonMainAccountRepository tonMainAccountRepository;

	@Override
	public TonAccountResponse getNewWalletAddress() {
		TonWalletAddress tonWalletAddress = tonWalletServiceHelper.fetchWalletAddress();
		TonUsedWalletAddress usedWalletAddress = TonAddressMapper.INSTANCE.mapAddressToUsedAddress(tonWalletAddress);
		usedWalletAddress.setId(null);
		tonUsedWalletAddressRepository.save(usedWalletAddress);
		return TonAccountResponse.builder().address(TonUtils.rawToUserFriendlyAddress(usedWalletAddress.getAddress())).build();
	}

	@Async
	@Override
	public void checkAddressAvailabilityAndCreate() {
		Predicate tonUsedWalletPredicate = new BooleanBuilder(qTonUsedWalletAddress.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		Predicate tonWalletPredicate = new BooleanBuilder(qTonWalletAddress.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		long usedCount = tonUsedWalletAddressRepository.findAndProject(tonUsedWalletPredicate, qTonUsedWalletAddress, qTonUsedWalletAddress.id).size();
		long total = tonWalletAddressRepository.findAndProject(tonWalletPredicate, qTonWalletAddress, qTonWalletAddress.id).size() + usedCount;
		if ((double) usedCount / total > 0.8) {
			createPoolOfTonWalletAddresses(TonWalletRequest.builder().count(Math.toIntExact(total * 2)).build());
		}
	}

	@Override
	@Transactional
	public void removeUsedTonWalletAddress(String address) {
		address = TonUtils.toRawAddress(address);
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonUsedWalletAddress.address.eq(address));
		TonUsedWalletAddress usedWalletAddress = tonUsedWalletAddressRepository.findAndProjectUnique(queryPredicate, qTonUsedWalletAddress,
				qTonUsedWalletAddress.address, qTonUsedWalletAddress.publicKey, qTonUsedWalletAddress.secretKey, qTonUsedWalletAddress.walletId,
				qTonUsedWalletAddress.walletType, qTonUsedWalletAddress.chainId, qTonUsedWalletAddress.mainNet, qTonUsedWalletAddress.id);
		tonUsedWalletAddressRepository.delete(usedWalletAddress);
		usedWalletAddress.setId(null);
		TonWalletAddress tonWalletAddress = TonAddressMapper.INSTANCE.mapUsedAddressToAddress(usedWalletAddress);
		tonWalletAddressRepository.save(tonWalletAddress);
	}

	@Override
	public void createPoolOfTonWalletAddresses(TonWalletRequest tonWalletRequest) {
		tonWalletServiceHelper.createPoolOfWalletAddresses(tonWalletRequest);
	}

	private List<TonWalletAddress> getAllTonAddresses() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonWalletAddress.chainId.eq(chainId));
		return tonWalletAddressRepository.findAndProject(queryPredicate, qTonWalletAddress, qTonWalletAddress.address, qTonWalletAddress.id,
				qTonWalletAddress.mainNet, qTonWalletAddress.chainId);
	}

	private List<TonUsedWalletAddress> getAllUsedTonAddresses() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonUsedWalletAddress.chainId.eq(chainId));
		return tonUsedWalletAddressRepository.findAndProject(queryPredicate, qTonUsedWalletAddress, qTonUsedWalletAddress.address,
				qTonUsedWalletAddress.id, qTonUsedWalletAddress.mainNet, qTonUsedWalletAddress.chainId);
	}

	private List<TonMainAccount> getMainAccounts() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.chainId.eq(chainId));
		return tonMainAccountRepository.findAndProject(queryPredicate, qTonMainAccount, qTonMainAccount.id, qTonMainAccount.address,
				qTonMainAccount.chainId, qTonMainAccount.secretKey, qTonMainAccount.publicKey);
	}

	@Override
	@Cacheable(value = Constants.CacheNames.RECEIVE_ADDRESSES, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public Set<String> fetchReceiveAddresses(Integer chainId) {
		Set<String> receiveAddresses = new HashSet<>();
		List<TonWalletAddress> tonAddressList = getAllTonAddresses();
		if (CollectionUtil.nonNullNonEmpty(tonAddressList)) {
			receiveAddresses.addAll(tonAddressList.stream().map(TonWalletAddress::getAddress).map(String::toUpperCase).collect(Collectors.toSet()));
		}
		List<TonUsedWalletAddress> usedTonAddressList = getAllUsedTonAddresses();
		if (CollectionUtil.nonNullNonEmpty(usedTonAddressList)) {
			receiveAddresses.addAll(
					usedTonAddressList.stream().map(TonUsedWalletAddress::getAddress).map(String::toUpperCase).collect(Collectors.toSet()));
		}
		return receiveAddresses;
	}

	@Override
	@Cacheable(value = Constants.CacheNames.SEND_ADDRESSES, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public Set<String> fetchSendAddresses(Integer chainId) {
		List<TonMainAccount> mainAccountList = getMainAccounts();
		if (CollectionUtil.nonNullNonEmpty(mainAccountList)) {
			return mainAccountList.stream().map(TonMainAccount::getAddress).map(String::toUpperCase).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}
}
