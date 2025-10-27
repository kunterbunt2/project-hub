@echo off
REM Script to manage Ollama container for testing with GPU support

set COMPOSE_FILE=docker-compose-ollama.yml

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="pull-model" goto pull_model
if "%1"=="status" goto status
goto help

:start
echo Starting Ollama container with GPU support...
docker-compose -f %COMPOSE_FILE% up -d
echo Waiting for Ollama to be ready...
timeout /t 10 > nul
goto pull_model

:stop
echo Stopping Ollama container...
docker-compose -f %COMPOSE_FILE% down
goto end

:pull_model
echo Pulling llama3.2:3b model...
docker exec ollama-agent-ollama-1 ollama pull llama3.2:3b
echo Model ready!
goto end

:status
docker-compose -f %COMPOSE_FILE% ps
goto end

:help
echo Usage: ollama-helper.bat [command]
echo Commands:
echo   start      - Start Ollama container with GPU support and pull model
echo   stop       - Stop Ollama container
echo   pull-model - Pull the llama3.2:3b model
echo   status     - Show container status
goto end

:end
pause
