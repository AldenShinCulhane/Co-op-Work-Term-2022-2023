create table screen_scraping_hotels
(
    id                          BIGSERIAL PRIMARY KEY,
    hotelcode                   INT NOT NULL,
    hotelname                   VARCHAR (1500),
    address                     VARCHAR (1500),
    city                        VARCHAR (500),
    state                       VARCHAR (100),
    zip                         VARCHAR (100),
    country                     VARCHAR (500),
    starrating                  VARCHAR (100),
    lat                         DECIMAL,
    lng                         DECIMAL,
    CREATED                     TIMESTAMP NOT NULL default now(),
    MODIFIED                    TIMESTAMP NOT NULL default now()
);