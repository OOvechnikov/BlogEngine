-- liquibase formatted sql
-- changeset Death:init-data

insert into global_settings(id, code, name, value) values (1, 'MULTIUSER_MODE', 'Многопользовательский режим', 'YES');
insert into global_settings(id, code, name, value) values (2, 'POST_PREMODERATION', 'Премодерация постов', 'YES');
insert into global_settings(id, code, name, value) values (3, 'STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', 'YES');