-- !Ups

CREATE TABLE "user"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"       VARCHAR   NOT NULL,
    "password"   VARCHAR   NOT NULL,
    "email"      VARCHAR   NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

CREATE TABLE "category"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"       VARCHAR   NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

CREATE TABLE "product"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"       VARCHAR   NOT NULL,
    "description" VARCHAR   NOT NULL,
    "category_id" INTEGER NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    FOREIGN KEY(category_id) REFERENCES category(id)
);

CREATE TABLE "promotion"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product_id" INTEGER NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    FOREIGN KEY(product_id) REFERENCES product(id)
);

CREATE TABLE "discount"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product_id" INTEGER NOT NULL,
    "user_id"    INTEGER NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    FOREIGN KEY(product_id) REFERENCES product(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE "order"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_id"   INTEGER NOT NULL,
    "amount"    REAL NOT NULL,
    "date"      VARCHAR NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE "order_element"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product_id" INTEGER NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    FOREIGN KEY(product_id) REFERENCES product(id)
);

CREATE TABLE "address"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "street"    VARCHAR ,
    "zipcode"    VARCHAR ,
    "number"    VARCHAR ,
    "city"    VARCHAR ,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

CREATE TABLE "credit_card"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "holder_name" VARCHAR ,
    "number"        INTEGER ,
    "cvv"       INTEGER ,
    "date"  VARCHAR ,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

CREATE TABLE "payment"
(
    "id"         INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_id" INTEGER,
    "credit_card_id" INTEGER ,
    "date" VARCHAR ,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    FOREIGN KEY(user_id) REFERENCES user(id),
    FOREIGN KEY(credit_card_id) REFERENCES credit_card(id)
);

CREATE TABLE "user"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerId"  VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL,
    "email"       VARCHAR NOT NULL
);

CREATE TABLE "authToken"
(
    "id"     INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "userId" INT     NOT NULL,
    FOREIGN KEY (userId) references user (id)
);

CREATE TABLE "passwordInfo"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerId"  VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL,
    "hasher"      VARCHAR NOT NULL,
    "password"    VARCHAR NOT NULL,
    "salt"        VARCHAR
);

CREATE TABLE "oAuth2Info"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerId"  VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL,
    "accessToken" VARCHAR NOT NULL,
    "tokenType"   VARCHAR,
    "expiresIn"   INTEGER
);


# --- !Downs

DROP TABLE "user";
DROP TABLE "authToken";
DROP TABLE "passwordInfo";
DROP TABLE "oAuth2Info";

-- !Downs

DROP TABLE "user"
DROP TABLE "product"
DROP TABLE "category"
DROP TABLE "promotion"
DROP TABLE "discount"
DROP TABLE "order_element"
DROP TABLE "order"
DROP TABLE "address"
DROP TABLE "credit_card"
DROP TABLE "payment"