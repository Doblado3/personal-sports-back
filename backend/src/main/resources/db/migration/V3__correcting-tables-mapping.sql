DO $$
BEGIN
  
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'metricasalud_id_usuario_fkey'
  ) THEN
    
    ALTER TABLE public.metricasalud
    ADD CONSTRAINT metricasalud_id_usuario_fkey
    FOREIGN KEY (id_usuario) REFERENCES public.users(id);
  END IF;
END
$$;

ALTER TABLE public.metricasalud
RENAME CONSTRAINT ukf73x8d22c7ifo0ygamgp0r1kb TO uk_metricasalud_id_usuario_fecha_registro;

ALTER TABLE public.trainingactivity
RENAME CONSTRAINT fksvu6sm9knoink132uuam8pj08 TO fk_trainingactivity_id_usuario;