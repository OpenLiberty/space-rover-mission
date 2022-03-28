cd server
call mvnw install -DskipTests=true -B
cd ..\leaderboard
call mvnw install -B
cd ..
call docker-compose down
call docker-compose up --build