set search_path to test,public;

create type foo as (
  id int,
  name text,
  json json
);

create type bar as (
  id int,
  foos foo[]
);

create type baz as (
  id int,
  foos foo[],
  bars bar[]
);

create table t_foo (
  id serial primary key,
  name text not null,
  json json
);
create table t_bar (
  id serial primary key,
  foo_ids int[]
);
create table t_baz (
  id serial primary key,
  foo_ids int[],
  foo_jsons json[],
  bar_ids int[]
);

create or replace view v_bar as
  select
      b.id,
      row(
          b.id,
          coalesce(f.data, '{}')
      )::bar as data
  from t_bar b
  left join lateral (
      select
          array_agg(
              row(
                  f.id,
                  f.name,
                  f.json
              )::foo
          )::foo[] as data
      from t_foo f
      where f.id = any(b.foo_ids)
  ) f on true;

create or replace view v_baz as
  select
      b.id,
      row(
          b.id,
          coalesce(f.data, '{}'),
          coalesce(f.json, '{}'),
          coalesce(r.data, '{}')
      )::baz as data
  from t_baz b
  left join lateral (
      select
          f.json,
          array_agg(
              row(
                  f.id,
                  f.name,
                  f.json
              )::foo
          )::foo[] as data
      from t_foo f
      where f.id = any(b.foo_ids)
  ) f on true
  left join lateral (
      select
          array_agg(
                  v.data
          )::bar[] as data
      from t_bar f
      left join v_bar v on f.id=v.id
      where f.id = any(b.bar_ids)
  ) r on true;
