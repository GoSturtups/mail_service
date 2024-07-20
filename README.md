A service that allows email to be sent via a grpc interface and a third-party SMPT server written in Kotlin and deployed using docker compose.
   
**Important**: for using the service in production it is required to modify security settings. The service will be running on open port insecurely by default.

### For starting the server:

1. Clone the repository:
  
   ```sh
   git clone git@github.com:GoSturtups/mail_service.git
   ```
2. open .env file and setup all the environment variables 
3. Run docker compose command:
   ```sh
   docker compose up -d
   ```


If you want to  Protobuf file is in proto/io/grpc/examples/mail/mail.proto directory.