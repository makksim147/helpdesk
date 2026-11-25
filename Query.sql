

INSERT INTO category (name) VALUES
                                ('Техническая проблема'),
                                ('Проблема с аккаунтом'),
                                ('Ошибка программы'),
                                ('Консультация'),
                                ('Другое');

SELECT * FROM request;

SELECT * FROM users;
SELECT * FROM request;

UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@gmail.com';

