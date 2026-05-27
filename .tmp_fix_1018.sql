SET NAMES utf8mb4;
USE rent_rent_out;
UPDATE category SET name = 'Pametni zvučnik' WHERE id = 1018;
SELECT id, HEX(name) AS hex_name, name FROM category WHERE id = 1018;
