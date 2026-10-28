
#DROP DATABASE IF EXISTS `HoneyWeb`;
#CREATE DATABASE IF NOT EXISTS `HoneyWeb`;

#DROP TABLE IF EXISTS `main_logs`;
CREATE TABLE `main_logs` (
    `ID` int(11) NOT NULL AUTO_INCREMENT,
    `attackerIP` VARCHAR(45) NOT NULL,
    `attackerBrowser` VARCHAR(600) NOT NULL,
    `Source` VARCHAR(600) NOT NULL,
    `Value_Server`  LONGTEXT NOT NULL,
    `Value_Get`  LONGTEXT NOT NULL,
    `Value_Post`  LONGTEXT NOT NULL,
    `Value_Cookie`  LONGTEXT NOT NULL,
    `Creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `Module` VARCHAR(600) NOT NULL,   
    `download_checked` SMALLINT NOT NULL DEFAULT 0,
     PRIMARY KEY (`ID`)                                      
) ENGINE=MyISAM COMMENT='table containing log-data';

#DROP TABLE IF EXISTS `binary_tools`;
CREATE TABLE binary_tools (
     id_files int(11) unsigned NOT NULL,
     bin_data longblob NOT NULL,
     source_url tinytext NOT NULL,
     filename varchar(256) NOT NULL,
     filesize int(11) NOT NULL,
     filetype varchar(50) NOT NULL,
    `Creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
     #PRIMARY KEY (id_files)
) ENGINE=MyISAM COMMENT='automatically downloaded,malicious tools';
