-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: ganesh_utsav_db
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `auction_items`
--

DROP TABLE IF EXISTS `auction_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auction_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bid_amount` decimal(12,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `day_number` int DEFAULT NULL,
  `item_name` varchar(255) NOT NULL,
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','UPI') DEFAULT NULL,
  `payment_status` enum('PAID','PENDING') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `winner_name` varchar(255) NOT NULL,
  `festival_year_id` bigint DEFAULT NULL,
  `recorded_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9nb6gdadh08sp6t627r3ahvxf` (`festival_year_id`),
  KEY `FKcjjerw1n6drp19jr7k2a9e9pd` (`recorded_by_id`),
  CONSTRAINT `FK9nb6gdadh08sp6t627r3ahvxf` FOREIGN KEY (`festival_year_id`) REFERENCES `festival_years` (`id`),
  CONSTRAINT `FKcjjerw1n6drp19jr7k2a9e9pd` FOREIGN KEY (`recorded_by_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction_items`
--

LOCK TABLES `auction_items` WRITE;
/*!40000 ALTER TABLE `auction_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `auction_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donations`
--

DROP TABLE IF EXISTS `donations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `amount` decimal(12,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `donation_date` date NOT NULL,
  `donor_name` varchar(255) NOT NULL,
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','UPI') NOT NULL,
  `phone_number` varchar(15) NOT NULL,
  `receipt_number` varchar(30) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `recorded_by_id` bigint DEFAULT NULL,
  `festival_year_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK90ywd60lsbig35gpjly2rni9k` (`receipt_number`),
  KEY `FKsvpo9lcikujou6awaalcjyvyq` (`recorded_by_id`),
  KEY `FKli01nbmabqsg0efoghtb6y24v` (`festival_year_id`),
  CONSTRAINT `FKli01nbmabqsg0efoghtb6y24v` FOREIGN KEY (`festival_year_id`) REFERENCES `festival_years` (`id`),
  CONSTRAINT `FKsvpo9lcikujou6awaalcjyvyq` FOREIGN KEY (`recorded_by_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donations`
--

LOCK TABLES `donations` WRITE;
/*!40000 ALTER TABLE `donations` DISABLE KEYS */;
INSERT INTO `donations` VALUES (1,'',1001.00,'2026-08-22 10:00:32.815184','2026-08-22','Arun','CASH','7894567891','GU-2026-49474','2026-08-22 10:00:32.815184',1,1);
/*!40000 ALTER TABLE `donations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expenses`
--

DROP TABLE IF EXISTS `expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `bill_file_path` varchar(255) DEFAULT NULL,
  `category` enum('CULTURAL_PROGRAMS','ELECTRICITY_LIGHTING','FOOD_PRASAD','IDOL_MURTI','IMMERSION_VISARJAN','MISCELLANEOUS','PANDAL_DECORATION','PRIEST_POOJA_MATERIALS','SECURITY','SOUND_SYSTEM') NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `expense_date` date NOT NULL,
  `paid_to` varchar(255) NOT NULL,
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','UPI') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `approved_by_id` bigint DEFAULT NULL,
  `recorded_by_id` bigint DEFAULT NULL,
  `day_number` int DEFAULT NULL,
  `note` varchar(500) DEFAULT NULL,
  `festival_year_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfu6ll3ch4v8ornaeskppmr88m` (`approved_by_id`),
  KEY `FKc917qgd1qyrk4qrd2a5hthmki` (`recorded_by_id`),
  KEY `FKonh3r9x5llq1nkr9eq2eqfjmj` (`festival_year_id`),
  CONSTRAINT `FKc917qgd1qyrk4qrd2a5hthmki` FOREIGN KEY (`recorded_by_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKfu6ll3ch4v8ornaeskppmr88m` FOREIGN KEY (`approved_by_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKonh3r9x5llq1nkr9eq2eqfjmj` FOREIGN KEY (`festival_year_id`) REFERENCES `festival_years` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expenses`
--

LOCK TABLES `expenses` WRITE;
/*!40000 ALTER TABLE `expenses` DISABLE KEYS */;
INSERT INTO `expenses` VALUES (1,7000.00,NULL,'IDOL_MURTI','2026-08-22 10:57:49.001345','Ganesh Idol','2026-08-22','Lakshmi Idols','UPI','2026-08-22 10:57:49.001345',NULL,1,NULL,'',1);
/*!40000 ALTER TABLE `expenses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `festival_years`
--

DROP TABLE IF EXISTS `festival_years`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `festival_years` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `carry_forward_balance` decimal(12,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `duration_days` int NOT NULL,
  `label` varchar(255) NOT NULL,
  `start_date` date NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `year` int NOT NULL,
  `created_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5gtl57n7bgay0dcexn13xgcuh` (`label`),
  KEY `FKc91eau1ml2c7cckq2b28xutrs` (`created_by_id`),
  CONSTRAINT `FKc91eau1ml2c7cckq2b28xutrs` FOREIGN KEY (`created_by_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `festival_years`
--

LOCK TABLES `festival_years` WRITE;
/*!40000 ALTER TABLE `festival_years` DISABLE KEYS */;
INSERT INTO `festival_years` VALUES (1,_binary '',10000.00,'2026-08-22 09:58:58.560589',3,'2026 Ganesh Ustav','2026-09-14','2026-08-22 09:58:58.579929',2026,1);
/*!40000 ALTER TABLE `festival_years` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loan_repayments`
--

DROP TABLE IF EXISTS `loan_repayments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loan_repayments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `interest_portion` decimal(12,2) NOT NULL,
  `payment_amount` decimal(12,2) NOT NULL,
  `payment_date` date NOT NULL,
  `principal_portion` decimal(12,2) NOT NULL,
  `remaining_principal_after` decimal(12,2) NOT NULL,
  `loan_id` bigint NOT NULL,
  `recorded_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmvfjvk48bhsvwbdis0s9uwn1t` (`loan_id`),
  KEY `FKd723ir5223n8mf31auil3p8q8` (`recorded_by_id`),
  CONSTRAINT `FKd723ir5223n8mf31auil3p8q8` FOREIGN KEY (`recorded_by_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKmvfjvk48bhsvwbdis0s9uwn1t` FOREIGN KEY (`loan_id`) REFERENCES `loans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loan_repayments`
--

LOCK TABLES `loan_repayments` WRITE;
/*!40000 ALTER TABLE `loan_repayments` DISABLE KEYS */;
/*!40000 ALTER TABLE `loan_repayments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loans`
--

DROP TABLE IF EXISTS `loans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `borrower_name` varchar(255) NOT NULL,
  `borrower_phone` varchar(15) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `current_principal` decimal(12,2) NOT NULL,
  `last_interest_date` date NOT NULL,
  `loan_date` date NOT NULL,
  `monthly_interest_rate_percent` decimal(5,2) NOT NULL,
  `original_principal` decimal(12,2) NOT NULL,
  `status` enum('ACTIVE','CLOSED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `festival_year_id` bigint DEFAULT NULL,
  `recorded_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe9o4qnjf0bxxr70abxh6rmaui` (`festival_year_id`),
  KEY `FK4jet3cyddmh1vdkn5jowlyico` (`recorded_by_id`),
  CONSTRAINT `FK4jet3cyddmh1vdkn5jowlyico` FOREIGN KEY (`recorded_by_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKe9o4qnjf0bxxr70abxh6rmaui` FOREIGN KEY (`festival_year_id`) REFERENCES `festival_years` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loans`
--

LOCK TABLES `loans` WRITE;
/*!40000 ALTER TABLE `loans` DISABLE KEYS */;
/*!40000 ALTER TABLE `loans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `members`
--

DROP TABLE IF EXISTS `members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `role` enum('PRESIDENT','SECRETARY','TREASURER','VOLUNTEER') NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1ftq959eeh8vwc4ywlxwclqjj` (`phone`),
  UNIQUE KEY `UKlj4daw762ura5d2y6iu7g5n1i` (`username`),
  UNIQUE KEY `UK9d30a9u1qpg8eou0otgkwrp5d` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `members`
--

LOCK TABLES `members` WRITE;
/*!40000 ALTER TABLE `members` DISABLE KEYS */;
INSERT INTO `members` VALUES (1,_binary '','2026-08-21 16:01:09.000000','ramesh@example.com','Ramesh Patil','$2b$10$kvKhvV..gXo2i7Iw/H546OegP7FZaBRPBRwwN/u01aY173gvW2rI2','9876543210','PRESIDENT','ADMIN'),(2,_binary '','2026-08-21 16:48:40.466767','','SUNEEL','$2a$10$UI6STlxfq5pR.c4u8cJkgO3VGN2iVLzdWSSYUOWxvJMJY5l4GKDuq','8722967538','VOLUNTEER','suneel');
/*!40000 ALTER TABLE `members` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-22 12:25:25
