[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://github.com/st4ck/BBFlow/blob/master/LICENSE)

# BBFlow
Java implementation of Fastflow's Bulding Blocks

**ff_node** is the basic block entity composed by a set of input and output channels (LinkedBlockingQueue<T>) and a computation code (runJob())
  
**ff_farm** is the implementation of the Farm model: Emitter, N Workers and a Collector. All computation code is customizable extending the classes, but normally only the worker needed
  

### Implementation choiches
Running on the actual latest version of Java (17) exploits all of the advantages introduced in the last year in the JVM.
  
Each ff_node runs in a Thread and nodes are synchronized using built-in LinkedBlockingQueue.
  
Channels are LinkedBlockingQueue objects and as per definition, getting and removing only first element in position 0, the complexity of r/w operation is O(1)
  
LinkedBlockingQueue are shared in memory between Threads connected each other.
  
Nodes can be connected straightly and also feedbacks are possible, just remember to set and send EOF to avoid infinite loops.
  

### Complexity
Complexity of the LinkedBlockingQueue is O(1) reading/deleting only first element when receiving data on a channel
  
Complexity of the LinkedBlockingQueue is O(1) while appending a new data/list at the end of the list
  
Being input and output channels fixed (so linear), the complexity for a node of scanning all input channels when receive a notification is still O(1)
  

### Farm's emitter communication strategies
By default Farm's emitter implements already 3 types of communication with the workers
  
- **ROUNDROBIN**: given a list of worker (N), each element received by Emitter is sent to the next worker in the list. When last worker reached, the emitter starts again from the first worker
  
- **SCATTER**: Emitter breaks data from input channel in N parts and send the correspondent part to each worker. The custom type must be chosen correclty to exploit the scattering properties. In a common type, like Integers, the scatter acts like ROUNDROBIN
  
- **BROADCAST**: each element received by Emitter is sent to all workers

### Farm's collector communication strategies
By default Farm's collector implements already 3 types of inbound communication with the workers

- **FIRSTCOME**: given a list of worker (N), retrieve in ROUNDROBIN manner data from workers when available; otherwise next worker is tried. A waiting timeout is set to 50ms by default

- **ROUNDROBIN**: given a list of worker (N), retrieve in ROUNDROBIN manner data from workers waiting for them

- **GATHER**: Collector joins data broke by SCATTER coming from N input channels. Receive N chunks of data, one chunk per channel and send to the output channel
  
### How to use it
Just import package bbflow and use classes as a library
There are different examples in src/tests directory to run and to take inspiration from

### JavaDoc Documentation
JavaDoc Documentation can be found at https://st4ck.github.io/BBFlow/
