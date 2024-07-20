A service that allows email to be sent via a grpc interface and a third-party SMPT server written in Kotlin and deployed using docker compose.
   
**Important**: for using the service in production it is required to modify security settings. The service will be running on open port insecurely by default.

### For starting the server:

1. Clone the repository:
  
   ```sh
   git clone https://github.com/GoSturtups/mail_service.git
   ``` 
   And go to mail_service directory:
   ```sh
   cd mail_service
   ```
2. If you want to install docker and create certificates:
   1. Add DNS records for your domain
   2. Run `sh install_debian_12.sh` and follow all the instructions
3. Edit .env file and setup all the environment variables 
4. Run docker compose command:
   ```sh
   docker compose up -d
   ```


If you want to  Protobuf file is in proto/io/grpc/examples/mail/mail.proto directory.