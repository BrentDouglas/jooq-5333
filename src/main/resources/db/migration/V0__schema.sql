create type foo as (
  id int,
  name text,
  otherThing text
);

create type bar as (
  id int,
  foos foo[]
);

create table t_foo (
  id serial primary key,
  name text not null,
  other text
);
create table t_bar (
  id serial primary key,
  foo_ids int[]
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
                  f.other
              )::foo
          )::foo[] as data
      from t_foo f
      where f.id = any(b.foo_ids)
  ) f on true;