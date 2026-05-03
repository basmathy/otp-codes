create table if not exists app_users (
    id bigserial primary key,
    login varchar(100) not null unique,
    password_hash varchar(255) not null,
    role varchar(20) not null,
    created_at timestamp not null default now()
);

create table if not exists otp_settings (
    id smallint primary key,
    code_length integer not null,
    lifetime_seconds integer not null,
    constraint only_one_otp_settings check (id = 1),
    constraint positive_code_length check (code_length > 0),
    constraint positive_lifetime check (lifetime_seconds > 0)
);

insert into otp_settings (id, code_length, lifetime_seconds)
values (1, 6, 300)
on conflict (id) do nothing;

create table if not exists otp_codes (
    id bigserial primary key,
    user_id bigint not null references app_users(id) on delete cascade,
    operation_id varchar(120) not null,
    code varchar(20) not null,
    status varchar(20) not null,
    created_at timestamp not null default now(),
    expires_at timestamp not null,
    used_at timestamp null
);

create index if not exists idx_otp_codes_user_operation_status
    on otp_codes (user_id, operation_id, status);

create index if not exists idx_otp_codes_status_expires_at
    on otp_codes (status, expires_at);
