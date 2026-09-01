-- MFA da company: somente e-mail habilitado.
-- KeepGuard Secondary (company_id = f7fc7350-b9fc-4e54-9c58-ac9385b23ae4)

BEGIN;

DELETE FROM ms_company.company_mfa_channels
WHERE company_id = 'f7fc7350-b9fc-4e54-9c58-ac9385b23ae4';

INSERT INTO ms_company.company_mfa_channels (
    id, company_id, channel, is_required, is_enabled, created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'f7fc7350-b9fc-4e54-9c58-ac9385b23ae4',
    'EMAIL',
    true,
    true,
    NOW(),
    NOW()
);

COMMIT;
