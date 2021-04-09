

create table if not exists user
(
    id            bigint auto_increment primary key,
    password_hash varchar(300)  null,
    email         varchar(250)  not null,
    is_Active     bit default 0 not null,
    Name          varchar(100)  null,
    firstname       varchar(100)  null,
    street varchar(100) null,
    housenr varchar(12) null,
    zipcode varchar(5) null,
    birthdate  datetime null,
    VerificationStatus int not null default 0,
    categories_bitstring int unsigned not null default 0,
    Admin boolean not null default false,
    verified_by_id bigint null,
    comments_read bigint default 0 not null,
    #commentrating_weight double default 1.0 not null,
    commentwrite_weight double default 1.0 not null,
    avg_rating double default 0.0 not null,
    phonenr varchar(20),
    FOREIGN KEY (verified_by_id)
        REFERENCES user (id) ON UPDATE NO ACTION ON DELETE SET NULL ,
    verified_date datetime null,
    created_time      timestamp default current_timestamp()   not null
) DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_unicode_ci;


INSERT INTO user (password_hash, email, is_Active, VerificationStatus, Admin)
VALUES ('$2a$13$MHpeR.EAugDk95/cvuaIzuW2m1mMbxxFOe0c1Dx6W7Oq7vefl9QQm', 'felix@montenegros.de', 1, 3, true);


create table parliament
(
    id         bigint auto_increment PRIMARY KEY,
    name       varchar(80)                           null,
    level      tinyint,
    upper_parliament_id bigint null,
    FOREIGN KEY (upper_parliament_id)
        REFERENCES parliament (id) ON UPDATE NO ACTION ON DELETE NO ACTION,

    constraint id
        unique (id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;

create table user_parliament_access
(
    user_id bigint not null,
    FOREIGN KEY (user_id)
        REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE ,
    parliament_id bigint not null,
    FOREIGN KEY (parliament_id)
        REFERENCES parliament (id) ON UPDATE CASCADE ON DELETE CASCADE,
    voteaccess boolean not null default false,
    PRIMARY KEY (user_id, parliament_id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;


create table local_franchise
(
    zipcode             varchar(5)   not   null,
    parliament_id    bigint not null,
    FOREIGN KEY (parliament_id)
        REFERENCES parliament (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    PRIMARY KEY (zipcode, parliament_id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;



create table party
(
    id              bigint auto_increment PRIMARY KEY,
    Name       varchar(80)                           null,
    constraint id
        unique (id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;


create table party_parliament_seats
(
    party_id bigint,
    parliament_id bigint not null,
    seats int not null,
    from_date       datetime not null,
    FOREIGN KEY (party_id)
        REFERENCES party (id) ON UPDATE NO ACTION ON DELETE SET NULL,
    FOREIGN KEY (parliament_id)
        REFERENCES parliament (id) ON UPDATE NO ACTION ON DELETE CASCADE
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;

create table follows
(
    follower_id bigint not null,
    followee_id bigint not null,
    FOREIGN KEY (follower_id)
    REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (followee_id)
    REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY(follower_id, followee_id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;



create table bill
(
    id              bigint auto_increment PRIMARY KEY,
    Name       varchar(200)                           not null,
    date_presented       datetime                              null,
    date_vote       datetime                              null,
    party_id bigint null,
    FOREIGN KEY (party_id)
        REFERENCES party (id) ON UPDATE NO ACTION ON DELETE SET NULL ,
    parliament_id bigint not null,
    parliament_role int not null,
    FOREIGN KEY (parliament_id)
        REFERENCES parliament (id) ON UPDATE NO ACTION ON DELETE CASCADE ,
    procedurekey varchar(40) null,
    billtype varchar(40) null,
    resolutionrecommendation varchar(200) null,
    status int null,
    final_yes_votes int null,
    final_no_votes int null,
    created_by_id       bigint,
    read_count    bigint not null,
    read_detail_count    bigint not null,
    relative_value double,
    ranking double,
    categories_bitstring int unsigned not null default 0,
    abstract varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    avgRelRatingPro double null,
    avgRelRatingContra double null,
    FOREIGN KEY (created_by_id)
        REFERENCES user (id) ON UPDATE NO ACTION ON DELETE SET NULL ,

    created_time      timestamp default current_timestamp()   not null,
    constraint id
        unique (id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;









create table user_bill_vote
(
    bill_id  bigint not null,
    user_id    bigint not null,
    vote boolean not null,
    FOREIGN KEY (bill_id)
        REFERENCES bill (id) ON UPDATE NO ACTION ON DELETE CASCADE ,
    FOREIGN KEY (user_id)
        REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;


create table representatives_bill_vote
(
    bill_id  bigint null,
    party_id    bigint,
    yesvotes int not null,
    novotes int not null,
    abstinences int not null,
    FOREIGN KEY (bill_id)
        REFERENCES bill (id) ON UPDATE NO ACTION ON DELETE CASCADE ,
    FOREIGN KEY (party_id)
        REFERENCES party (id) ON UPDATE CASCADE ON DELETE SET NULL
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;






create table comment
(
    id            bigint auto_increment primary key,
    bill_id  bigint not null,
    FOREIGN KEY (bill_id)
        REFERENCES bill (id) ON UPDATE NO ACTION ON DELETE CASCADE ,
    user_id bigint not null,
    FOREIGN KEY (user_id)
        REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    text VARCHAR(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    created_time    timestamp default current_timestamp() not null,
    reply_comment_id bigint null,
    read_count    bigint not null,
    relative_value double,
    ranking double,
    pro boolean not null,
    FOREIGN KEY (reply_comment_id)
        REFERENCES comment (id) ON UPDATE NO ACTION ON DELETE CASCADE
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;






create table comment_rating
(
    comment_id  bigint not null,
    user_id    bigint not null,
    rating int not null,
    FOREIGN KEY (comment_id)
        REFERENCES comment (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (user_id)
        REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    PRIMARY KEY (comment_id, user_id)
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;


create table notification
(
    id            bigint auto_increment primary key,
    receiver_user_id    bigint not null,
    created_time timestamp default current_timestamp()   not null,
    message varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci not null,
    link varchar(256) null,
    noted boolean not null default false,
    FOREIGN KEY (receiver_user_id)
        REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE
) charset = utf8mb4 COLLATE utf8mb4_unicode_ci;


CREATE INDEX i1 ON comment (user_id);
CREATE INDEX i2 ON comment (reply_comment_id);
CREATE INDEX i3 ON comment (bill_id);
CREATE INDEX i4 ON notification (receiver_user_id);
CREATE INDEX i5 ON bill (parliament_id);
CREATE FULLTEXT INDEX billsearch ON bill (Name,abstract);