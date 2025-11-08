@echo off
REM    Project structure
REM   
REM    project-root
REM    ├── platform
REM    │   └── deployment
REM    │       └── docker-compose
REM    │           └── docker-compose.yml
REM    ├── bounded-contexts
REM    │   ├── mds.historical
REM    │   │   ├── src
REM    │   │   └── compose_up.cmd
REM    │   └── ...
REM    └── ...

set COMPOSE_FILE=../../platform/deployment/docker-compose/docker-compose.yml
set SERVICE=mds-historical

for /f "tokens=*" %%i in ('docker ps -a -q -f "name=%SERVICE%"') do (
    docker rm -f %%i
)

docker compose -f "%COMPOSE_FILE%" up -d
docker compose -f "%COMPOSE_FILE%" build %SERVICE%
docker compose -f "%COMPOSE_FILE%" up -d --force-recreate %SERVICE%
pause