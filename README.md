# User Manager

Spring Boot 3 / Java 21 REST service for user management: create/login users, attach a
profile image (stored in S3), and publish a `ProfileCreatedEvent` to Kafka whenever a
profile is created. See `usermanager-auditor` (separate repo) for the consumer that
audits these events into its own database table.

## Stack

- Java 21, Spring Boot 3.3.10
- PostgreSQL (JPA/Hibernate)
- Kafka producer (`spring-kafka`), JSON-serialized events keyed by `userId` for
  per-user ordering — see `KafkaProducerServiceImpl`
- AWS S3 (SDK v2) for profile image storage
- Spring Boot Actuator — `/actuator/health/liveness` (process only) and
  `/actuator/health/readiness` (includes a DB connectivity check)

## Running locally

The only supported local environment is a [kind](https://kind.sigs.k8s.io/) cluster —
see `~/projects/infra/local/README.md` for full cluster setup (namespace, shared
Postgres, Kafka, ingress-nginx). Once that's up:

```bash
# build the image and load it into kind (kind can't pull from your local docker daemon)
docker build -t usermanager-app:latest .
kind load docker-image usermanager-app:latest --name infra-local

# AWS creds: create this secret once, out-of-band (never put real creds in a values file)
kubectl -n senthil-apis create secret generic usermanager-aws \
  --from-literal=AWS_ACCESS_KEY_ID=... \
  --from-literal=AWS_SECRET_ACCESS_KEY=... \
  --from-literal=AWS_SESSION_TOKEN=

# install/upgrade
helm upgrade --install usermanager ./helm \
  -f ./helm/values-local.yaml \
  --namespace senthil-apis --kube-context kind-infra-local

kubectl --context kind-infra-local -n senthil-apis rollout status deployment/usermanager

# reachable the same way a real ALB would reach it in prod (NodePort -> ingress -> Service -> pod)
curl http://localhost:8080/api/v1/version
```


## API

All endpoints under `/api/v1`:

| Method | Path | Description |
|---|---|---|
| GET | `/version` | Health/version check, returns `v1` |
| POST | `/users` | Create a user (JSON body: `userName`, `userPassword`) |
| GET | `/users/login?userName=&password=` | Login |
| PUT | `/users/profile?userid=&profileUrl=` | Attach a profile URL to a user; publishes a `ProfileCreatedEvent` to Kafka in the same DB transaction (Kafka failure rolls back the DB write — see `UserServiceImpl.addProfileToUser`) |
| GET | `/users/{userid}/profile` | Fetch a user + profile URL |
| POST | `/users/image` (multipart, field `file`) | Upload an image to S3, returns its URL |
| GET | `/users/image?imageHash=` | Check an image exists in S3 |
| DELETE | `/users/image?imageHash=` | Delete an image from S3 |

OpenAPI docs (springdoc) are available at `/api-docs` once the app is running.

## Kafka topic

Events are published to `logged-in-users` (3 partitions), keyed by `userId` so all
events for the same user land on the same partition and stay ordered. The producer
blocks on send (`.get(5, TimeUnit.SECONDS)`) inside the `@Transactional` method, so a
Kafka failure rolls back the associated database write rather than silently dropping
the event.
