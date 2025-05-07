# Development with DevContainers

## Overview

This guide explains how to set up and use DevContainers for local development of the Performance Automation Framework. DevContainers provide a consistent, isolated environment for development that includes all necessary dependencies and tools pre-configured, ensuring compatibility with Java 21 and all required script engines.

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed on your system
- [Visual Studio Code](https://code.visualstudio.com/) installed
- [Remote Development extension pack](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.vscode-remote-extensionpack) installed in VS Code

## DevContainer Configuration

The Performance Automation Framework includes a pre-configured DevContainer setup in the `.devcontainer` directory, which provides:

- Java 21 (OpenJDK) with required script engines
- Maven 3.9.6 
- JMeter 5.6 with DSL integration
- Script engines (Groovy 4.0.15 and Nashorn 15.4)
- All required dependencies and tools for development
- Pre-configured VS Code extensions for Java, Maven, and testing

## Getting Started with DevContainers

### Step 1: Clone the Repository

```bash
git clone https://github.com/TestAutomationMultiverse/PerformanceAutomationFramework.git
cd PerformanceAutomationFramework
```

### Step 2: Open in VS Code

Open the project folder in Visual Studio Code:

```bash
code .
```

### Step 3: Reopen in Container

When you open the project for the first time, VS Code should detect the DevContainer configuration and prompt you to reopen the project in a container. If not, you can:

1. Press `F1` to open the command palette
2. Type "Remote-Containers: Reopen in Container" and select it

VS Code will build the container (this may take a few minutes the first time) and then reopen the project inside the container environment.

## Using the DevContainer Environment

Once inside the DevContainer, you'll have access to:

- A terminal with all necessary command-line tools
- Pre-configured Java and Maven settings
- Extensions for Java development pre-installed
- Consistent environment matching CI/CD pipelines

### Running Tests

You can run tests directly from the integrated terminal:

```bash
mvn clean test
```

### Building the Project

To build the entire project:

```bash
mvn clean package
```

### Debugging Tests

1. Set breakpoints in your Java code
2. Go to the Run and Debug view (`Ctrl+Shift+D`)
3. Select "Debug JUnit Test" from the dropdown
4. Press F5 to start debugging

## DevContainer Customization

### Adding Dependencies

If you need additional dependencies, you can:

1. Modify the `pom.xml` file to add Java dependencies
2. Modify the `.devcontainer/Dockerfile` to add system dependencies

After modifying the Dockerfile, you'll need to rebuild the container:

1. Press `F1` to open the command palette
2. Type "Remote-Containers: Rebuild Container" and select it

### Customizing VS Code Settings

The DevContainer configuration includes VS Code settings specific to the container environment. You can customize these in:

- `.devcontainer/devcontainer.json`: Container configuration
- `.devcontainer/settings.json`: VS Code settings for the container

## Troubleshooting

### Container Build Fails

If the container build fails, check:

1. Docker Desktop is running
2. You have sufficient disk space
3. The Docker daemon has sufficient resources allocated (memory/CPU)

### Performance Issues

If experiencing slow performance:

1. Increase the resources allocated to Docker in Docker Desktop settings
2. Consider using volume mounts for large directories like `.m2/repository`

### Java 21 and Script Engine Issues

The framework requires Java 21 and specific script engines. If experiencing issues:

1. Check the Java version inside the container: `java -version` (should be 21+)
2. Verify script engines are available:
   ```bash
   java -cp target/jmeter-dsl-framework-1.0.0.jar:target/lib/* com.perftest.utils.ScriptEngineChecker
   ```
3. Ensure all dependencies are correctly installed:
   ```bash
   mvn dependency:tree | grep -E 'nashorn|groovy'
   ```
4. Check that both Nashorn and Groovy dependencies are present in your POM file

## Advanced Usage

### Running with Specific JMeter Properties

To run tests with specific JMeter properties:

```bash
mvn clean test -DjmeterProps="-Jproperty1=value1 -Jproperty2=value2"
```

### Using Shared Maven Repository

To speed up builds by sharing the Maven repository with your host:

```json
// In devcontainer.json
"mounts": [
  "source=${localEnv:HOME}/.m2,target=/root/.m2,type=bind,consistency=cached"
]
```

## Creating a DevContainer Setup

If you're working with a fresh clone of the repository that doesn't have a DevContainer configuration yet, follow these steps to create one:

### Step 1: Generate DevContainer Files

1. In VS Code, press `F1` and search for "Remote-Containers: Add Development Container Configuration Files..."
2. Select "Java" from the list of container templates
3. Select the latest Java version (21)
4. Select any additional features you want to add (Maven, Git, etc.)
5. This will create a `.devcontainer` directory in your project

### Step 2: Customize the Dockerfile

Edit `.devcontainer/Dockerfile` to include the following:

```Dockerfile
FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

# Install Maven
ARG MAVEN_VERSION=3.9.6
RUN su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""

# Set up a non-root user
USER vscode

# Install required packages for JMeter and the framework
RUN mkdir -p /home/vscode/.m2
COPY pom.xml /tmp/
WORKDIR /tmp
RUN mvn dependency:go-offline

# Set the working directory
WORKDIR /workspaces/performance-automation-framework
```

### Step 3: Configure devcontainer.json

Edit the `.devcontainer/devcontainer.json` file to include Java extensions and settings:

```json
{
    "name": "Java Performance Testing",
    "dockerFile": "Dockerfile",
    "extensions": [
        "vscjava.vscode-java-pack",
        "vscjava.vscode-maven",
        "redhat.java",
        "ms-azuretools.vscode-docker",
        "streetsidesoftware.code-spell-checker"
    ],
    "settings": {
        "java.server.launchMode": "Standard",
        "java.configuration.updateBuildConfiguration": "automatic",
        "maven.executable.path": "/usr/local/sdkman/candidates/maven/current/bin/mvn"
    },
    "remoteUser": "vscode"
}
```

## Conclusion

Using DevContainers provides a consistent, reproducible environment for development that closely matches the CI/CD and production environments. This helps eliminate "works on my machine" issues and streamlines onboarding for new developers.

The Performance Automation Framework with Java 21 compatibility requires specific dependencies and configurations that are automatically included in the DevContainer setup, making it easier to get started with development without worrying about script engine compatibility issues.