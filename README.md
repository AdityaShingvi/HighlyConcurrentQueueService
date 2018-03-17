# Highly Concurrent Scalable Queue Service

================== DESCRIPTION =========================

 * A Queue library that supports concurrent Producer Consumer system.
 * Multiple Producers can produce to the Queue at the same time without blocking
 * each other. Similarly, multiple consumers can consume from the queue without
 * blocking each other. The consumers have a timeout associated with their read
 * operation. During this timeout, they can dequeue the object from the queue
 * and until then the object won't be available for subsequent reads for any other
 * consumers. Only after the timeout, the object will be made available again for
 * read if its not already dequeued. Each object is associated with an ElementId,
 * which is used for the dequeue operation by the consumers. This elementId is returned
 * as a part of {@code ReadResponse}to the consumers as a result of the read() call.
