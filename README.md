# Esprit-PI-3A27-2025-2026-HUMA

## Overview
This project was developed as part of the PI – 3rd Year Engineering Program at *Esprit School of Engineering* (Academic Year 2025–2026).

HUMA BD is a JavaFX-based desktop application focused on candidate and recruitment workflow management, with database persistence and external service integrations.

## Features
- Candidate management workflows
- Authentication (signup/login) and session handling
- Dashboard and recruitment-oriented UI flows
- MySQL-backed data persistence
- CV processing utilities and ranking support
- External integrations (mail, SMS, Slack, Trello, facial/sentiment APIs)

## Tech Stack
### Frontend
- JavaFX (FXML)

### Backend
- Java 17
- Maven
- MySQL
- JDBC
- Jackson / org.json

## Architecture
- controller: JavaFX controllers for UI and business flow orchestration
- models: domain entities
- services: business services and data operations
- utils: infrastructure utilities (DB, API clients, storage, messaging)
- resources/fxml: UI views
- resources/sql: SQL scripts and database assets

## Contributors
- Esprit PI Team (TalentFlow)

## Academic Context
Developed at *Esprit School of Engineering – Tunisia*

PI – 3A27 | 2025–2026

## Getting Started
### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+

### Run locally
mvn -q -DskipTests javafx:run

### Build
mvn clean package

## Acknowledgments
- *Esprit School of Engineering* for academic supervision and project framework
- GitHub Education ecosystem for learning and deployment opportunities
