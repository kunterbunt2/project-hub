@echo off
REM Chatterbox TTS Container Management Helper

setlocal
cd /d %~dp0

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="logs" goto logs
if "%1"=="build" goto build
if "%1"=="test" goto test
if "%1"=="status" goto status
if "%1"=="shell" goto shell
goto usage

:start
echo Starting Chatterbox TTS container...
docker-compose -f docker-compose-chatterbox.yml up -d
echo.
echo Chatterbox TTS is starting up. This may take up to 2 minutes for model loading.
echo Check status with: chatterbox-helper.bat status
echo View logs with: chatterbox-helper.bat logs
echo Test API with: chatterbox-helper.bat test
goto end

:stop
echo Stopping Chatterbox TTS container...
docker-compose -f docker-compose-chatterbox.yml down
goto end

:restart
echo Restarting Chatterbox TTS container...
docker-compose -f docker-compose-chatterbox.yml restart
goto end

:logs
echo Showing Chatterbox TTS container logs (Ctrl+C to exit)...
docker-compose -f docker-compose-chatterbox.yml logs -f
goto end

:build
echo Building Chatterbox TTS container (this may take several minutes)...
docker-compose -f docker-compose-chatterbox.yml build --no-cache
echo.
echo Build complete. Start the container with: chatterbox-helper.bat start
goto end

:test
echo Testing Chatterbox TTS API...
echo.
echo [1/3] Health check:
curl -s http://localhost:4123/health
echo.
echo.
echo [2/3] Available languages:
curl -s http://localhost:4123/languages
echo.
echo.
echo [3/3] Testing speech generation (saving to test-output.wav)...
curl -X POST http://localhost:4123/v1/audio/speech ^
  -H "Content-Type: application/json" ^
  -d "{\"input\":\"Hello, this is a test of Chatterbox TTS.\",\"temperature\":0.7,\"exaggeration\":1.0,\"cfg_weight\":3.0}" ^
  --output test-output.wav
echo.
if exist test-output.wav (
    echo ✓ Success! Audio saved to test-output.wav
    echo You can play it to verify the output.
) else (
    echo ✗ Failed to generate audio
)
goto end

:status
echo Chatterbox TTS container status:
docker-compose -f docker-compose-chatterbox.yml ps
echo.
echo Container logs (last 20 lines):
docker-compose -f docker-compose-chatterbox.yml logs --tail=20
goto end

:shell
echo Opening shell in Chatterbox TTS container...
docker exec -it chatterbox-tts /bin/bash
goto end

:usage
echo Chatterbox TTS Container Management Helper
echo.
echo Usage: chatterbox-helper.bat [command]
echo.
echo Commands:
echo   start      - Start Chatterbox TTS container
echo   stop       - Stop Chatterbox TTS container
echo   restart    - Restart Chatterbox TTS container
echo   logs       - Show container logs (follow mode)
echo   build      - Build container from scratch
echo   test       - Test API endpoints and generate sample audio
echo   status     - Show container status and recent logs
echo   shell      - Open bash shell in container
echo.
echo Examples:
echo   chatterbox-helper.bat start
echo   chatterbox-helper.bat test
echo   chatterbox-helper.bat logs
goto end

:end
endlocal

