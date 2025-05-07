FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Install JMeter and other tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    curl \
    wget \
    unzip \
    fontconfig \
    apt-transport-https \
    ca-certificates \
    gnupg \
    software-properties-common \
    nano \
    git \
    bash-completion \
    && apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*