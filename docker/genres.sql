--
-- PostgreSQL database dump
--

-- Dumped from database version 15.8
-- Dumped by pg_dump version 15.8

-- Started on 2024-12-13 10:55:10 MSK

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 16859)
-- Name: genres; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE IF NOT EXISTS public.genres (
                               id uuid NOT NULL,
                               name character varying(255),
                               picture_url character varying(255)
);


ALTER TABLE public.genres OWNER TO postgres;

--
-- TOC entry 4461 (class 0 OID 16859)
-- Dependencies: 217
-- Data for Name: genres; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.genres (id, name, picture_url) FROM stdin;
0b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Electronic	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
1b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Indie	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
2b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Punk	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
3b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Pop	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
4b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Rap & Hip-hop	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
5b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Russian Rap	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
6b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Country	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
7b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Alternative	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
8b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Hardcore	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
9b5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Metal	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
ab5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Rock	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
bb5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Russian Rock	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
cb5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Dance	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
db5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Jazz	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
eb5fb493-b45b-4b5b-b5b2-6b8c4b5b4b5b	Blues	http://192.168.1.46:8080/api/v1/files/electronic.png?folderName=genres
\.


--
-- TOC entry 4318 (class 2606 OID 16907)
-- Name: genres genres_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.genres
    ADD CONSTRAINT genres_pkey PRIMARY KEY (id);


-- Completed on 2024-12-13 10:55:10 MSK

--
-- PostgreSQL database dump complete
--
