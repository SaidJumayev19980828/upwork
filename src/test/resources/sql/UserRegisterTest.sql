--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());



--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id, encrypted_password)
    VALUES (88001, 'user1@nasnav.com',now(), now(), 'user1','123', 99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id, encrypted_password)
    VALUES (88002, 'user1@nasnav.com',now(), now(), 'user1','456', 99002, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, address, country, city, phone_number, image, organization_id, encrypted_password)
    VALUES (88003, 'user2@nasnav.com',now(), now(), 'user2','789', '21 jump street', 'Egypt', 'Cairo', '+021092154875','/urls/images/fdsafag23.jpg',  99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

   
--inserting Employee Users
INSERT INTO public.employee_users(id, name, created_at, updated_at, email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (159, 'Walid', now(), now(), 'user2@nasnav.com', 99001, 'nopqrst',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

