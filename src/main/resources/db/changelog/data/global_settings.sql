-- liquibase formatted sql
-- changeset Death:init-data

insert into global_settings(code, name, value) values ('MULTIUSER_MODE', 'Многопользовательский режим', 'NO');
insert into global_settings(code, name, value) values ('POST_PREMODERATION', 'Премодерация постов', 'NO');
insert into global_settings(code, name, value) values ('STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', 'YES');