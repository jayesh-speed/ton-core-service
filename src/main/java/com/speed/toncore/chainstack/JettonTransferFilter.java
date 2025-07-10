package com.speed.toncore.chainstack;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.pojo.JettonTransferDto;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Builder
public class JettonTransferFilter {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final int PAGE_LIMIT = 1000;
	private final OkHttpClient httpClient;
	private final List<MutablePair<String, Long>> jettonMasters;
	private final String apiUrl;
	private final String apiKey;
	private final Sinks.Many<JettonTransferDto> sink = Sinks.many().unicast().onBackpressureBuffer();
	private final Flux<JettonTransferDto> transferFlux = sink.asFlux();
	private long pollingInterval;
	private Disposable disposable;

	public Flux<JettonTransferDto> start() {
		startPolling();
		return transferFlux;
	}

	public void stop() {
		if (disposable != null && !disposable.isDisposed()) {
			disposable.dispose();
		}
	}

	private void startPolling() {
		disposable = Flux.interval(Duration.ZERO, Duration.ofSeconds(pollingInterval)).concatMap(tick -> pollTransfers().doOnNext(transfer -> {
			Sinks.EmitResult result = sink.tryEmitNext(transfer);
			if (result.isFailure()) {
				LOG.warn(Errors.TonIndexer.DROPPED_TRANSFERS_ERROR, transfer);
			}
		}).doOnError(sink::tryEmitError)).subscribe();
	}

	private Flux<JettonTransferDto> pollTransfers() {
		return Flux.defer(() -> {
			List<JettonTransferDto> newTransfers = new ArrayList<>();
			try {
				long startTime = System.currentTimeMillis();
				for (MutablePair<String, Long> jettonPair : jettonMasters) {
					List<JettonTransferDto> transfers = fetchJettonTransfers(jettonPair);
					newTransfers.addAll(transfers);
				}
				LOG.info("Fetched Jetton Transfer {}", newTransfers.size());
				LOG.info(Errors.TonIndexer.ELAPSE_TIME, System.currentTimeMillis() - startTime);
				return Flux.fromIterable(newTransfers);
			} catch (Exception e) {
				return Flux.error(e);
			}
		});
	}

	private List<JettonTransferDto> fetchJettonTransfers(MutablePair<String, Long> jettonPair) {
		String jettonMasterAddress = jettonPair.getLeft();
		long fromLt = jettonPair.getRight();
		long maxLt = fromLt;

		List<JettonTransferDto> transfers = new ArrayList<>();
		boolean hasMore = true;
		long offset = 0;

		while (hasMore) {
			try {
				Request request = buildRequest(jettonMasterAddress, fromLt, offset);
				try (Response response = httpClient.newCall(request).execute()) {
					if (response.isSuccessful()) {
						String responseBody = response.body() != null ? response.body().string() : null;
						if (StringUtil.nullOrEmpty(responseBody)) {
							LOG.warn(Errors.TonIndexer.EMPTY_RESPONSE_BODY);
							break;
						}
						JsonNode body = OBJECT_MAPPER.readTree(responseBody);
						if (!body.has(JsonKeys.TonIndexer.JETTON_TRANSFERS)) {
							LOG.warn(Errors.TonIndexer.MISSING_TRANSFERS_FIELD);
							break;
						}
						JsonNode transfersArray = body.get(JsonKeys.TonIndexer.JETTON_TRANSFERS);
						if (!transfersArray.isArray() || transfersArray.isEmpty()) {
							break;
						}
						for (JsonNode item : transfersArray) {
							try {
								JettonTransferDto transfer = OBJECT_MAPPER.treeToValue(item, JettonTransferDto.class);
								transfers.add(transfer);
								if (transfer.getTransactionLt() != null && transfer.getTransactionLt() > maxLt) {
									maxLt = transfer.getTransactionLt();
								}
							} catch (JsonParseException e) {
								throw new InternalServerErrorException(String.format(Errors.TonIndexer.FAILED_TO_PARSE_TRANSFER, item.toString()), e);
							}
						}
						hasMore = transfersArray.size() == PAGE_LIMIT;
						offset += PAGE_LIMIT;
					} else {
						throw new InternalServerErrorException(
								String.format(Errors.TonIndexer.ERROR_ON_FETCHING_JETTON_TRANSFERS, jettonMasterAddress, response.body(),
										response.code()));
					}
				}
			} catch (InternalServerErrorException ex) {
				throw ex;
			} catch (Exception e) {
				throw new InternalServerErrorException(String.format(Errors.TonIndexer.UNEXPECTED_ERROR_ON_FETCH_TRANSFERS, jettonMasterAddress), e);
			}
		}
		jettonPair.setRight(maxLt);
		return transfers;
	}

	private Request buildRequest(String jettonMaster, long fromLt, long offset) {
		HttpUrl url = Objects.requireNonNull(HttpUrl.parse(apiUrl))
				.newBuilder()
				.addQueryParameter(JsonKeys.QueryParameters.JETTON_MASTER, jettonMaster)
				.addQueryParameter(JsonKeys.QueryParameters.LIMIT, String.valueOf(PAGE_LIMIT))
				.addQueryParameter(JsonKeys.QueryParameters.OFFSET, String.valueOf(offset))
				.addQueryParameter(JsonKeys.QueryParameters.SORT, JsonKeys.QueryParameters.SORT_ASC)
				.addQueryParameter(JsonKeys.QueryParameters.START_LT, String.valueOf(fromLt + 1))
				.build();
		return new Request.Builder().url(url)
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.addHeader(Constants.X_API_KEY, apiKey)
				.build();
	}
}
