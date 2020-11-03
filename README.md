# Ajiranet API

Requirements
	For building and running the application you need:
		JDK 1.8
		Maven 3

Running the application locally
	There are several ways to run a Spring Boot application on your local machine. One way is to execute the main method in the com.example.AjiraNet.AjiraNetApplication class from your IDE.

	Alternatively you can run api using jar:
		java -jar /target/AjiraNet-0.0.1-SNAPSHOT.jar

	Alternatively you can use the Spring Boot Maven plugin like so:
		mvn spring-boot:run

Testing the application using curl command:

1. For Adding device
	curl -H "Content-Type: application/json" -d "{ \"type\": \"COMPUTER\", \"name\": "A1\"}"  "http://localhost:8080/ajiranet/process/CREATE/devices"

2. For defining or modifying device strength
	curl -H "Content-Type: application/json" -d "{ \"value\": 2}" "http://localhost:8080/ajiranet/process/MODIFY/devices/A1/strength"

3. For creating connections between devices
	curl -H "Content-Type: application/json" -d "{\"source\": \"A1\" \"target\": [\"A2\",\"R1\"]}" "http://localhost:8080/ajiranet/process/CREATE/connections"

4. For listing devices
	curl -H "Content-Type: application/json" -d "{}" localhost:8080/ajiranet/process/FETCH/devices

5. For getting route to pass information between devices 
	curl -H "Content-Type: application/json" -d "{}" "http://localhost:8080/ajiranet/process/FETCH/info-routes?from=A1&to=A6"
