ALTER TABLE user ADD emailverif VARCHAR(100);


ALTER TABLE user MODIFY COLUMN password_hash varchar(100) ;
ALTER TABLE user MODIFY COLUMN email         varchar(100);
