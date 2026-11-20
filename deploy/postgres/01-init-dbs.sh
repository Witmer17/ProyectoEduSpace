#!/bin/bash
set -e

echo "=========================================="
echo " EduSpace Production Database Initializer "
echo "=========================================="

# Create all databases if not exists
databases=(
  "auth_db"
  "planificacion_db"
  "alumnos_db"
  "matricula_db"
  "asistencia_db"
  "calificaciones_db"
  "pago_db"
  "examenes_db"
  "tareas_db"
  "notificaciones_db"
  "keycloak_db"
)

for db in "${databases[@]}"; do
  echo "Checking/creating database: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE $db OWNER $POSTGRES_USER'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
    GRANT ALL PRIVILEGES ON DATABASE $db TO $POSTGRES_USER;
EOSQL
done

# Restore pre-seeded data if dumps exist
if [ -d "/docker-entrypoint-initdb.d/dumps" ]; then
  for dump_file in /docker-entrypoint-initdb.d/dumps/*.sql; do
    if [ -f "$dump_file" ]; then
      db_target=$(basename "$dump_file" .sql)
      echo "Restoring seed dump into $db_target..."
      psql -v ON_ERROR_STOP=0 --username "$POSTGRES_USER" --dbname "$db_target" < "$dump_file" || true
    fi
  done
fi

echo "=========================================="
echo " Databases initialized and seeded!        "
echo "=========================================="
