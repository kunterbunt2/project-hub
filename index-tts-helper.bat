@echo off
REM Index TTS Container Management Helper

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="logs" goto logs
if "%1"=="build" goto build
if "%1"=="test" goto test
if "%1"=="status" goto status
goto usage

:start
echo Starting Index TTS container...
docker-compose -f docker-compose-index-tts.yml up -d
echo.
echo Index TTS is starting up. This may take up to 90 seconds for model loading.
echo Check status with: index-tts-helper.bat status
echo View logs with: index-tts-helper.bat logs
goto end

:stop
echo Stopping Index TTS container...
docker-compose -f docker-compose-index-tts.yml down
goto end

:restart
echo Restarting Index TTS container...
docker-compose -f docker-compose-index-tts.yml restart
goto end

:logs
echo Showing Index TTS container logs (Ctrl+C to exit)...
docker-compose -f docker-compose-index-tts.yml logs -f
goto end

:build
echo Building Index TTS container (this may take several minutes)...
docker-compose -f docker-compose-index-tts.yml build --no-cache
echo.
echo Build complete. Start the container with: index-tts-helper.bat start
goto end

:test
echo Testing Index TTS connection...
echo.
echo Health check:
curl -s http://localhost:5124/health
echo.
echo.
echo Available voices:
curl -s http://localhost:5124/voices
echo.
echo.
echo Available emotions:
curl -s http://localhost:5124/emotions
echo.
goto end

:status
echo Checking Index TTS container status...
docker-compose -f docker-compose-index-tts.yml ps
goto end

:usage
echo.
echo Index TTS Container Management Helper
echo =====================================
echo.
echo Usage: index-tts-helper.bat [command]
echo.
echo Commands:
echo   start   - Start the Index TTS container
echo   stop    - Stop the Index TTS container
echo   restart - Restart the Index TTS container
echo   logs    - Show container logs (live)
echo   build   - Build/rebuild the container from scratch
echo   test    - Test the connection and show available options
echo   status  - Show container status
echo.
echo Examples:
echo   index-tts-helper.bat start
echo   index-tts-helper.bat logs
echo   index-tts-helper.bat test
echo.
goto end

:end

