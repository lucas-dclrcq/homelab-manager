# Homelab Manager

### Description

**Homelab Manager** is a modular application designed to handle notifications and workflows for home media setups. It
integrates with popular services like Sonarr, Radarr, and Lidarr to process notifications via Kafka and deliver them to
various channels, such as Matrix chat rooms. This tool is ideal for those who want to automate media management
workflows in a homelab environment.

This app is quite specific to my needs, and also because I use it to experiment and keep current on the latest tech, it is quite overkill :D

### Features

- **Matrix Chat Integration**: Easily forwards notifications to Matrix chat rooms for easy monitoring.

### Prerequisites

Make sure you have the following installed before running the application:

- Java Development Kit (JDK) **21**
- **Apache Kafka** for notification streaming
- A **Matrix Server** instance and API access token
- Build Tool: **Maven**

### Installation

1. **Clone the Repository**:

``` bash
   git clone https://github.com/your-username/homelab-manager.git  
   cd homelab-manager  
```

1. **Build the Application**:
   Ensure Maven is installed and run the following command:

``` bash
   mvn clean install  
```

1. **Configure Environment Variables**:
   Set the necessary environment variables for Kafka, Matrix, and other services. See
   the [Configuration](#configuration) section for details.
2. **Run the Application**:
   Use the following command to start the application:

``` bash
   java -jar target/homelab-manager.jar  
```

### Configuration

This application is configured entirely through **environment variables**, removing the need for property files in
production. Quarkus automatically maps environment variables to application properties by replacing `.` with `_` and
converting them to uppercase.

#### Required Environment Variables

| Environment Variable      | Description                                        | Example                   |
|---------------------------|----------------------------------------------------|---------------------------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers for the application to use           | `localhost:9092`          |
| `MATRIX_ROOM_SONARR`      | Matrix room for Sonarr notifications               | `!sonarr:test-server.tld` |
| `MATRIX_ROOM_RADARR`      | Matrix room for Radarr notifications               | `!radarr:test-server.tld` |
| `MATRIX_ROOM_LIDARR`      | Matrix room for Lidarr notifications               | `!lidarr:test-server.tld` |
| `MATRIX_API_TOKEN`        | API access token for Matrix notifications          | `your-matrix-token`       |

#### Optional Environment Variables

| Environment Variable | Description                      | Default Value |
|----------------------|----------------------------------|---------------|
| `LOG_LEVEL`          | Logging level of the application | `INFO`        |

### Usage

#### Running the Application Locally

1. Export necessary environment variables:

``` bash
   export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"  
   export MATRIX_ROOM_SONARR="!sonarr:test-server.tld"  
   export MATRIX_ROOM_RADARR="!radarr:test-server.tld"  
   export MATRIX_ROOM_LIDARR="!lidarr:test-server.tld"  
   export MATRIX_API_TOKEN="your-matrix-token"  
```

1. Run the jar file:

``` bash
   java -jar target/homelab-manager.jar  
```

#### Running with Docker

You can package the application into a Docker container and run it easily.

1. **Build the Docker Image**:

``` bash
   docker build -t homelab-manager .  
```

1. **Run the Container**:
   Pass the environment variables to the container like this:

``` bash
   docker run -e KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \  
   -e MATRIX_ROOM_SONARR="sonarr-topic" \  
   -e MATRIX_ROOM_RADARR="radarr-topic" \  
   -e MATRIX_ROOM_LIDARR="lidarr-topic" \  
   -e MATRIX_API_TOKEN="your-matrix-token" \  
   homelab-manager  
```

### License

This project is licensed under the [MIT License](LICENSE).
