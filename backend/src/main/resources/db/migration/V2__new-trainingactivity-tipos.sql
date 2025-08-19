ALTER TABLE trainingactivity DROP CONSTRAINT trainingactivity_tipo_check;

ALTER TABLE trainingactivity
ADD CONSTRAINT trainingactivity_tipo_check
CHECK (tipo::text = ANY (ARRAY['RIDE'::text, 'RUNNING'::text, 'TRAILRUNNING'::text, 'FUERZA'::text, 'MOVILIDAD'::text, 'SENDERISMO'::text, 'MOUNTAINBIKERIDE'::text, 'UNKNOWN'::text]));