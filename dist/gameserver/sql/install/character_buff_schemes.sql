CREATE TABLE IF NOT EXISTS `character_buff_schemes` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`obj_Id` INT NOT NULL,
	`scheme_name` VARCHAR(16) NOT NULL,
	`buffs` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;