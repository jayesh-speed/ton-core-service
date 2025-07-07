--liquibase formatted sql

--changeset addTonTables:1
CREATE TABLE speed_node.tbl_ton_fee_accounts
(
    `id`                 varchar(40)    NOT NULL,
    `address`            varchar(80)    NOT NULL,
    `secret_key`         varchar(255)   NOT NULL,
    `public_key`         varchar(255)            DEFAULT NULL,
    `deployment_tx_hash` varchar(80)             DEFAULT NULL,
    `wallet_id`          int unsigned  NOT NULL,
    `chain_id`           tinyint unsigned NOT NULL,
    `wallet_type`        varchar(30)    NOT NULL,
    `ton_balance`        decimal(19, 9) NOT NULL DEFAULT 0,
    `main_net`           tinyint(1) DEFAULT 0,
    `created`            bigint(20) NOT NULL,
    `modified`           bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    index                chain_id (`chain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_jettons
(
    `id`                    varchar(40) NOT NULL,
    `jetton_master_address` varchar(80) NOT NULL,
    `jetton_name`           varchar(30) NOT NULL,
    `jetton_symbol`         varchar(30) NOT NULL,
    `main_net`              tinyint(1)        DEFAULT 0,
    `chain_id`              tinyint unsigned  NOT NULL,
    `decimals`              int         NOT NULL,
    `no_of_cell`            tinyint unsigned  DEFAULT NULL,
    `no_of_bits`            int    DEFAULT NULL,
    `gas_unit`              int unsigned  DEFAULT NULL,
    `deployment_cost`       bigint DEFAULT NULL,
    `reserve_storage_fee`   bigint DEFAULT NULL,
    `created`               bigint(20)        NOT NULL,
    `modified`              bigint(20)        NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_jetton_master_address_chain` (`jetton_master_address`, `chain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_listeners
(
    `id`       varchar(40) NOT NULL,
    `status`   varchar(10) NOT NULL,
    `chain_id` tinyint unsigned NOT NULL,
    `main_net` tinyint(1)       DEFAULT 0,
    `created`  bigint(20)       NOT NULL,
    `modified` bigint(20)       NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chain_id` (`chain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_main_accounts
(
    `id`                    varchar(40)    NOT NULL,
    `address`               varchar(80)    NOT NULL,
    `public_key`            varchar(255)            DEFAULT NULL,
    `secret_key`            varchar(255)   NOT NULL,
    `deployment_tx_hash`    varchar(100)            DEFAULT NULL,
    `wallet_id`             int unsigned NOT NULL,
    `wallet_type`           varchar(30)    NOT NULL,
    `ton_balance`           decimal(19, 9) NOT NULL DEFAULT 0,
    `jetton_master_address` varchar(80)    NOT NULL,
    `jetton_wallet_address` varchar(80)             DEFAULT NULL,
    `chain_id`              tinyint unsigned NOT NULL,
    `main_net`              tinyint(1)   DEFAULT 0,
    `created`               bigint(20)   NOT NULL,
    `modified`              bigint(20)   NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_on_chain_tx
(
    `id`                     varchar(40)     NOT NULL,
    `jetton_master_address`  varchar(80)     NOT NULL,
    `from_address`           varchar(80)     NOT NULL,
    `to_address`             varchar(80)     NOT NULL,
    `amount`                 decimal(32, 16) NOT NULL,
    `transaction_hash`       varchar(80)     NOT NULL,
    `trace_id`               varchar(80)    DEFAULT NULL,
    `transaction_type`       varchar(20)     NOT NULL,
    `transaction_status`     tinyint        DEFAULT NULL,
    `main_net`               tinyint(1)      DEFAULT 0,
    `chain_id`               tinyint unsigned NOT NULL,
    `transaction_fee`        decimal(19, 9) DEFAULT NULL,
    `timestamp`              bigint(20)      NOT NULL,
    `tx_reference`           varchar(255)   DEFAULT NULL,
    `confirmation_timestamp` bigint(20)     DEFAULT NULL,
    `logical_time`           bigint(20)      DEFAULT NULL,
    `transaction_date`       datetime        NOT NULL,
    `created`                bigint(20)      NOT NULL,
    `modified`               bigint(20)      NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT txhash_to_token UNIQUE (`transaction_hash`, `to_address`, `jetton_master_address`, `transaction_type`),
    INDEX                    chain_id_logical_time (`chain_id`, `logical_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_sweep_tx
(
    `id`                     varchar(40)  NOT NULL,
    `jetton_master_address`  varchar(80)  NOT NULL,
    `from_address`           varchar(80)  NOT NULL,
    `to_address`             varchar(80)  NOT NULL,
    `amount`                 decimal(32, 16) DEFAULT NULL,
    `transaction_hash`       varchar(80)  NOT NULL,
    `trace_id`               varchar(80)     DEFAULT NULL,
    `transaction_status`     tinyint         DEFAULT NULL,
    `main_net`               tinyint(1)      NOT NULL,
    `chain_id`               tinyint unsigned NOT NULL,
    `transaction_fee`        decimal(32, 16) DEFAULT NULL,
    `timestamp`              bigint(20)      NOT NULL,
    `tx_reference`           varchar(255) NOT NULL,
    `confirmation_timestamp` bigint(20)     DEFAULT NULL,
    `logical_time`           bigint(20)      DEFAULT NULL,
    `transaction_date`       datetime     NOT NULL,
    `created`                bigint(20)      NOT NULL,
    `modified`               bigint(20)      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_used_wallet_address
(
    `id`          varchar(40)  NOT NULL,
    `address`     varchar(80)  NOT NULL,
    `public_key`  varchar(255) DEFAULT NULL,
    `secret_key`  varchar(255) NOT NULL,
    `wallet_id`   int unsigned NOT NULL,
    `wallet_type` varchar(30)  NOT NULL,
    `chain_id`    tinyint unsigned NOT NULL,
    `main_net`    tinyint(1)      DEFAULT 0,
    `created`     bigint(20)      NOT NULL,
    `modified`    bigint(20)      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_wallet_address
(
    `id`          varchar(40)  NOT NULL,
    `address`     varchar(80)  NOT NULL,
    `public_key`  varchar(255) DEFAULT NULL,
    `secret_key`  varchar(255) NOT NULL,
    `wallet_id`   int unsigned NOT NULL,
    `wallet_type` varchar(30)  NOT NULL,
    `chain_id`    tinyint unsigned NOT NULL,
    `main_net`    tinyint(1)      DEFAULT 0,
    `created`     bigint(20)      NOT NULL,
    `modified`    bigint(20)      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE speed_node.tbl_ton_withdraw_process
(
    `id`                    varchar(40) NOT NULL,
    `reference_id`          varchar(40) NOT NULL,
    `account_id`            varchar(40)     DEFAULT NULL,
    `withdraw_request`      varchar(80) NOT NULL,
    `transaction_hash`      varchar(80)     DEFAULT NULL,
    `target_amount`         decimal(32, 16) DEFAULT NULL,
    `target_currency`       varchar(100)    DEFAULT NULL,
    `status`                varchar(50)     DEFAULT NULL,
    `reference`             varchar(40)     DEFAULT NULL,
    `actual_fee`            decimal(32, 16) DEFAULT NULL,
    `withdraw_type`         varchar(50)     DEFAULT NULL,
    `tx_reference`          varchar(255)    DEFAULT NULL,
    `failure_reason`        varchar(255)    DEFAULT NULL,
    `target_amount_paid_at` bigint(20)      DEFAULT NULL,
    `address`               varchar(80)     DEFAULT NULL,
    `main_net`              boolean         DEFAULT false,
    `created`               bigint(20)      NOT NULL,
    `modified`              bigint(20)      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
