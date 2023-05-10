----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting yeshtery users
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (808, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (809, 'test2@nasnav.com','user2','456', 99001);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id ,yeshtery_user_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001,808);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id,yeshtery_user_id)
VALUES (89, 'test2@nasnav.com','user2','456', 99001,809);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '456', now(), null, 89);


--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--insert loyalty point transaction entity
INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(1,88,5,99001,now() - INTERVAL '2 DAY',2,10);
INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(2,88,10,99001,now() - INTERVAL '2 DAY',3,20);
-- INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(3,88,20,99001,now() - INTERVAL '2 DAY',2,30);
-- INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(4,88,30,99001,now() - INTERVAL '2 DAY',2,40);

--insert loyalty spent transaction entity
INSERT INTO public.loyalty_spent_transactions(id,transaction_id,reverse_transaction_id)values(1,1,2);
-- INSERT INTO public.loyalty_spent_transactions(id,transaction_id,reverse_transaction_id)values(2,3,4);
