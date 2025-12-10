
# Token Encryption Service

A Spring Boot service providing AES-256-CBC encryption/decryption capabilities for sensitive token data. Designed as a reusable component that can be injected into other Spring applications.

## Purpose

This service provides secure, production-ready encryption and decryption of token data using AES-256-CBC with PKCS5 padding. Each encryption operation generates a unique random IV (Initialization Vector), ensuring identical plaintexts produce different ciphertexts.

## Intended Audience

- **Backend Developers** integrating encryption into Spring Boot microservices
- **Security Engineers** implementing token protection mechanisms
- **DevOps Engineers** deploying encryption services in AWS environments

## Features

- ✅ AES-256-CBC encryption with random IV per operation
- ✅ Base64-encoded output for easy transmission
- ✅ Flexible key management with fallback chain
- ✅ Spring Boot integration via dependency injection
- ✅ Comprehensive unit and integration tests
- ✅ AWS Secrets Manager integration for production

## Architecture

### Core Components

- **`CryptoService`** - Main service providing `encrypt()` and `decrypt()` methods
- **`KeyProvider`** - Interface for encryption key retrieval
- **`AesKeyProvider`** - Production implementation with fallback chain
- **`CryptoProperties`** - Configuration properties from `application.yml`

### Key Management Strategy

The service uses a **fallback chain** to retrieve the encryption key:

1. **AWS Secrets Manager** (Production) - Primary source for production environments
2. **Environment Variable** (`AES_KEY_B64`) - CI/CD and containerized deployments
3. **Configuration File** (`application.yml`) - Local development only

This design ensures security in production while providing flexibility for development and testing.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Production only) AWS account with Secrets Manager access

## Configuration

### Application Properties

Edit `src/main/resources/application.yml`:
