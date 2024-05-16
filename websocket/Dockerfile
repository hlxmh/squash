# 1
FROM gradle:latest as builder

# 2
WORKDIR /app
COPY . .

# 3
FROM openjdk:latest

# 4
WORKDIR /app
COPY --from=builder /app/build/libs ./
CMD ["java", "-jar", "websocket-all.jar"]
