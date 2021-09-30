[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://github.com/st4ck/BBFlow/blob/master/LICENSE)

# BBFlow
Java implementation of Fastflow's Bulding Blocks

**block** basic entity of each building blocks. Each building block is of type block<T>

**ff_node** is the basic block entity composed by a set of input and output channels (LinkedBlockingQueue<T>) and a computation code (runJob())
  
**ff_farm** this block is the implementation of the Farm model: Emitter, N Workers and a Collector. All computation code is customizable extending the classes, but normally only the worker needed

**ff_pipeline** this block is the abstraction of a Pipeline allowing to interconnect blocks easily in pipeline manner. Contains a set of blocks<T> (any building blocks) connected each other. Each block can be added with the appendNewBB() function

**ff_all2all** block that combine multiple ff_farm together connecting workers in a N to M manner

### Queues / Channels
**ff_queue** the default class of the queues (channels). Channels are 1-1 between nodes of type SPSC and FIFO
  

### Implementation choiches
Running on the actual latest version of Java (17).

~~Each ff_node runs in a Thread and nodes are synchronized using built-in LinkedBlockingQueue.~~

~~Channels are LinkedBlockingQueue objects and as per definition, getting and removing only first element in position 0, the complexity of r/w operation is O(1)~~

~~LinkedBlockingQueue are shared in memory between Threads connected each other.~~

Nodes can be connected straightly and also feedbacks are possible, just remember to set and send EOS to avoid infinite loops.
  

### Complexity
Complexity of the ff_queue is O(1) reading/deleting only first element when receiving data on a channel
  
Complexity of the ff_queue is O(1) while appending a new data/list at the end of the list
  
Being input and output channels fixed (so linear), the complexity for a node of scanning all input channels when receive a notification is still O(1)
  
An overhead is added only when the throughput of the data is not sufficient to keep channels with at least one element. In this case waiting (and synchronization in the case of BLOCKING queues) can occur.

### Farm's emitter communication strategies
By default Farm's emitter implements already 3 types of communication with the workers
  
- **ROUNDROBIN**: given a list of worker (N), each element received by Emitter is sent to the next worker in the list. When last worker reached, the emitter starts again from the first worker
  
- **SCATTER**: Emitter breaks vector from input channel in N parts and send the correspondent part to each worker.
  
- **BROADCAST**: each element received by Emitter is sent to all workers

### Farm's collector communication strategies
By default Farm's collector implements already 3 types of inbound communication with the workers

- **FIRSTCOME**: given a list of worker (N), retrieve in ROUNDROBIN manner data from workers when available; otherwise next worker is tried. A waiting timeout is set to 50ms by default

- **ROUNDROBIN**: given a list of worker (N), retrieve in ROUNDROBIN manner data from workers waiting for them

- **GATHER**: Collector joins items (or vectors) coming from workers from N input channels. Receive N items (vectors) and generate a new vector containing N items (or the concatenation of source vectors)  and send it to the output channel.
  
### How to use it
Just import package bbflow and use classes as a library
There are different examples in src/tests directory to run and to take inspiration from

### JavaDoc Documentation
JavaDoc Documentation can be found at https://st4ck.github.io/BBFlow/
