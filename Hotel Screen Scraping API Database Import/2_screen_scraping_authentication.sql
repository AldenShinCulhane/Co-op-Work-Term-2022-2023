create table screen_scraping_authentication
(
    id                          BIGSERIAL PRIMARY KEY,
    access_token                VARCHAR(1000),
    token_type                  VARCHAR(100),
    expires_in                  INT,
    username                    VARCHAR(500),
    issued                      VARCHAR(100),
    expires                     VARCHAR(100),
    CREATED                     TIMESTAMP NOT NULL default now(),
    MODIFIED                    TIMESTAMP NOT NULL default now()
);