--liquibase formatted sql

--changeset Mark:add services dbms:postgresql splitStatements:false failOnError:true

--comment: add services

--inserting service
INSERT INTO public.service(id,code,name,description) values (1,'CHAT_SERVICES','CHAT_SERVICES','CHAT_SERVICES Service');
INSERT INTO public.service(id,code,name,description) values (2,'360_TOURS','360_TOURS','360_TOURS Service');
INSERT INTO public.service(id,code,name,description) values (3,'CUSTOMER_EVENTS','CUSTOMER_EVENTS','CUSTOMER_EVENTS Service');
INSERT INTO public.service(id,code,name,description) values (4,'VIDEO_CALL','VIDEO_CALL','VIDEO_CALL Service');
INSERT INTO public.service(id,code,name,description) values (5,'UP_TO_10_PERSONS','UP_TO_10_PERSONS','UP_TO_10_PERSONS Service');
INSERT INTO public.service(id,code,name,description) values (6,'STANDARD_ECOMMERCE','STANDARD_ECOMMERCE','STANDARD_ECOMMERCE Service');
INSERT INTO public.service(id,code,name,description) values (7,'BOGO_FEATURES','BOGO_FEATURES','BOGO_FEATURES Service');
INSERT INTO public.service(id,code,name,description) values (8,'UP_TO_20_PERSONS','UP_TO_20_PERSONS','UP_TO_20_PERSONS Service');
INSERT INTO public.service(id,code,name,description) values (9,'INFLUENCER_PROGRAM','INFLUENCER_PROGRAM','INFLUENCER_PROGRAM Service');
INSERT INTO public.service(id,code,name,description) values (10,'STAKING_REWARDS','STAKING_REWARDS','STAKING_REWARDS Service');
INSERT INTO public.service(id,code,name,description) values (11,'LOYALTY_PROGRAM','LOYALTY_PROGRAM','LOYALTY_PROGRAM Service');
INSERT INTO public.service(id,code,name,description) values (12,'CUSTOM_SERVICE','CUSTOM_SERVICE','CUSTOM_SERVICE Service');