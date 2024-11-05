-- Создание таблицы content_entity
CREATE TABLE IF NOT EXISTS public.content_entity (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    user_id UUID REFERENCES users(id),
    content VARCHAR(255)
);

-- Миграция записей из posts
INSERT INTO public.content_entity (id, created_at, user_id, content)
SELECT id, publish_date, user_id, content
FROM public.posts;

-- Удаление ненужных столбцов из posts
ALTER TABLE public.posts DROP COLUMN publish_date;
ALTER TABLE public.posts DROP COLUMN user_id;
ALTER TABLE public.posts DROP COLUMN content;

-- Сначала создайте content_entity, затем добавьте записи в posts
-- Затем добавьте ограничение внешнего ключа к posts
ALTER TABLE public.posts ADD CONSTRAINT fk_content_entity
    FOREIGN KEY (id) REFERENCES public.content_entity(id);

-- Миграция записей из messages
INSERT INTO public.content_entity (id, created_at, user_id, content)
SELECT id, timestamp, sender_id, content
FROM public.messages;

-- Удаление ненужных столбцов из messages
ALTER TABLE public.messages DROP COLUMN content;
ALTER TABLE public.messages DROP COLUMN timestamp;
ALTER TABLE public.messages DROP COLUMN sender_id;

-- Добавление ограничения внешнего ключа к messages
ALTER TABLE public.messages ADD CONSTRAINT fk_content_entity
    FOREIGN KEY (id) REFERENCES public.content_entity(id);
