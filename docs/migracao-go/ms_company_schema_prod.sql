--
-- PostgreSQL database dump
--

-- Dumped from database version 15.5 (Debian 15.5-1.pgdg120+1)
-- Dumped by pg_dump version 15.5 (Debian 15.5-1.pgdg120+1)

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

--
-- Name: ms_company; Type: SCHEMA; Schema: -; Owner: keepguard_api_user
--

CREATE SCHEMA ms_company;


ALTER SCHEMA ms_company OWNER TO keepguard_api_user;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: companies; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.companies (
    id uuid NOT NULL,
    cnpj character varying(14) NOT NULL,
    code_company uuid NOT NULL,
    created_at timestamp(6) without time zone,
    ein character varying(20),
    legal_name character varying(200) NOT NULL,
    municipal_registration character varying(20),
    name character varying(150) NOT NULL,
    state_registration character varying(20),
    status character varying(255) NOT NULL,
    tax_regime character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    tenant_id uuid NOT NULL,
    CONSTRAINT companies_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('INACTIVE'::character varying)::text, ('PENDING_APPROVAL'::character varying)::text, ('SUSPENDED'::character varying)::text, ('BLOCKED'::character varying)::text]))),
    CONSTRAINT companies_tax_regime_check CHECK (((tax_regime)::text = ANY (ARRAY[('SIMPLES_NACIONAL'::character varying)::text, ('LUCRO_PRESUMIDO'::character varying)::text, ('LUCRO_REAL'::character varying)::text])))
);


ALTER TABLE ms_company.companies OWNER TO keepguard_api_user;

--
-- Name: company_addresses; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_addresses (
    id uuid NOT NULL,
    active boolean NOT NULL,
    city character varying(100) NOT NULL,
    complement character varying(100),
    country character varying(100) NOT NULL,
    created_at timestamp(6) without time zone,
    district character varying(100) NOT NULL,
    number character varying(20) NOT NULL,
    state character varying(2) NOT NULL,
    street character varying(150) NOT NULL,
    updated_at timestamp(6) without time zone,
    zip_code character varying(8) NOT NULL,
    company_id uuid NOT NULL
);


ALTER TABLE ms_company.company_addresses OWNER TO keepguard_api_user;

--
-- Name: company_bank_accounts; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_bank_accounts (
    id uuid NOT NULL,
    bank_account_digit character varying(1) NOT NULL,
    bank_account_number character varying(20) NOT NULL,
    bank_account_type character varying(255) NOT NULL,
    active boolean NOT NULL,
    bank_agency character varying(10) NOT NULL,
    bank_agency_digit character varying(1),
    bank_code character varying(3) NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    company_id uuid NOT NULL,
    CONSTRAINT company_bank_accounts_bank_account_type_check CHECK (((bank_account_type)::text = ANY (ARRAY[('CORRENTE'::character varying)::text, ('POUPANCA'::character varying)::text, ('PJ'::character varying)::text])))
);


ALTER TABLE ms_company.company_bank_accounts OWNER TO keepguard_api_user;

--
-- Name: company_cnaes; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_cnaes (
    id uuid NOT NULL,
    active boolean NOT NULL,
    class_code character varying(4),
    code character varying(7) NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(500) NOT NULL,
    division character varying(2),
    group_code character varying(3),
    principal boolean NOT NULL,
    section character varying(1),
    subclass_code character varying(5),
    updated_at timestamp(6) without time zone,
    company_id uuid NOT NULL
);


ALTER TABLE ms_company.company_cnaes OWNER TO keepguard_api_user;

--
-- Name: company_contacts; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_contacts (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone,
    department character varying(100),
    email character varying(150) NOT NULL,
    name character varying(100) NOT NULL,
    phone character varying(20) NOT NULL,
    "position" character varying(100),
    updated_at timestamp(6) without time zone,
    website character varying(150),
    company_id uuid NOT NULL
);


ALTER TABLE ms_company.company_contacts OWNER TO keepguard_api_user;

--
-- Name: company_mfa_channels; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_mfa_channels (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id uuid NOT NULL,
    channel character varying(30) NOT NULL,
    is_required boolean DEFAULT true NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ms_company.company_mfa_channels OWNER TO keepguard_api_user;

--
-- Name: company_policies; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_policies (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    company_id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    created_by character varying(64),
    description character varying(255) NOT NULL,
    effective_from timestamp(6) without time zone NOT NULL,
    effective_to timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    updated_by character varying(64),
    version integer NOT NULL,
    CONSTRAINT company_policies_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('INACTIVE'::character varying)::text])))
);


ALTER TABLE ms_company.company_policies OWNER TO keepguard_api_user;

--
-- Name: company_representatives; Type: TABLE; Schema: ms_company; Owner: keepguard_api_user
--

CREATE TABLE ms_company.company_representatives (
    id uuid NOT NULL,
    active boolean NOT NULL,
    birth_date date NOT NULL,
    cpf character varying(11) NOT NULL,
    created_at timestamp(6) without time zone,
    email character varying(150) NOT NULL,
    name character varying(150) NOT NULL,
    phone character varying(20) NOT NULL,
    rg character varying(15),
    role character varying(100),
    updated_at timestamp(6) without time zone,
    company_id uuid NOT NULL
);


ALTER TABLE ms_company.company_representatives OWNER TO keepguard_api_user;

--
-- Name: companies companies_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);


--
-- Name: company_addresses company_addresses_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_addresses
    ADD CONSTRAINT company_addresses_pkey PRIMARY KEY (id);


--
-- Name: company_bank_accounts company_bank_accounts_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_bank_accounts
    ADD CONSTRAINT company_bank_accounts_pkey PRIMARY KEY (id);


--
-- Name: company_cnaes company_cnaes_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_cnaes
    ADD CONSTRAINT company_cnaes_pkey PRIMARY KEY (id);


--
-- Name: company_contacts company_contacts_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_contacts
    ADD CONSTRAINT company_contacts_pkey PRIMARY KEY (id);


--
-- Name: company_mfa_channels company_mfa_channels_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_mfa_channels
    ADD CONSTRAINT company_mfa_channels_pkey PRIMARY KEY (id);


--
-- Name: company_policies company_policies_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_policies
    ADD CONSTRAINT company_policies_pkey PRIMARY KEY (id);


--
-- Name: company_representatives company_representatives_pkey; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_representatives
    ADD CONSTRAINT company_representatives_pkey PRIMARY KEY (id);


--
-- Name: companies uk1phmrrhw2946lhdeh2yjis697; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.companies
    ADD CONSTRAINT uk1phmrrhw2946lhdeh2yjis697 UNIQUE (cnpj);


--
-- Name: company_mfa_channels uk_company_channel; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_mfa_channels
    ADD CONSTRAINT uk_company_channel UNIQUE (company_id, channel);


--
-- Name: companies ukepfsjvvy2l0w26bu631a00u2v; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.companies
    ADD CONSTRAINT ukepfsjvvy2l0w26bu631a00u2v UNIQUE (code_company);


--
-- Name: company_cnaes ukjhyu0nflr0jusrtggllcufa45; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_cnaes
    ADD CONSTRAINT ukjhyu0nflr0jusrtggllcufa45 UNIQUE (company_id, code);


--
-- Name: companies ukphlxrfhjaovjgc7nx36c75ux5; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.companies
    ADD CONSTRAINT ukphlxrfhjaovjgc7nx36c75ux5 UNIQUE (tenant_id);


--
-- Name: company_contacts uktpgbpe0yntxqq832qdcha35e8; Type: CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_contacts
    ADD CONSTRAINT uktpgbpe0yntxqq832qdcha35e8 UNIQUE (email);


--
-- Name: company_mfa_channels company_mfa_channels_company_id_fkey; Type: FK CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_mfa_channels
    ADD CONSTRAINT company_mfa_channels_company_id_fkey FOREIGN KEY (company_id) REFERENCES ms_company.companies(id) ON DELETE CASCADE;


--
-- Name: company_cnaes fk2njvp1hvv6e722jr0ubh11cxw; Type: FK CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_cnaes
    ADD CONSTRAINT fk2njvp1hvv6e722jr0ubh11cxw FOREIGN KEY (company_id) REFERENCES ms_company.companies(id);


--
-- Name: company_contacts fk5a8peljwaldko3px1k3dxj5v4; Type: FK CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_contacts
    ADD CONSTRAINT fk5a8peljwaldko3px1k3dxj5v4 FOREIGN KEY (company_id) REFERENCES ms_company.companies(id);


--
-- Name: company_addresses fk710v23c1e02xk6mheanqc6vc0; Type: FK CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_addresses
    ADD CONSTRAINT fk710v23c1e02xk6mheanqc6vc0 FOREIGN KEY (company_id) REFERENCES ms_company.companies(id);


--
-- Name: company_representatives fka5jme709hu6c1d5puct5kpocl; Type: FK CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_representatives
    ADD CONSTRAINT fka5jme709hu6c1d5puct5kpocl FOREIGN KEY (company_id) REFERENCES ms_company.companies(id);


--
-- Name: company_bank_accounts fks7kwxjp4p33kv6261uu34kigc; Type: FK CONSTRAINT; Schema: ms_company; Owner: keepguard_api_user
--

ALTER TABLE ONLY ms_company.company_bank_accounts
    ADD CONSTRAINT fks7kwxjp4p33kv6261uu34kigc FOREIGN KEY (company_id) REFERENCES ms_company.companies(id);


--
-- PostgreSQL database dump complete
--

