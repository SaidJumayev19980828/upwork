--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'organization-1');
INSERT INTO public.organizations(id, name,  p_name) VALUES (99002, 'organization_2', 'organization-2');


--inserting organizations domains
INSERT INTO public.organization_domains (id, "domain", subdir, organization_id) VALUES(55001, 'nasnav.com', 'organization_1', 99001);
INSERT INTO public.organization_domains (id, "domain", subdir, organization_id) VALUES(55002, 'nasnav.com', 'organization_2', 99002);



--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password)
    VALUES (88001, 'user1@nasnav.com','user1','123', 99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password)
    VALUES (88002, 'user1@nasnav.com','user1','456', 99002, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');
INSERT INTO public.users(id, email,  user_name, authentication_token, address, country, city, phone_number, image, organization_id, encrypted_password)
    VALUES (88003, 'user2@nasnav.com','user2','789', '21 jump street', 'Egypt', 'Cairo', '+021092154875','/urls/images/fdsafag23.jpg',  99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

   
--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (159, 'Walid', 'user2@nasnav.com', 99001, 'nopqrst',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

