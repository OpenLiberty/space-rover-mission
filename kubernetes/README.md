# Using Kubernetes with the Space Rover
This document covers the process of configuring the OpenLiberty Space rover to user Kubernetes to manage it's services.

## Getting started with Kubernetes
One of the easiest ways to work with Kubernetes locally is with Docker Desktop. In your Docker Desktop settings, you can enable a local Kubernetes cluster.

## Building your Images for Kubernetes
When running the Open Liberty Space Rover with Docker Compose, the service images are created in a build step within the Docker Compose file. This implies that to use the images for Kubernetes you will have to build all the images manually. You are welcome to do this by hand if you have customizations you wish to make, otherwise we recommend you use the `buildServices.bat` or `buildServices.sh` scripts to build all of the images. 

Users on Mac with Apple Silicon need to enable "Use Rosetta for x86/amd64 emulation on Apple Silicon" in Docker Desktop settings and build images with the `export DOCKER_DEFAULT_PLATFORM=linux/amd64` flag set.

If you decide to use the build scripts, ensure you run the script from the `kubernetes` directory.

After running the scripts you should see the following to indcate that you images are created.
```
Your images are complete!
You can now apply your kubernetes files.
```

## Configuring your Kubernetes Files
By default, the Kubernetes files are configured to run from the local images you just built. Alternatively, you can place these images in a container registry such as OpenShift Container Registry.

### Configure MongoDB
Ensure that the device running the Kubernetes cluster provides an available location to allocate it's persistent volume and configure it so in the `mongo-pv.yaml` file. To do this, navigate to `services\k8s\mongo-pv.yaml` and provide an path to a folder on your device that you wish to allocate, as seen below. In addition, folder must already exist at runtime if you specify it.
```
hostPath:
    # Provide the existing directory below to use as Persistent Volume.
    path: /opt/data/mongo
```

Additionally, create a Secret for the database credentials.
```
kubectl create secret generic mongo-secret \
    --from-literal=MONGO_INITDB_ROOT_USERNAME=<user> \
    --from-literal=MONGO_INITDB_ROOT_PASSWORD=<password> 
```

## Applying you Kubernetes Files to your Cluster
After your images are built and you have configured repo to your use case, you can apply them to your cluster. Firstly, ensure that your cluster is operating properly with the following command:
```
kubectl get nodes
```

You should see the following:
```
NAME             STATUS   ROLES           AGE   VERSION
docker-desktop   Ready    control-plane   22h   v1.24.2
```

Next, it is time to apply the `.yaml` files by navigating to the `services` directory and executing the following command:
```
kubectl apply -f ./k8s
```

It takes roughly 20-30 seconds for the Kubernetes to create all the pods and the containers, so be patient. To check on their status you are able to run the following command:
```
kubectl get pods
```

Once all of your pods are in `Running` status and your ouput looks like the following, then you are able to move onto testing your cluster.
```
NAME                           READY   STATUS    RESTARTS   AGE
client-76bc76b7b4-9qzm2        1/1     Running   0          20s
gameservice-66857669fc-h24rr   1/1     Running   0          20s
grafana-6d6c864f76-859hs       1/1     Running   0          20s
leaderboard-5cd7c966c5-9jvr9   1/1     Running   0          20s
mongo-0                        1/1     Running   0          19s
prometheus-74799df65-4nbxl     1/1     Running   0          19s
```

To run the `mockboard` and `mockrover` services, use files in the `services/k8s/mock` with a recursive apply command:
```
kubectl apply -R -f ./k8s
```

## Testing your Cluster
At this point you should be able to navigate to [Client WebApp](http://localhost:3000) to play the game!

## Shutting Down your Cluster
After you are done testing out your cluster, you are able to shut it down with the executing the following command from the `services/` directory:
```
kubectl delete -f ./k8s
```
The output should be similar to the following:
```
deployment.apps "client" deleted
service "client" deleted
deployment.apps "gameservice" deleted
service "gameservice" deleted
deployment.apps "grafana" deleted
service "grafana" deleted
deployment.apps "leaderboard" deleted
service "leaderboard" deleted
deployment.apps "mockboard" deleted
service "mockboard" deleted
deployment.apps "mockrover" deleted
service "mockrover" deleted
deployment.apps "mongo" deleted
persistentvolume "mongo-data-pv" deleted
persistentvolumeclaim "mongo-pvc-0" deleted
service "mongo" deleted
deployment.apps "prometheus" deleted
service "prometheus" deleted
```

Then, you can run the following command to verify all your containers are down:
```
kubectl get pods
```

Which should give you the following:
```
No resources found in default namespace.
```

