-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 27, 2016 at 12:03 PM
-- Server version: 5.7.16-0ubuntu0.16.04.1
-- PHP Version: 7.0.8-0ubuntu0.16.04.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `chat`
--

-- --------------------------------------------------------

--
-- Table structure for table `friends`
--

CREATE TABLE `friends` (
  `user_id` int(11) NOT NULL,
  `friend_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `message_id` int(11) NOT NULL,
  `message_sender_id` int(11) NOT NULL,
  `message_receiver_id` int(11) NOT NULL,
  `message_creation_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `message_status` varchar(1) DEFAULT NULL,
  `message_type` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `messages_files`
--

CREATE TABLE `messages_files` (
  `file_id` int(11) NOT NULL,
  `file_body` longtext,
  `file_filename` varchar(50) DEFAULT NULL,
  `message_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `messages_maps`
--

CREATE TABLE `messages_maps` (
  `map_id` int(11) NOT NULL,
  `map_latitude` varchar(100) DEFAULT NULL,
  `map_longitude` varchar(100) DEFAULT NULL,
  `message_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `messages_texts`
--

CREATE TABLE `messages_texts` (
  `text_id` int(11) NOT NULL,
  `text_body` varchar(500) DEFAULT NULL,
  `message_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `tokens`
--

CREATE TABLE `tokens` (
  `token_id` int(11) NOT NULL,
  `token_body` varchar(64) DEFAULT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `user_username` varchar(20) NOT NULL,
  `user_password` varchar(50) NOT NULL,
  `user_email` varchar(40) NOT NULL,
  `user_status_id` varchar(1) NOT NULL,
  `user_publickey` longtext NOT NULL,
  `user_date_creation` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `friends`
--
ALTER TABLE `friends`
  ADD PRIMARY KEY (`user_id`,`friend_id`);

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`message_id`,`message_sender_id`,`message_receiver_id`),
  ADD KEY `fk_messages_receiver_id` (`message_receiver_id`);

--
-- Indexes for table `messages_files`
--
ALTER TABLE `messages_files`
  ADD PRIMARY KEY (`file_id`,`message_id`),
  ADD UNIQUE KEY `message_id` (`message_id`);

--
-- Indexes for table `messages_maps`
--
ALTER TABLE `messages_maps`
  ADD PRIMARY KEY (`map_id`,`message_id`),
  ADD UNIQUE KEY `message_id` (`message_id`);

--
-- Indexes for table `messages_texts`
--
ALTER TABLE `messages_texts`
  ADD PRIMARY KEY (`text_id`,`message_id`),
  ADD UNIQUE KEY `message_id` (`message_id`);

--
-- Indexes for table `tokens`
--
ALTER TABLE `tokens`
  ADD PRIMARY KEY (`token_id`,`user_id`),
  ADD KEY `fk_token_userid` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `user_username` (`user_username`),
  ADD UNIQUE KEY `user_email` (`user_email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `message_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;
--
-- AUTO_INCREMENT for table `messages_files`
--
ALTER TABLE `messages_files`
  MODIFY `file_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `messages_maps`
--
ALTER TABLE `messages_maps`
  MODIFY `map_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `messages_texts`
--
ALTER TABLE `messages_texts`
  MODIFY `text_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;
--
-- AUTO_INCREMENT for table `tokens`
--
ALTER TABLE `tokens`
  MODIFY `token_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `friends`
--
ALTER TABLE `friends`
  ADD CONSTRAINT `fk_friends_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `fk_messages_receiver_id` FOREIGN KEY (`message_receiver_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `messages_files`
--
ALTER TABLE `messages_files`
  ADD CONSTRAINT `fk_messages_files` FOREIGN KEY (`message_id`) REFERENCES `messages` (`message_id`);

--
-- Constraints for table `messages_maps`
--
ALTER TABLE `messages_maps`
  ADD CONSTRAINT `fk_messages_maps` FOREIGN KEY (`message_id`) REFERENCES `messages` (`message_id`);

--
-- Constraints for table `messages_texts`
--
ALTER TABLE `messages_texts`
  ADD CONSTRAINT `fk_messages_texts` FOREIGN KEY (`message_id`) REFERENCES `messages` (`message_id`);

--
-- Constraints for table `tokens`
--
ALTER TABLE `tokens`
  ADD CONSTRAINT `fk_token_userid` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
