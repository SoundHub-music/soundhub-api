ALTER TABLE users
    DROP COLUMN base_url;

ALTER TABLE users
    ALTER COLUMN is_online SET NOT NULL;


ALTER TABLE users
    DROP CONSTRAINT users_role_check;

UPDATE users SET role = 'USER' WHERE role = 'ROLE_USER';
UPDATE users SET role = 'ADMIN' WHERE role = 'ROLE_ADMIN';

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (role::text = ANY (ARRAY['ADMIN'::character varying::text, 'USER'::character varying::text]));

