-- MySQL dump 10.13  Distrib 8.4.11, for macos15 (arm64)
--
-- Host: localhost    Database: ecommerce_db
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `customer_id_sequence`
--

DROP TABLE IF EXISTS `customer_id_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_id_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_id_sequence`
--

LOCK TABLES `customer_id_sequence` WRITE;
/*!40000 ALTER TABLE `customer_id_sequence` DISABLE KEYS */;
INSERT INTO `customer_id_sequence` VALUES (1000000,'2026-11-01 03:44:40.924104'),(1000001,'2026-11-01 03:50:21.937935'),(1000002,'2026-11-01 05:37:33.733476'),(1000003,'2026-11-01 17:07:10.000000'),(1000004,'2026-11-02 02:54:44.301182'),(1000005,'2026-11-02 09:08:33.255391');
/*!40000 ALTER TABLE `customer_id_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `user_account_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `emial` (`email`),
  UNIQUE KEY `uk_customers_user_account` (`user_account_id`),
  CONSTRAINT `fk_customers_user_account` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (7401,'Rakesh Bangaru','rakesh.updated@example.com',NULL),(8002,'Alice','alice.test@yahoo.com',NULL),(8101,'Rakesh','rakesh.payment@example.com',NULL),(8201,'Rakesh','rakesh.checkout@example.com',NULL),(1000000,'Rakesh Bangaru','rakeshbangaru22@gmail.com',4),(1000001,'V7 Checkout Test','v7checkout@test.com',5),(1000003,'Tech Store Seller','seller1@test.com',6),(1000004,'Demo Customer','demo.customer@test.com',7),(1000005,'Bangaru Rakesh','bangarurakesh288083@gmail.com',8);
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-10-31 03:58:02',0,1),(2,'2','extend products for catalog','SQL','V2__extend_products_for_catalog.sql',435364161,'root','2026-10-31 03:58:02',23,1),(3,'3','create users','SQL','V3__create_users.sql',-38901785,'root','2026-10-31 04:14:54',21,1),(4,'4','create sellers','SQL','V4__create_sellers.sql',-546617230,'root','2026-10-31 13:27:01',25,1),(5,'5','add seller to products','SQL','V5__add_seller_to_products.sql',386134865,'root','2026-10-31 13:27:01',34,1),(6,'6','link customers to users','SQL','V6__link_customers_to_users.sql',-1687419951,'root','2026-11-01 03:44:01',44,1),(7,'7','create customer id sequence','SQL','V7__create_customer_id_sequence.sql',1014925724,'root','2026-11-01 03:44:01',5,1),(8,'8','snapshot seller on order items','SQL','V8__snapshot_seller_on_order_items.sql',270445463,'root','2026-11-01 10:27:00',46,1),(9,'9','add user auth version','SQL','V9__add_user_auth_version.sql',-1074775971,'root','2026-11-02 02:05:21',38,1),(10,'10','auto generate product ids','SQL','V10__auto_generate_product_ids.sql',26152672,'root','2026-11-02 13:09:51',72,1),(11,'11','create product images','SQL','V11__create_product_images.sql',-1807282825,'root','2026-11-02 14:25:32',39,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`product_id`),
  CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `chk_inventory_quantity` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (7001,14),(7101,2),(7201,1),(8803,25),(8804,8),(8805,10),(8806,16),(8807,30),(8808,20),(8809,35),(8810,50),(8811,40),(8812,25),(8813,45),(8814,30),(8815,40),(8816,30),(8817,20),(8818,35);
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `seller_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_id`,`product_id`),
  KEY `idx_order_items_seller_id` (`seller_id`),
  KEY `fk_order_items_product` (`product_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_order_items_seller` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`),
  CONSTRAINT `chk_order_items_quantity` CHECK ((`quantity` > 0)),
  CONSTRAINT `chk_order_items_unit_price` CHECK ((`unit_price` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (6001,7101,1,75000.00,NULL),(9901001,7001,2,80000.00,1),(1789442473124,7001,1,75000.00,1),(1791090086633,7201,2,75000.00,NULL),(1792812223246,7101,1,75000.00,NULL),(1792814183494,7201,1,75000.00,NULL),(1792814350205,7001,1,80000.00,1),(1792814350205,7201,1,75000.00,NULL),(1793505411427,7001,1,80000.00,1),(1793506348886,7001,1,80000.00,1),(1793506371553,7001,1,80000.00,1),(1793506371553,7101,1,75000.00,NULL),(1793506371553,7201,2,75000.00,NULL),(1793533271592,7101,1,75000.00,NULL),(1793533271592,7201,3,75000.00,NULL),(1793533298129,7001,3,80000.00,1),(1793614459714,7001,1,80000.00,1);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `status` varchar(30) NOT NULL,
  `payment_type` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_orders_customer` (`customer_id`),
  CONSTRAINT `fk_orders_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `chk_orders_payment_type` CHECK ((`payment_type` in (_utf8mb4'CARD',_utf8mb4'UPI',_utf8mb4'WALLET'))),
  CONSTRAINT `chk_orders_status` CHECK ((`status` in (_utf8mb4'CREATED',_utf8mb4'PAYMENT_PENDING',_utf8mb4'PAID',_utf8mb4'PROCESSING',_utf8mb4'SHIPPED',_utf8mb4'DELIVERED',_utf8mb4'PAYMENT_FAILED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (6001,8101,'SHIPPED','CARD','2026-09-14 09:59:46'),(9901001,8101,'DELIVERED','CARD','2026-10-23 08:29:55'),(1789442473124,8201,'CANCELLED','CARD','2026-09-15 08:51:13'),(1791090086633,8101,'PAID','UPI','2026-10-04 10:31:27'),(1792812223246,8101,'PAID','UPI','2026-10-24 08:53:43'),(1792814183494,8101,'PAID','WALLET','2026-10-24 09:26:24'),(1792814350205,8101,'PAID','CARD','2026-10-24 09:29:10'),(1793505411427,1000000,'PAID','CARD','2026-11-01 09:26:52'),(1793506348886,1000001,'PAID','CARD','2026-11-01 09:42:29'),(1793506371553,1000001,'PAID','CARD','2026-11-01 09:42:52'),(1793533271592,1000003,'PAID','CARD','2026-11-01 17:11:12'),(1793533298129,1000003,'PAID','CARD','2026-11-01 17:11:38'),(1793614459714,1000005,'PAID','CARD','2026-11-02 15:44:20');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `payment_type` varchar(20) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `successful` tinyint(1) NOT NULL,
  `message` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_payments_order` (`order_id`),
  CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_payment_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=1970 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1902,6001,'CARD',75000.00,0,'First attempt failed','2026-09-14 09:59:46'),(1903,6001,'CARD',75000.00,1,'Second attempt successful','2026-09-14 09:59:46'),(1955,1789442473124,'CARD',75000.00,1,'Card payment successful','2026-09-15 08:51:13'),(1959,1791090086633,'UPI',150000.00,1,'UPI payment successful','2026-10-04 10:31:27'),(1960,9901001,'CARD',160000.00,1,'Card payment successful','2026-10-23 08:29:55'),(1961,1792812223246,'UPI',75000.00,1,'UPI payment successful','2026-10-24 08:53:43'),(1962,1792814183494,'WALLET',75000.00,1,'Wallet payment successful','2026-10-24 09:26:24'),(1963,1792814350205,'CARD',155000.00,1,'Card payment successful','2026-10-24 09:29:10'),(1964,1793505411427,'CARD',80000.00,1,'Card payment successful','2026-11-01 09:26:52'),(1965,1793506348886,'CARD',80000.00,1,'Card payment successful','2026-11-01 09:42:29'),(1966,1793506371553,'CARD',305000.00,1,'Card payment successful','2026-11-01 09:42:52'),(1967,1793533271592,'CARD',300000.00,1,'Card payment successful','2026-11-01 17:11:12'),(1968,1793533298129,'CARD',240000.00,1,'Card payment successful','2026-11-01 17:11:38'),(1969,1793614459714,'CARD',80000.00,1,'Card payment successful','2026-11-02 15:44:20');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `storage_key` varchar(512) NOT NULL,
  `original_filename` varchar(255) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `file_size` bigint NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_primary` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `primary_product_id` bigint GENERATED ALWAYS AS ((case when (`is_primary` = 1) then `product_id` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_images_storage_key` (`storage_key`),
  UNIQUE KEY `uk_product_images_primary` (`primary_product_id`),
  KEY `idx_product_images_product_order` (`product_id`,`sort_order`,`id`),
  CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `chk_product_images_file_size` CHECK ((`file_size` > 0)),
  CONSTRAINT `chk_product_images_sort_order` CHECK ((`sort_order` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` (`id`, `product_id`, `storage_key`, `original_filename`, `content_type`, `file_size`, `sort_order`, `is_primary`, `created_at`) VALUES (1,8802,'973bcce0-c237-4fda-9597-bcf7921a5ee0.png','keyboard.jpeg','image/png',1925189,0,0,'2026-11-03 07:50:20.919780'),(3,8802,'8c6413e3-ec5e-4141-9b47-f38f23654070.png','Rainbow-lit mechanical keyboard on wood desk.png','image/png',3061503,1,1,'2026-11-03 08:29:18.598559'),(4,7001,'364889a0-6926-455a-b702-42fd5d68e457.png','laptop.png','image/png',45787,0,1,'2026-11-03 10:49:08.579110'),(5,7101,'0ea64f78-6372-4741-b06c-6fe4dc4ceb3d.png','laptop.png','image/png',45787,0,1,'2026-11-03 10:49:08.614811'),(6,7201,'71222b9a-8fa5-4da4-ba0c-6eaeac9ff230.png','laptop.png','image/png',45787,0,1,'2026-11-03 10:49:08.633791'),(7,8803,'53fc1e46-c81d-449f-94e0-012ceb6b5456.png','headphones.png','image/png',36333,0,1,'2026-11-03 11:07:16.900973'),(8,8804,'beeb660d-4385-4c62-947b-92e6549b5d44.png','refrigerator.png','image/png',42815,0,1,'2026-11-03 11:07:16.928605'),(9,8805,'c49c8c68-9298-44dd-bcc8-cb94e4fc23fa.png','washing-machine.png','image/png',54493,0,1,'2026-11-03 11:07:16.946026'),(10,8806,'454088ac-4557-44c6-bc3d-4d74c6f3c9b2.png','microwave.png','image/png',33567,0,1,'2026-11-03 11:07:16.960057'),(11,8807,'47c0f780-d35d-48f4-b62f-15d38712ef5f.png','smartphone.png','image/png',42379,0,1,'2026-11-03 11:07:16.972288'),(12,8808,'eef73504-51f7-4da9-bafc-4817cc0bca17.png','tablet.png','image/png',51511,0,1,'2026-11-03 11:07:16.984275'),(13,8809,'ffd624b1-14c2-4f63-800e-24283ed39485.png','smartwatch.png','image/png',33212,0,1,'2026-11-03 11:07:16.993689'),(14,8810,'be1428c8-c3c2-46a4-9f8d-4aa70ca88ac6.png','tshirt.png','image/png',51444,0,1,'2026-11-03 11:07:17.003314'),(15,8811,'8d8bb876-82d9-4f16-aed1-d18ca307aed6.png','jeans.png','image/png',58571,0,1,'2026-11-03 11:07:17.013355'),(16,8812,'3e3737a8-2f0b-4ec6-a5c3-7a654c817ad9.png','sneakers.png','image/png',35006,0,1,'2026-11-03 11:07:17.022715'),(17,8813,'c0221d8c-9f91-4417-a029-bebf8356b7c5.png','book-java.png','image/png',53352,0,1,'2026-11-03 11:07:17.033214'),(18,8814,'65838e69-3ef0-4684-b372-4f0727a24a50.png','book-spring.png','image/png',54918,0,1,'2026-11-03 11:07:17.045110'),(19,8815,'578857ea-c02e-40a9-9fe3-dc7d5c1edbba.png','book-dsa.png','image/png',51970,0,1,'2026-11-03 11:07:17.056353'),(20,8816,'ecd4179b-d722-4bcf-b189-d30b0d08efcb.png','football.png','image/png',39035,0,1,'2026-11-03 11:07:17.066236'),(21,8817,'2ed18884-b4e6-4f6b-bf5d-04176092aee1.png','cricket-bat.png','image/png',34205,0,1,'2026-11-03 11:07:17.074705'),(22,8818,'36013fe4-1162-4464-8abf-0eef4de38492.png','yoga-mat.png','image/png',51504,0,1,'2026-11-03 11:07:17.084599');
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `category` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `brand` varchar(120) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `seller_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_products_seller_id` (`seller_id`),
  CONSTRAINT `fk_products_seller` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8819 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (7001,'Test Laptop','Electronics',80000.00,NULL,NULL,NULL,1,1),(7101,'Laptop','Electronics',75000.00,NULL,NULL,NULL,1,NULL),(7201,'Laptop','Electronics',75000.00,NULL,NULL,NULL,1,NULL),(7401,'Wireless Mouse','Electronics',2500.00,NULL,NULL,NULL,1,NULL),(8801,'Audit Test Product','Testing',999.00,NULL,NULL,NULL,1,NULL),(8802,'Mechanical Keyboard','Electronics',2499.00,NULL,NULL,NULL,1,NULL),(8803,'Headphones','Electronics',2999.00,'Wireless over-ear headphones','Demo Audio',NULL,1,NULL),(8804,'Refrigerator','Home Appliances',34999.00,'Double-door energy-efficient refrigerator','Demo Home',NULL,1,NULL),(8805,'Washing Machine','Home Appliances',26999.00,'Fully automatic front-load washing machine','Demo Home',NULL,1,NULL),(8806,'Microwave','Home Appliances',6999.00,'Compact microwave oven','Demo Home',NULL,1,NULL),(8807,'Smartphone','Mobiles',29999.00,'5G smartphone with high-resolution display','Demo Mobile',NULL,1,NULL),(8808,'Tablet','Mobiles',19999.00,'Portable tablet for entertainment and study','Demo Mobile',NULL,1,NULL),(8809,'Smartwatch','Mobiles',3999.00,'Smartwatch with activity tracking','Demo Wearables',NULL,1,NULL),(8810,'T-Shirt','Fashion',799.00,'Comfortable cotton crew-neck T-shirt','Demo Fashion',NULL,1,NULL),(8811,'Jeans','Fashion',1499.00,'Classic denim jeans','Demo Fashion',NULL,1,NULL),(8812,'Sneakers','Fashion',2999.00,'Comfortable everyday sneakers','Demo Footwear',NULL,1,NULL),(8813,'Java Programming Book','Books',899.00,'Introduction to Java and object-oriented programming','Demo Books',NULL,1,NULL),(8814,'Spring Boot Book','Books',999.00,'Building REST APIs and applications with Spring Boot','Demo Books',NULL,1,NULL),(8815,'DSA Book','Books',799.00,'Data structures and algorithms practice guide','Demo Books',NULL,1,NULL),(8816,'Football','Sports',1299.00,'Durable football for recreational play','Demo Sports',NULL,1,NULL),(8817,'Cricket Bat','Sports',1499.00,'Cricket bat for practice and recreational matches','Demo Sports',NULL,1,NULL),(8818,'Yoga Mat','Sports',599.00,'Non-slip exercise and yoga mat','Demo Fitness',NULL,1,NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sellers`
--

DROP TABLE IF EXISTS `sellers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sellers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `business_name` varchar(200) NOT NULL,
  `approval_status` varchar(30) NOT NULL DEFAULT 'PENDING',
  `approved_at` timestamp(6) NULL DEFAULT NULL,
  `approved_by_user_id` bigint DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sellers_user` (`user_id`),
  KEY `fk_sellers_approved_by` (`approved_by_user_id`),
  CONSTRAINT `fk_sellers_approved_by` FOREIGN KEY (`approved_by_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_sellers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sellers`
--

LOCK TABLES `sellers` WRITE;
/*!40000 ALTER TABLE `sellers` DISABLE KEYS */;
INSERT INTO `sellers` VALUES (1,6,'TechStore','APPROVED','2026-11-01 05:40:45.534194',1,'2026-11-01 05:40:45.534194','2026-11-01 05:40:45.534194');
/*!40000 ALTER TABLE `sellers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `role` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL,
  `auth_provider` varchar(30) NOT NULL,
  `provider_user_id` varchar(255) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `last_activity_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `auth_version` bigint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_provider_identity` (`auth_provider`,`provider_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'root.local@test.com','$2a$10$sLNUJABWNici.lx/LjvUMObgzJKZEJLOkVwr5/3pP0tsLhVTuhDVS','ROOT','ACTIVE','LOCAL',NULL,'2026-11-03 10:28:57.160831','2026-11-03 10:28:57.160831','2026-10-31 13:36:57.487962','2026-11-03 10:28:57.167936',1),(2,'support.local@test.com','$2a$10$AsgfEwTZKW1p..6C77SYZuEGvl9d89/UyOYYkuSuqAfgjwBf8MEsi','IT_SUPPORT','ACTIVE','LOCAL',NULL,'2026-11-02 10:03:50.428343','2026-11-02 10:03:50.428343','2026-10-31 13:39:16.081509','2026-11-02 10:03:50.429289',1),(3,'customer.local@test.com','$2a$10$DyYTTNqOTHTmwJQ.ICeX6OEwYm12x3UvxorLMu5jXIhtGQJvR9Gqu','CUSTOMER','ACTIVE','LOCAL',NULL,'2026-11-01 05:33:49.882010','2026-11-01 05:33:49.882010','2026-10-31 13:47:21.442679','2026-11-01 05:33:49.892189',1),(4,'rakeshbangaru22@gmail.com','$2a$10$M0SEycvrEzZCOGhB5f5MDe8bafPSQ9h7NJ12LCxJauCNU/nYtC6r2','ROOT','ACTIVE','LOCAL',NULL,'2026-11-02 09:23:01.541528','2026-11-02 09:23:01.541528','2026-11-01 03:44:40.913915','2026-11-03 10:29:08.217499',2),(5,'v7checkout@test.com','$2a$10$H7JPVaZyyw516nocjjjmIu/pr1dXj7cSC9wu9/CP8Y8fJL7patNA2','CUSTOMER','ACTIVE','LOCAL',NULL,'2026-11-01 10:27:33.672616','2026-11-01 10:27:33.672616','2026-11-01 03:50:21.934843','2026-11-01 10:27:33.681012',1),(6,'seller1@test.com','$2a$10$8iGvjOy.1xIqhSZgoq.H3unHvii2NASsgmU0sNe1KgpdXvB8.CGHW','SELLER_ADMIN','ACTIVE','LOCAL',NULL,'2026-11-03 05:21:46.169484','2026-11-03 05:21:46.169484','2026-11-01 05:37:33.725840','2026-11-03 05:21:46.176864',1),(7,'demo.customer@test.com','$2a$10$t1XPddC/T/A0XX4L3VQ0DuZ3FqZVVRtR.6OCXfWTr3tB.1BzSU7dy','CUSTOMER','ACTIVE','LOCAL',NULL,'2026-11-02 02:54:51.836987','2026-11-02 02:54:51.836987','2026-11-02 02:54:44.274486','2026-11-02 02:54:51.844504',1),(8,'bangarurakesh288083@gmail.com',NULL,'IT_SUPPORT','ACTIVE','GOOGLE','110254289786849225326','2026-11-02 10:17:44.505014','2026-11-02 10:17:44.505014','2026-11-02 08:43:00.751446','2026-11-02 10:17:44.506599',2);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-03 16:14:52
