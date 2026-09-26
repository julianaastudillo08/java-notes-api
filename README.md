# Java Notes API

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![HTTP](https://img.shields.io/badge/stdlib-HttpServer-blue)
![License](https://img.shields.io/badge/license-MIT-blue)

API REST mínima en **Java puro** (`com.sun.net.httpserver`) — sin Spring, ideal para demostrar fundamentos.

## Endpoints
- `GET /health`
- `GET /notes`
- `POST /notes` `{ "title": "...", "body": "..." }`
- `DELETE /notes/{id}`

## Arranque
```bash
javac src/com/juliana/notesapi/Main.java
java -cp src com.juliana.notesapi.Main
```
http://localhost:4140

## Autora
**Juliana Chantre Astudillo** · [GitHub](https://github.com/jchantre-jpg)
