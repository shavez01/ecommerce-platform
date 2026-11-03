-- V1 baseline schema for the existing ecommerce database.
-- Contains CREATE TABLE statements only.
-- Do not add DROP TABLE statements to this migration.

SET NAMES utf8mb4;

CREATE TABLE `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrfbvkrffamfql7cjmen8v976v` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgkh6xnlemabe7eck1gl0pa0or` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `available_quantity` int NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKaha8cxa4l2rcp2q8lnbxx0m6j` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `order_id` bigint NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `tax_amount` decimal(12,2) NOT NULL,
  `tax_rate` decimal(5,2) NOT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo45gewij3b7g1hohe7mf8ulmo` (`invoice_number`),
  UNIQUE KEY `UKjendrpp8pjicnmlv595kh9hnu` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `delivery_status` varchar(255) NOT NULL,
  `message` varchar(2000) DEFAULT NULL,
  `notification_type` varchar(255) NOT NULL,
  `order_id` bigint NOT NULL,
  `recipient_email` varchar(255) NOT NULL,
  `subject` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_order_type` (`order_id`,`notification_type`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `customer_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `status` varchar(255) NOT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_idempotency_key` (`idempotency_key`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `order_id` bigint NOT NULL,
  `payment_method` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `transaction_reference` varchar(255) NOT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh7rs09df50x2tpnp5yx4gs7cl` (`order_id`),
  UNIQUE KEY `UKetkoj7iao2lgbun6abfqy4mpb` (`transaction_reference`),
  UNIQUE KEY `uk_payment_idempotency_key` (`idempotency_key`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(12,2) NOT NULL,
  `sku` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5d59j8ichibxhmsg0owpghbj0` (`sku`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monolith_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3ra1h7jocd8pijihe9xaxu82w` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `product_id` varchar(255) NOT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_price` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `outbox_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aggregate_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `event_type` varchar(100) NOT NULL,
  `last_error` varchar(1000) DEFAULT NULL,
  `payload` tinytext NOT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  `retry_count` int NOT NULL,
  `status` varchar(20) NOT NULL,
  `topic` varchar(100) NOT NULL,
  `locked_until` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_outbox_status_created` (`status`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
