-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE IF NOT EXISTS public.users (
	id bigserial NOT NULL,
	username varchar(50) NOT NULL,
	display_name varchar(100) NOT NULL,
	avatar_url varchar(500) NULL,
	status varchar(20) DEFAULT 'OFFLINE'::character varying NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	"password" varchar(255) NULL,
	password_plain varchar(255) NULL,
	"role" varchar(100) NULL,
	email varchar(500) NULL,
	phone varchar(30) NULL,
	"position" varchar(500) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_username_key UNIQUE (username)
);


-- public."groups" definition

-- Drop table

-- DROP TABLE public."groups";

CREATE TABLE IF NOT EXISTS public."groups" (
	id bigserial NOT NULL,
	"name" varchar(100) NOT NULL,
	description text NULL,
	avatar_url varchar(500) NULL,
	created_by int8 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	group_code varchar(50) NULL,
	CONSTRAINT groups_pkey PRIMARY KEY (id),
	CONSTRAINT groups_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE
);


-- public.messages definition

-- Drop table

-- DROP TABLE public.messages;

CREATE TABLE IF NOT EXISTS public.messages (
	id bigserial NOT NULL,
	group_id int8 NOT NULL,
	sender_id int8 NOT NULL,
	"content" text NULL,
	message_type varchar(20) DEFAULT 'TEXT'::character varying NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	info_data text NULL,
	group_code varchar(50) NULL,
	CONSTRAINT messages_pkey PRIMARY KEY (id),
	CONSTRAINT messages_group_id_fkey FOREIGN KEY (group_id) REFERENCES public."groups"(id) ON DELETE CASCADE,
	CONSTRAINT messages_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id) ON DELETE CASCADE
);
CREATE INDEX idx_messages_created_at ON public.messages USING btree (created_at DESC);
CREATE INDEX idx_messages_group_id ON public.messages USING btree (group_id);
CREATE INDEX idx_messages_sender_id ON public.messages USING btree (sender_id);


-- public.group_members definition

-- Drop table

-- DROP TABLE public.group_members;

CREATE TABLE IF NOT EXISTS public.group_members (
	id bigserial NOT NULL,
	group_id int8 NOT NULL,
	user_id int8 NOT NULL,
	"role" varchar(20) DEFAULT 'MEMBER'::character varying NULL,
	joined_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT group_members_group_id_user_id_key UNIQUE (group_id, user_id),
	CONSTRAINT group_members_pkey PRIMARY KEY (id),
	CONSTRAINT group_members_group_id_fkey FOREIGN KEY (group_id) REFERENCES public."groups"(id) ON DELETE CASCADE,
	CONSTRAINT group_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);
CREATE INDEX idx_group_members_group_id ON public.group_members USING btree (group_id);
CREATE INDEX idx_group_members_user_id ON public.group_members USING btree (user_id);


-- public.message_attachments definition

-- Drop table

-- DROP TABLE public.message_attachments;

CREATE TABLE IF NOT EXISTS public.message_attachments (
	id bigserial NOT NULL,
	message_id int8 NOT NULL,
	file_name varchar(255) NOT NULL,
	file_type varchar(50) NOT NULL,
	file_size int8 NOT NULL,
	file_url varchar(500) NOT NULL,
	storage_path varchar(500) NOT NULL,
	thumbnail_url varchar(500) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	width int4 NULL,
	height int4 NULL,
	duration int4 NULL,
	CONSTRAINT message_attachments_pkey PRIMARY KEY (id),
	CONSTRAINT message_attachments_message_id_fkey FOREIGN KEY (message_id) REFERENCES public.messages(id) ON DELETE CASCADE
);
CREATE INDEX idx_message_attachments_message_id ON public.message_attachments USING btree (message_id);
-- Sample data
delete from public.users;
INSERT INTO public.users (username,display_name,avatar_url,status,created_at,updated_at,"password",password_plain,"role",email,phone,"position") VALUES
	 ('user2','Khoai Tây',NULL,'ONLINE','2025-11-22 16:51:18.125388','2025-11-23 12:10:16.671016','$2a$10$8kjYGubcIRLzu7qhqqySIuV6tf/2GOMVWKFd96c1zpFU3uEo0uqlC','admin123!','MEMBER',NULL,NULL,NULL),
	 ('admin2','Cá Heo',NULL,'OFFLINE','2025-11-22 20:01:52.599212','2025-11-22 20:41:53.247995','$2a$10$f/hf4xw2aW1qUnsoS.y33eUWPgiXOf2lap/MFmDwn9MFGbIw1lXpC','admin123!','ADMIN',NULL,NULL,NULL),
	 ('admin1','Gà Đồi',NULL,'OFFLINE','2025-11-22 19:59:25.485549','2025-11-22 20:42:45.881155','$2a$10$wBt.7oqbL3pWsQwxSXHjmOwyFNO9axTnxLmrxZxCM.lHxkU3bvIvS','admin123!','ADMIN',NULL,NULL,NULL),
	 ('admin','Administrator',NULL,'ONLINE','2025-11-22 16:51:18.125388','2025-11-23 12:07:27.943492','$2a$10$GwkhplhLbZ7y5ztvNSoKTepCkZs9RopGzaLG/2Vu50JZoQIsYted.','admin123!','ADMIN',NULL,NULL,NULL),
	 ('user1','Khoai Lang',NULL,'ONLINE','2025-11-22 16:51:18.125388','2025-11-23 12:08:16.554265','$2a$10$MRgX0Ud3VLEZL9x3PGmNWuosbUd9NGHzQ/O3PC0.i1kIUwZ.JyukO','admin123!','MEMBER',NULL,NULL,NULL)

ON CONFLICT (username) DO NOTHING;

delete from public."groups";
INSERT INTO public."groups" ("name",description,avatar_url,created_by,created_at,updated_at,group_code) VALUES
	 ('General','General',NULL,1,'2025-11-22 17:18:31.904829','2025-11-22 17:18:31.904829','G-0001'),
	 ('LoanGiang','',NULL,4,'2025-11-22 20:01:26.437805','2025-11-22 20:01:26.437805','G-0002'),
	 ('test','test',NULL,1,'2025-11-22 20:33:23.05381','2025-11-22 20:33:23.05381','G-0003'),
	 ('Test2','Test2',NULL,1,'2025-11-23 10:07:03.682198','2025-11-23 10:07:03.682198','EHX427');
ON CONFLICT (group_code) DO NOTHING;

delete from public.group_members;
INSERT INTO public.group_members (group_id,user_id,"role",joined_at) VALUES
	 (2,4,'ADMIN','2025-11-22 20:01:26.443807'),
	 (2,5,'MEMBER','2025-11-22 20:02:29.930869'),
	 (3,1,'ADMIN','2025-11-22 20:33:23.068472'),
	 (3,4,'MEMBER','2025-11-22 20:33:29.760151'),
	 (2,2,'MEMBER','2025-11-22 20:46:04.754404'),
	 (2,3,'MEMBER','2025-11-22 20:46:10.321421'),
	 (1,5,'MEMBER','2025-11-22 20:46:16.127321'),
	 (4,1,'ADMIN','2025-11-23 10:07:03.687218'),
	 (4,3,'MEMBER','2025-11-23 10:10:40.707133'),
	 (4,2,'MEMBER','2025-11-23 10:10:44.861629');
