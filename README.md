[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

# BBFlow
Basic java version of Fastflow Bulding Blocks implementation
ff_node is the basic block entity composed by a set of input and output channels (LinkedList<T>) and a computation code (runJob())
ff_farm is the implementation of the Farm model: Emitter, N Workers and a Collector. All computation code is customizable extending the classes, but normally only the worker needed

### Implementation choiches
Running on the actual latest version of Java (17) exploits all of the advantages introduced in the last year in the JVM.
Each ff_node runs in a Thread and nodes are synchronized using 2 locks, one for input and one for output.
Lock on the input side used to wait new data on input channels if nothing available.
Lock on the output side used to notify there are new data to output channels.
Channels are LinkedList objects and as per definition, getting and removing only first element in position 0, the complexity of r/w operation is O(1)
LinkedList are shared in memory between Threads connected each other.
Nodes can be connected straightly and also feedbacks are possible, just remember to set and send EOF to avoid infinite loops-

### Complexity
Complexity of the LinkedList is O(1) reading/deleting only first element when receiving data on a channel
Complexity of the LinkedList is O(1) while appending a new data/list at the end of the list
There's one lock for all input channels and one lock for all output channels.
Being input and output channels fixed (so linear), the complexity for a node of scanning all input channels when receive a notification is still O(1)

### Communications strategies
By default Farm implements already 3 types of communication with the workers
- ROUNDROBIN: given a list of worker (N), each element received by Emitter is sent to the next worker in the list. When last worker reached, the emitter starts again from the first worker
- SCATTER: Emitter breaks data from input channel in N parts and send the correspondent part to each worker. The custom type must be chosen correclty to exploit the scattering properties. In a common type, like Integers, the scatter acts like ROUNDROBIN
- BROADCAST: each element received by Emitter is sent to all workers
