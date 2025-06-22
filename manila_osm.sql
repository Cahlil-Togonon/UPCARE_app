--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3
-- Dumped by pg_dump version 16.3

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
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


--
-- Name: pgrouting; Type: EXTENSION; Schema: -; Owner: -
--

-- CREATE EXTENSION IF NOT EXISTS pgrouting WITH SCHEMA public;


-- --
-- -- Name: EXTENSION pgrouting; Type: COMMENT; Schema: -; Owner: -
-- --

-- COMMENT ON EXTENSION pgrouting IS 'pgRouting Extension';


--
-- Name: aqi_filter(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE OR REPLACE FUNCTION public.aqi_filter(z integer, x integer, y integer, threshold integer DEFAULT 500)
RETURNS bytea
LANGUAGE plpgsql
STABLE PARALLEL SAFE
AS $function$
DECLARE
    result bytea;
    latest_timestamp timestamp without time zone;
BEGIN
    SELECT data_timestamp
    INTO latest_timestamp
    FROM street_aqi_historical
    ORDER BY data_timestamp DESC
    LIMIT 1;

    WITH
    bounds AS (
        SELECT ST_TileEnvelope(z, x, y) AS geom
    ),
    mvtgeom AS (
    SELECT ST_AsMVTGeom(ST_Transform(t.wkb_geometry, 3857), bounds.geom) AS geom,
        t.aqi, t.osm_id, t.data_timestamp
    FROM street_aqi_historical t, bounds
    WHERE ST_Intersects(t.wkb_geometry, ST_Transform(bounds.geom, 4326))
    AND t.aqi < threshold
    AND t.data_timestamp = latest_timestamp
    )
    SELECT ST_AsMVT(mvtgeom.*, 'default')
    INTO result
    FROM mvtgeom;

    RETURN result;
END;
$function$;


CREATE OR REPLACE FUNCTION public.aqi_timestamp(
    z integer,
    x integer,
    y integer,
    input_data_timestamp text DEFAULT ''
)
RETURNS bytea
LANGUAGE plpgsql
STABLE PARALLEL SAFE
AS $function$
DECLARE
    result bytea;
    closest_timestamp timestamp without time zone;
BEGIN
    IF input_data_timestamp = '' THEN
        input_data_timestamp := NULL;
    END IF;

    IF input_data_timestamp IS NOT NULL THEN
        BEGIN
            closest_timestamp := TO_TIMESTAMP(input_data_timestamp, 'YYYY-MM-DD HH24:MI:SS');
        EXCEPTION
            WHEN others THEN
                closest_timestamp := NULL;
        END;
    END IF;

    IF closest_timestamp IS NULL THEN
        SELECT MAX(data_timestamp)
        INTO closest_timestamp
        FROM street_aqi_historical;
    END IF;

    IF closest_timestamp IS NOT NULL THEN
        -- Find the closest data_timestamp to the input_data_timestamp
        SELECT data_timestamp
        INTO closest_timestamp
        FROM street_aqi_historical
        WHERE data_timestamp IS NOT NULL
        ORDER BY ABS(EXTRACT(EPOCH FROM (data_timestamp - closest_timestamp)))
        LIMIT 1;
    END IF;

    WITH bounds AS (
        SELECT ST_TileEnvelope(z, x, y) AS geom
    ),
    mvtgeom AS (
        SELECT
            ST_AsMVTGeom(ST_Transform(t.wkb_geometry, 3857), bounds.geom) AS geom,
            t.aqi, t.osm_id, t.data_timestamp
        FROM street_aqi_historical t
        JOIN bounds ON ST_Intersects(t.wkb_geometry, ST_Transform(bounds.geom, 4326))
        WHERE t.data_timestamp = closest_timestamp
    )
    SELECT ST_AsMVT(mvtgeom.*, 'default')
    INTO result
    FROM mvtgeom;

    RETURN result;
END;
$function$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: data_timestamps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_timestamps (
    data_timestamp timestamp without time zone NOT NULL
);


--
-- Name: osm_ids; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.osm_ids (
    osm_id bigint NOT NULL
);


--
-- Name: street_aqi; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.street_aqi (
    osm_id bigint NOT NULL,
    wkb_geometry public.geometry(MultiLineString,4326),
    aqi double precision,
    data_timestamp timestamp without time zone
);

CREATE TABLE public.street_aqi_historical (
    osm_id bigint NOT NULL,
    wkb_geometry public.geometry(MultiLineString,4326),
    aqi double precision,
    data_timestamp timestamp without time zone
);

--
-- Name: street_aqi_linestring; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.street_aqi_linestring (
    osm_id bigint,
    line_part integer[],
    line_string text,
    aqi double precision,
    data_timestamp timestamp without time zone,
    id integer NOT NULL,
    source integer,
    target integer
);


--
-- Name: street_aqi_linestring_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.street_aqi_linestring_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: street_aqi_linestring_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.street_aqi_linestring_id_seq OWNED BY public.street_aqi_linestring.id;


--
-- Name: osm_id_geometry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.osm_id_geometry (
    ogc_fid integer NOT NULL,
    wkb_geometry public.geometry(MultiLineString,4326),
    full_id character varying,
    osm_id character varying,
    surface character varying,
    oneway character varying,
    name character varying,
    maxspeed character varying,
    lanes character varying
);


--
-- Name: osm_id_geometry_ogc_fid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.osm_id_geometry_ogc_fid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: osm_id_geometry_ogc_fid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.osm_id_geometry_ogc_fid_seq OWNED BY public.osm_id_geometry.ogc_fid;


--
-- Name: test_a; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.test_a (
    id integer NOT NULL,
    full_id character varying,
    osm_id character varying,
    surface character varying,
    oneway character varying,
    name character varying,
    maxspeed character varying,
    lanes character varying,
    shape_leng double precision,
    shape_area double precision,
    adm1_en character varying,
    adm1_pcode character varying,
    adm1_ref character varying,
    adm1alt1en character varying,
    adm1alt2en character varying,
    adm0_en character varying,
    adm0_pcode character varying,
    date timestamp with time zone,
    validon timestamp with time zone,
    validto timestamp with time zone,
    geom public.geometry(MultiLineString,4326)
);


--
-- Name: test_a_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.test_a_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: test_a_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.test_a_id_seq OWNED BY public.test_a.id;

--
-- Name: street_aqi_linestring id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.street_aqi_linestring ALTER COLUMN id SET DEFAULT nextval('public.street_aqi_linestring_id_seq'::regclass);


--
-- Name: osm_id_geometry ogc_fid; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.osm_id_geometry ALTER COLUMN ogc_fid SET DEFAULT nextval('public.osm_id_geometry_ogc_fid_seq'::regclass);


--
-- Name: test_a id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_a ALTER COLUMN id SET DEFAULT nextval('public.test_a_id_seq'::regclass);


--
-- Name: data_timestamps data_timestamps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_timestamps
    ADD CONSTRAINT data_timestamps_pkey PRIMARY KEY (data_timestamp);


--
-- Name: osm_ids osm_ids_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.osm_ids
    ADD CONSTRAINT osm_ids_pkey PRIMARY KEY (osm_id);


--
-- Name: street_aqi_backup_vertices_pgr street_aqi_backup_vertices_pgr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

--
-- Name: street_aqi_linestring street_aqi_linestring_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.street_aqi_linestring
    ADD CONSTRAINT street_aqi_linestring_pkey PRIMARY KEY (id);


--
-- Name: osm_id_geometry osm_id_geometry_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.osm_id_geometry
    ADD CONSTRAINT osm_id_geometry_pk PRIMARY KEY (ogc_fid);


--
-- Name: test_a test_a_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_a
    ADD CONSTRAINT test_a_pkey PRIMARY KEY (id);

--
-- Name: osm_id_geometry_wkb_geometry_geom_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX osm_id_geometry_wkb_geometry_geom_idx ON public.osm_id_geometry USING gist (wkb_geometry);


--
-- Name: test_a_geom_geom_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX test_a_geom_geom_idx ON public.test_a USING gist (geom);


--
-- Name: street_aqi street_aqi_data_timestamp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.street_aqi
    ADD CONSTRAINT street_aqi_data_timestamp_fkey FOREIGN KEY (data_timestamp) REFERENCES public.data_timestamps(data_timestamp);


--
-- Name: street_aqi street_aqi_osm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.street_aqi
    ADD CONSTRAINT street_aqi_osm_id_fkey FOREIGN KEY (osm_id) REFERENCES public.osm_ids(osm_id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

CREATE USER grafanareader WITH PASSWORD 'monitoring';
CREATE USER tileserv WITH PASSWORD 'guest';

GRANT USAGE ON SCHEMA public TO grafanareader;
GRANT USAGE ON SCHEMA public TO tileserv;


--
-- Name: TABLE data_timestamps; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_timestamps TO tileserv;


--
-- Name: TABLE osm_ids; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.osm_ids TO tileserv;


--
-- Name: TABLE street_aqi; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.street_aqi TO tileserv;


--
-- Name: TABLE osm_id_geometry; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.osm_id_geometry TO grafanareader;


--
-- PostgreSQL database dump complete
--

