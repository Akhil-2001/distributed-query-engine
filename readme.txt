## Instructions to run -

## Java version - 21.0.8-amzn
## Gradle version - 8.13

## Use SDKMAN to easily manage versions if needed

# Start Orchestrator Node - (Single node)

cd ./orchestrator
./gradlew build --no-daemon
./gradlew :app:run --args="9001 9002 9003"

cd ..

## NOTE - You need to manually ensure workers are up and ports are correct

# Start 3 Worker Nodes - (N Nodes)

cd ./compute-engine
./gradlew :app:run --args="9001 W1" &
./gradlew :app:run --args="9001 W2" &
./gradlew :app:run --args="9001 W3" &

# Invoke the task

curl localhost:9000/startTask

# Enjoy your output in ./compute-engine/output.txt

## SMALL LIMITATION - Restart all workers before starting a new query. I'm not really clearing the transient structures. In a prod scenario, I'd maintain a QueryVsTransientResult map and handle clearing it, which I'm not really doing rn.

## This design should be scalable. You can start 4,5,6 worker nodes.

## Some design philosophy -
## 	Worker :
## 		Each worker node reads one file at a time and processes it one at a time.
##  	Worker node is multi-threaded. Uses a fixed Threadpool internally to chunk the file and process concurrently.
##		The results of each sub-query (csv) are stored in-memory of the worker node.
##
##		Query execution happens in three phases -
##			Read & Merge: 		Read CSV and merge into in-memory transient table (if exists)
##			Shuffle & Reduce:	Pick two nodes (W1,W2), merge their data to reduce the query-result.
##			Write:				Finally when all transient tables are merged, write the output to a file.
##
##		Orchestrator picks 2 (W1,W2) nodes at a time and merges their data in shuffle phase.
##		This design ensures that you can horizontally scale worker nodes to a large number, and orchestrator ensures all their results are merged correctly in a "distributed fashion".
##
##	Orchestrator -
##		Maintains the state of each worker and ensures transition of worker node through different phases.
##		All coordination happens through HTTP REST for simplicity. Async http reqs in some cases where you don't need results.
##

