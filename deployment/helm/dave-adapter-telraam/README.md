# Dave Adapter Helm Chart

## Configuration

The following table lists the configurable parameters of the Dave Adapter chart and their default values.

### App parameters
| Parameter | Description | Default |
|-----------|-------------|---------|
| `app.dave_url` | URL for DAVe backend | `http://localhost:8080/detector/save-latest-detections` |


### Standard values

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Image repository | `starwitorg/dave-adapter` |
| `image.pullPolicy` | Image pull policy | `Always` |
| `image.tag` | Image tag | `${project.version}` |
| `service.port` | Service port | `8080` |
| `extraEnv` | Additional environment variables (YAML list) | `nil` |
| `autoscaling.enabled` | Enable autoscaling | `false` |
| `app.context_path` | Application context path | `""` |
| `spring.security.oauth2.client.registration.daveclient.client_id` | OAuth2 client ID | `dave_api` |
| `spring.security.oauth2.client.registration.daveclient.client_secret` | OAuth2 client secret | `supersecret` |
| `spring.security.oauth2.client.provider.daveprovider.token_uri` | OAuth2 token URI | `https://uri` |



## Example Usage

```bash
#TODO
```

## Mapping Configuration



```json

```
