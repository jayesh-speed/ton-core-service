package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TonTransactionDto {

	@JsonProperty("transactions")
	private List<TonTransaction> transactions;

	@JsonProperty("address_book")
	private Map<String, AddressBookEntry> addressBook;

	@Data
	public static class TonTransaction {

		private String account;
		private String hash;
		private String lt;
		private long now;

		@JsonProperty("mc_block_seqno")
		private long mcBlockSeqno;

		@JsonProperty("trace_id")
		private String traceId;

		@JsonProperty("prev_trans_hash")
		private String prevTransHash;

		@JsonProperty("prev_trans_lt")
		private String prevTransLt;

		@JsonProperty("orig_status")
		private String origStatus;

		@JsonProperty("end_status")
		private String endStatus;

		@JsonProperty("total_fees")
		private String totalFees;

		private TransactionDescription description;

		@JsonProperty("block_ref")
		private BlockRef blockRef;

		@JsonProperty("in_msg")
		private TransactionMessage inMsg;

		@JsonProperty("out_msgs")
		private List<TransactionMessage> outMsgs;

		@JsonProperty("account_state_before")
		private AccountState accountStateBefore;

		@JsonProperty("account_state_after")
		private AccountState accountStateAfter;
	}

	@Data
	public static class TransactionDescription {

		private String type;
		private boolean aborted;
		private boolean destroyed;

		@JsonProperty("credit_first")
		private boolean creditFirst;

		@JsonProperty("storage_ph")
		private StoragePhase storagePh;

		@JsonProperty("compute_ph")
		private ComputePhase computePh;

		private ActionPhase action;
	}

	@Data
	public static class StoragePhase {

		@JsonProperty("storage_fees_collected")
		private String storageFeesCollected;

		@JsonProperty("status_change")
		private String statusChange;
	}

	@Data
	public static class ComputePhase {

		private boolean skipped;
		private boolean success;

		@JsonProperty("msg_state_used")
		private boolean msgStateUsed;

		@JsonProperty("account_activated")
		private boolean accountActivated;

		@JsonProperty("gas_fees")
		private String gasFees;

		@JsonProperty("gas_used")
		private String gasUsed;

		@JsonProperty("gas_limit")
		private String gasLimit;

		@JsonProperty("gas_credit")
		private String gasCredit;

		private int mode;

		@JsonProperty("exit_code")
		private int exitCode;

		@JsonProperty("vm_steps")
		private int vmSteps;

		@JsonProperty("vm_init_state_hash")
		private String vmInitStateHash;

		@JsonProperty("vm_final_state_hash")
		private String vmFinalStateHash;
	}

	@Data
	public static class ActionPhase {

		private boolean success;
		private boolean valid;

		@JsonProperty("no_funds")
		private boolean noFunds;

		@JsonProperty("status_change")
		private String statusChange;

		@JsonProperty("total_fwd_fees")
		private String totalFwdFees;

		@JsonProperty("total_action_fees")
		private String totalActionFees;

		@JsonProperty("result_code")
		private int resultCode;

		@JsonProperty("tot_actions")
		private int totActions;

		@JsonProperty("spec_actions")
		private int specActions;

		@JsonProperty("skipped_actions")
		private int skippedActions;

		@JsonProperty("msgs_created")
		private int msgsCreated;

		@JsonProperty("action_list_hash")
		private String actionListHash;

		@JsonProperty("tot_msg_size")
		private MsgSize totMsgSize;
	}

	@Data
	public static class MsgSize {

		private String cells;
		private String bits;
	}

	@Data
	public static class BlockRef {

		private int workchain;
		private String shard;
		private long seqno;
	}

	@Data
	public static class TransactionMessage {

		private String hash;
		private String source;
		private String destination;
		private String value;

		@JsonProperty("fwd_fee")
		private String fwdFee;

		@JsonProperty("ihr_fee")
		private String ihrFee;

		@JsonProperty("created_lt")
		private String createdLt;

		@JsonProperty("created_at")
		private String createdAt;

		private String opcode;

		@JsonProperty("ihr_disabled")
		private Boolean ihrDisabled;

		private Boolean bounce;
		private Boolean bounced;

		@JsonProperty("import_fee")
		private String importFee;

		@JsonProperty("message_content")
		private MessageContent messageContent;

		@JsonProperty("init_state")
		private Object initState;
	}

	@Data
	public static class MessageContent {

		private String hash;
		private String body;
		private String decoded;
	}

	@Data
	public static class AccountState {

		private String hash;
		private String balance;

		@JsonProperty("account_status")
		private String accountStatus;

		@JsonProperty("frozen_hash")
		private String frozenHash;

		@JsonProperty("data_hash")
		private String dataHash;

		@JsonProperty("code_hash")
		private String codeHash;
	}

	@Data
	public static class AddressBookEntry {

		@JsonProperty("user_friendly")
		private String userFriendly;
	}
}

