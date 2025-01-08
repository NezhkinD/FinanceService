INSERT INTO user (login, password) VALUES ('dnezhkin', '$2a$12$Keh5sG5lCqobxsLOsKrhWOXkL35YJ.7mQu05MfVdf.YALBej1wFMu');

INSERT INTO category (name, type, user_id, limitC) VALUES ('Зарплата', 'income', 1, 0.0);
INSERT INTO category (name, type, user_id, limitC) VALUES ('Бонус', 'income', 1, 0.0);
INSERT INTO category (name, type, user_id, limitC) VALUES ('Еда', 'cost', 1, 50000.0);
INSERT INTO category (name, type, user_id, limitC) VALUES ('Такси', 'cost', 1, 5000.0);
INSERT INTO category (name, type, user_id, limitC) VALUES ('Развлечения', 'cost', 1, 10000.0);
INSERT INTO category (name, type, user_id, limitC) VALUES ('Коммунальные услуги', 'cost', 1, 0.0);

INSERT INTO budget (category_id, sum) VALUES (1, 100000.0);
INSERT INTO budget (category_id, sum) VALUES (2, 500000.0);
INSERT INTO budget (category_id, sum) VALUES (3, 4000.0);
INSERT INTO budget (category_id, sum) VALUES (3, 2000.0);
INSERT INTO budget (category_id, sum) VALUES (3, 6000.0);
INSERT INTO budget (category_id, sum) VALUES (4, 600.0);
INSERT INTO budget (category_id, sum) VALUES (4, 800.0);
INSERT INTO budget (category_id, sum) VALUES (4, 1200.0);
INSERT INTO budget (category_id, sum) VALUES (5, 3000.0);
INSERT INTO budget (category_id, sum) VALUES (5, 2000.0);
INSERT INTO budget (category_id, sum) VALUES (5, 4300.0);
INSERT INTO budget (category_id, sum) VALUES (6, 4000.0);
INSERT INTO budget (category_id, sum) VALUES (6, 3000.0);
