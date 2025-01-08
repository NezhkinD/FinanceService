CREATE TABLE IF NOT EXISTS `user`
(
    `id`       INT AUTO_INCREMENT PRIMARY KEY,
    `login`    VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `category`
(
    `id`      INT AUTO_INCREMENT PRIMARY KEY,
    `name`    VARCHAR(255) NOT NULL,
    `type`    VARCHAR(10)  NOT NULL,
    `user_id` INT          NOT NULL,
    `limitC`   FLOAT        NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_category` (`name`, `type`, `user_id`)
);

CREATE TABLE IF NOT EXISTS `budget`
(
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT          NOT NULL,
    `sum`         FLOAT        NOT NULL,
    FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE
);
