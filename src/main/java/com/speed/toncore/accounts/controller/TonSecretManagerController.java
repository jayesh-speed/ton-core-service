package com.speed.toncore.accounts.controller;

import com.speed.toncore.constants.Errors;
import com.speed.toncore.pojo.TraceDto;
import com.speed.toncore.ton.TonCoreServiceHelper;
import com.speed.toncore.util.SecurityManagerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class TonSecretManagerController {

	private final TonCoreServiceHelper tonCoreServiceHelper;

	public TonSecretManagerController(TonCoreServiceHelper tonCoreServiceHelper) {this.tonCoreServiceHelper = tonCoreServiceHelper;}

	@GetMapping("/create-secret-key/{length}")
	public ResponseEntity<String> createSecretKey(@PathVariable Integer length) throws NoSuchAlgorithmException {
		return ResponseEntity.ok(Base64.getEncoder().encodeToString(SecurityManagerUtil.generateKey(length)));
	}

	@GetMapping("/decrypt")
	public ResponseEntity<String> decrypt(@RequestBody String text) {
		return ResponseEntity.ok(SecurityManagerUtil.decrypt("AES", text, Base64.getDecoder().decode("z9x0/TNvFVgDg5VaVyuPzQnW/DJQzYUalpM6cgoeoV4=")));
	}

	@GetMapping("/get-trace")
	public BigDecimal getTrace(@RequestBody String text) {
		try {
			TraceDto traceDto = tonCoreServiceHelper.getTraceByTraceId(text);
			TraceDto.Trace trace = traceDto.getTraces().getFirst();
			List<String> txOrder = trace.getTransactionsOrder();
			Map<String, TraceDto.Trace.Transaction> txMap = trace.getTransactions();

			TraceDto.Trace.Transaction tx1 = txMap.get(txOrder.getFirst());
			String fee1 = tx1.getTotalFees();
			String fwdFee1 = tx1.getOutMsgs().getFirst().getFwdFee();

			TraceDto.Trace.Transaction tx2 = txMap.get(txOrder.get(1));
			String fee2 = tx2.getTotalFees();
			TraceDto.Trace.Transaction.Message msg2First = tx2.getOutMsgs().getFirst();
			String fwdFee2 = msg2First.getFwdFee();
			String msg2Value = msg2First.getValue();

			TraceDto.Trace.Transaction lastTx = txMap.get(txOrder.getLast());
			if ("1".equalsIgnoreCase(lastTx.getInMsg().getValue()) && txOrder.size() > 4) {
				lastTx = txMap.get(txOrder.get(4));
			}

			String lastFee = lastTx.getTotalFees();
			String lastInValue = lastTx.getInMsg().getValue();

			return new BigDecimal(fee1).add(new BigDecimal(fwdFee1))
					.add(new BigDecimal(fee2))
					.add(new BigDecimal(fwdFee2))
					.add(new BigDecimal(lastFee))
					.add(new BigDecimal(msg2Value).subtract(new BigDecimal(lastInValue)));

		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_ON_FETCHING_TRACE, text), e);
			return BigDecimal.ZERO;
		}
	}

}
