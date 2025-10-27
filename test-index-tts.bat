@echo off
REM Quick test script for Index TTS
echo Testing Index TTS v2...
echo.

echo 1. Testing health endpoint...
curl -s http://localhost:5124/health
echo.
echo.

echo 2. Generating happy speech...
curl -X POST http://localhost:5124/v1/audio/speech -H "Content-Type: application/json" -d "{\"input\":\"Hello! I am very excited to demonstrate Index TTS with emotional synthesis!\",\"emotions\":{\"happy\":0.8,\"neutral\":0.2}}" -o happy.wav
echo.
echo Saved to happy.wav
echo.

echo 3. Generating sad speech...
curl -X POST http://localhost:5124/v1/audio/speech -H "Content-Type: application/json" -d "{\"input\":\"I feel a bit down today.\",\"emotions\":{\"sad\":0.8,\"neutral\":0.2}}" -o sad.wav
echo.
echo Saved to sad.wav
echo.

echo 4. Generating angry speech...
curl -X POST http://localhost:5124/v1/audio/speech -H "Content-Type: application/json" -d "{\"input\":\"This is absolutely unacceptable!\",\"emotions\":{\"angry\":0.9,\"neutral\":0.1}}" -o angry.wav
echo.
echo Saved to angry.wav
echo.

echo.
echo ========================================
echo All tests complete!
echo Generated files:
echo   - happy.wav
echo   - sad.wav
echo   - angry.wav
echo ========================================
echo.
pause

