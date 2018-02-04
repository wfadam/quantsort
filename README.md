# quantsort
A spinlock lock implementation for atomic operation amoung JVMs

## Sample code -1- Initialize counters
```java
    @Override
    public void programStartAction() {
        
        quantsort.QuantSort.init("RandomData/lot2cnt.txt"); 
        
    }
```


## Sample code -2- do atomic operations


```java
    quantsort.QuantSort.tryLock();  //blocking unless acquires the lock
    /* 
      Do something exclusive on this JVM 
      while other JVMs are blocked 
    */
    quantsort.QuantSort.freeLock();
```
