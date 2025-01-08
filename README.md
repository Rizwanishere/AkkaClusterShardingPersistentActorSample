# Akka Cluster Sharding and Persistent Actors

## 1. What is Cluster Sharding?

Cluster Sharding is a mechanism in Akka that allows you to manage distributed stateful entities (actors) across a cluster of nodes.

### Key Concepts:
- **Shards**: Logical groups of entities that are distributed across the cluster. Each shard manages multiple entities.
- **Entities**: The actual actors performing specific tasks (e.g., user sessions, data processors).
- **Entity ID**: A unique identifier for each entity.
- **Shard Region**: The abstraction that manages sharding logic, routing, and distribution of entities across nodes.

### Flow of Cluster Sharding:
1. **Initialization**:
    - The cluster initializes a `ShardRegion`, which manages all shard-related activities.
    - When you define an `EntityTypeKey`, it represents the type of actor entities that the region will manage.

2. **Incoming Message Routing**:
    - When a message is sent to a `ShardRegion`, it is routed to the appropriate shard and then to the specific entity based on the entity ID.
    - If the entity does not yet exist, the system creates it automatically.

3. **Distribution Across Nodes**:
    - Shards are distributed across cluster nodes. If a node crashes or leaves the cluster, the shards are rebalanced to other nodes, ensuring availability.

4. **Resilience**:
    - If an entity crashes, the system can restart it automatically, as it knows its identity and can recreate its state if necessary.

---

## 2. What is a Persistent Actor?

A Persistent Actor is a special type of actor in Akka that can persist its internal state, making it resilient to restarts, crashes, and node failures. It uses event sourcing to store events that represent state changes.

### Key Concepts:
- **Commands**: Messages that the actor receives to perform actions (e.g., "AddEntry").
- **Events**: Immutable records of actions taken by the actor (e.g., "EntryAdded").
- **State**: The current state of the actor, rebuilt from past events.
- **Journal**: A durable storage medium where events are stored.
- **Snapshot**: A periodic save of the full actor state to optimize recovery.

### Flow of a Persistent Actor:
1. **Receiving a Command**:
    - The actor receives a command (e.g., "AddEntry").
    - It decides whether to persist an event based on the command.

2. **Persisting an Event**:
    - If the command is valid, the actor generates an event (e.g., "EntryAdded") and persists it to the journal.

3. **Applying the Event**:
    - After persisting, the actor applies the event to its internal state (e.g., adding the entry to its list).

4. **Crash Recovery**:
    - On restart, the actor replays all past events from the journal to rebuild its state.
    - If snapshots exist, the actor loads the latest snapshot first, then replays events that occurred after the snapshot.

---

## 3. How Do Cluster Sharding and Persistent Actors Work Together?

### Connecting the Two:
Cluster Sharding distributes entities (actors) across nodes, while Persistent Actors ensure that each entity can retain its state even if the node hosting it fails or the entity is restarted.

### Flow of the Combined Architecture:
1. **Cluster Initialization**:
    - The Akka system starts with multiple nodes forming a cluster.
    - A `ShardRegion` is initialized to manage the entities.

2. **Entity Creation**:
    - When a message is sent to an entity (via its `EntityRef`), the `ShardRegion` determines the shard and the specific entity based on the entity ID.
    - If the entity doesn't exist, the system creates it.
    - If the entity is a Persistent Actor, its state is restored by replaying events from the journal (or snapshots).

3. **Message Handling**:
    - The message is routed to the entity, which processes it and persists any necessary events.
    - The entity's state is updated based on the persisted events.

4. **Node Failure**:
    - If a node crashes, the shards hosted on that node are rebalanced to other nodes.
    - Entities on the rebalanced shards are recreated on the new nodes, with their states recovered using event replay.

5. **Scaling**:
    - As new nodes are added to the cluster, shards are redistributed across the nodes.
    - This ensures that the load is balanced, and no single node becomes a bottleneck.

---

## 4. Example Use Case

### User Session Tracking System:
1. **Entities**:
    - Each user session is represented by an entity (actor).

2. **Sharding**:
    - The system uses cluster sharding to distribute session actors across nodes.
    - Each session has a unique user ID.

3. **Persistence**:
    - Each session actor is a Persistent Actor.
    - It stores actions like "Login", "Add to Cart", "Logout" as events.

4. **Recovery**:
    - If the session actor crashes or the node fails, the actor is restored with its state intact on another node.

5. **Scalability**:
    - As the number of users grows, you can add more nodes to the cluster, and sharding will automatically rebalance the load.

---

## Benefits of This Architecture

1. **Resilience**:
    - Persistent Actors ensure no data is lost on crashes.
    - Cluster Sharding ensures entities are redistributed on node failures.

2. **Scalability**:
    - Entities can scale horizontally across cluster nodes.

3. **State Management**:
    - Persistent Actors handle complex stateful operations seamlessly.

4. **Automatic Load Balancing**:
    - Sharding balances shards across nodes dynamically as the cluster changes.

---

## In Summary

- **Cluster Sharding**: Manages the lifecycle and distribution of stateful entities across the cluster.
- **Persistent Actors**: Manage the durability and recovery of an entity’s state.
- Together, they provide a highly scalable and fault-tolerant architecture for building distributed, stateful systems.
