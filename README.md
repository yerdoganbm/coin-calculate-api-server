
mvn clean compile install

#Dockerfile : 
docker build --build-arg JAR_FILE=coin-calculate-api-server-app/target/*.jar -t springbootapp .

docker run -p 8088:8080 springbootapp  

#Docker-Compose:

docker-compose -f docker-compose.yml up
